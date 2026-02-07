package com.hbcy.authcenter.api.modules.intgr.oa.service;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.f4b6a3.ulid.UlidCreator;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.hbcy.authcenter.api.common.constants.G;
import com.hbcy.authcenter.api.common.enums.OrgNodeCategoryEnum;
import com.hbcy.authcenter.api.common.enums.OrgNodeTypeEnum;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.api.modules.core.org.service.OrgTreeService;
import com.hbcy.authcenter.api.modules.core.perm.dao.PermUnitUserMapper;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnitUser;
import com.hbcy.authcenter.api.modules.core.user.dao.UserMapper;
import com.hbcy.authcenter.api.modules.core.user.dao.UserOrgMapper;
import com.hbcy.authcenter.api.modules.core.user.model.User;
import com.hbcy.authcenter.api.modules.core.user.model.UserOrg;
import com.hbcy.authcenter.api.modules.core.user.service.UserService;
import com.hbcy.authcenter.api.modules.intgr.oa.constants.OaConstants;
import com.hbcy.authcenter.api.modules.intgr.oa.dao.OaSyncMapper;
import com.hbcy.authcenter.api.modules.intgr.oa.dto.*;
import com.hbcy.authcenter.api.modules.intgr.oa.feign.OaAuthClient;
import com.hbcy.authcenter.api.modules.intgr.oa.feign.OaBizClient;
import com.hbcy.authcenter.api.modules.intgr.oa.vo.OaOrgQueryVO;
import com.hbcy.authcenter.api.modules.intgr.oa.vo.OaPageSizeModifyVO;
import com.hbcy.authcenter.api.modules.intgr.oa.vo.OaPersonQueryVO;
import com.hbcy.authcenter.api.modules.intgr.oa.vo.OaQueryTableVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ClientError;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.error.PermissionError;
import com.hbcy.common.base.error.ServerError;
import com.hbcy.common.base.json.JsonUtils;
import com.hbcy.common.lock.service.RedissonDistributedLock;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hbcy.authcenter.api.modules.intgr.oa.constants.OaConstants.OA_ORG_KEY;

/**
 * @author 姚泰然
 * @date 2026-01-19 17:33
 */
@Service
@Slf4j
public class EcologyService {
    public static final String LOCK_KEY = "portal:oa:token:lock";
    public static final String TOKEN_KEY = "portal:oa:token";
    public static final int EXPIRE_TIME = 3540;
    public static final String SYNC_LOCK = "portal:oa:sync:lock";
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private OaAuthClient oaAuthClient;
    @Resource
    private OaBizClient oaBizClient;
    @Resource
    private RedissonDistributedLock redissonDistributedLock;
    @Resource
    private UserService userService;
    @Resource
    private OrgTreeService orgTreeService;
    @Resource
    private OaSyncMapper oaSyncMapper;

    private RSA rsa;
    /**
     * OA系统返回的密钥
     */
    @Value("${oa.secret:}")
    private String secret;
    /**
     * OA返回的公钥
     */
    @Value("${oa.public-key:}")
    private String spk;
    /**
     * 注册在oa里面的appId
     */
    @Value("${oa.app-id:ChuyuPortalDev}")
    private String appId;
    @Value("${oa.url:}")
    private String oaUri;
    @Value("${oa.sso-id:}")
    private String ssoId;

    @Resource
    private PermUnitUserMapper permUnitUserMapper;
    @Resource
    private UserOrgMapper userOrgMapper;
    @Resource
    private UserMapper userMapper;

    @PostConstruct
    public void init() {
        rsa = new RSA(null, spk);
    }

    private User getUser() {
        String uid = UserContextUtils.getUserId();
        User user = userService.getById(uid);
        if (user == null || user.getForbidden() == 1) {
            throw new PermissionError();
        }
        if (StringUtils.isBlank(user.getSrcId())) {
            throw new ParamError("用户未绑定oa账号");
        }
        return user;
    }

    /**
     * 获取OA系统访问header
     * 由于浏览器的限制，打开页面不能携带header，所以这个功能主要用于后端调用API
     * @return header数据
     */
    public OaAccessDataDTO getOaAccessHeaders() {
        User user = getUser();
        String srcId = user.getSrcId();
        if (srcId.contains(",")) {
            srcId = srcId.split(",")[0];
        }
        return getOaAccessHeaders(srcId);
    }

    /**
     * 结合服务端认证和OAUTH跳转
     * @param srcId 待办关联流程的requestId
     * @param srcUser 待办关联流程的用户id
     * @return 地址和header
     */
    public String accessWorkflow(String srcId, String srcUser) {
        User user = getUser();
        //see: https://www.e-cology.com.cn/sp/ebdcus/ktree/help/freepass?pathKey=aW50ZWdyYXRpb24vb2F1dGgyX3NlcnZlcg==&lang=7
        String token = oaAuthClient.getSSOToken(ssoId, user.getPhone());
        if (token.startsWith("Token")) {
            throw new ServerError(token);
        }
        String uri = oaUri + "/spa/workflow/static4form/index.html";
        uri += "?ssoToken=" + token;
        uri += "#/main/workflow/req";
        uri += "?requestid=" + srcId;
        String allSrcId = user.getSrcId();
        //如果不是主账号，需要跳转
        if (!allSrcId.equals(srcId) && !allSrcId.startsWith(srcId + ",")) {
            uri += "&f_weaver_belongto_usertype=0&f_weaver_belongto_userid=" + srcUser;
        }
        return uri;
    }

    private String tryFetchToken() {
        String token = stringRedisTemplate.opsForValue().get(TOKEN_KEY);
        if (StringUtils.isNotBlank(token)) {
            return token;
        }
        if (redissonDistributedLock.tryLock(LOCK_KEY, TimeUnit.SECONDS, 10, 30)) {
            //已经被更新了，直接返回
            token = stringRedisTemplate.opsForValue().get(TOKEN_KEY);
            if (StringUtils.isNotBlank(token)) {
                return token;
            }
            try {
                String encryptSecret = rsa.encryptBase64(secret, CharsetUtil.CHARSET_UTF_8, KeyType.PublicKey);
                String respStr = oaAuthClient.applyToken(appId, encryptSecret, String.valueOf(EXPIRE_TIME));
                OaApplyTokenResp resp = JsonUtils.readValue(respStr, OaApplyTokenResp.class);
                if (resp == null) {
                    throw new ServerError("服务通信错误，请重试");
                }
                if (resp.getCode() == 0) {
                    token = resp.getToken();
                    stringRedisTemplate.opsForValue().set(TOKEN_KEY, token, EXPIRE_TIME, TimeUnit.SECONDS);
                }
            } finally {
                redissonDistributedLock.unlock(LOCK_KEY);
            }
        }
        return stringRedisTemplate.opsForValue().get(TOKEN_KEY);
    }

    /**
     * 获取OA访问header，相当于服务端替用户完成了登录
     * @param userId oa中的用户id，不是手机号
     * @return 跳转链接
     */
    public OaAccessDataDTO getOaAccessHeaders(String userId) {
        String token = tryFetchToken();
        if (StringUtils.isBlank(token)) {
            throw new ServerError("服务通信错误，请重试");
        }
        String uid = rsa.encryptBase64(userId, CharsetUtil.CHARSET_UTF_8, KeyType.PublicKey);
        return new OaAccessDataDTO().setAppid(appId).setUserid(uid).setToken(token);
    }

    //xxl-job触发同步，参数是json格式
    @Transactional
    @XxlJob("oaSync")
    public void sync(String params) {
        JsonNode node = JsonUtils.readTree(params);
        if (node == null) {
            throw new ClientError("参数格式错误");
        }
        doSync(node.get("oaOrgId").asText(), node.get("tenantId").asText());
    }

    @Async
    @Transactional
    public void sync(String oaOrgId, String tenantId) {
        doSync(oaOrgId, tenantId);
    }

    private void doSync(String oaOrgId, String tenantId) {
        boolean ok = redissonDistributedLock.tryLock(SYNC_LOCK, TimeUnit.SECONDS, 1, 30);
        if (!ok) {
            return;
        }
        Map<String, String> oaId2id = syncOrg(oaOrgId, tenantId);
        syncUser(oaOrgId, tenantId, oaId2id);
    }

    //查询OA中楚禹下面的所有部门和分部（子公司）
    //OA中分部的id和部门的id可能冲突，所以key是type_id，不能直接用id
    //返回parent key和直接子级
    private Map<String, OaOrgDTO> queryFullOrg(String oaOrgId, Map<String, String> relateIdMap, String tenantId) {
        Map<String, OaOrgDTO> typeIdDict = new HashMap<>();
        //循环依据
        List<String> loopIds = new ArrayList<>();
        //楚禹公司的id是固定的
        String rootId = OA_ORG_KEY.formatted(OaConstants.ORG_TYPE_SUBCOMPANY, oaOrgId);
        loopIds.add(rootId);
        //重新计算所有的层级
        Map<String, String> parentIdPathMap = new HashMap<>();
        parentIdPathMap.put(rootId, OrgTree.ORG_ID_TEMPLATE.formatted(tenantId, 0) + G.ID_PATH_SPLITTER +
                OrgTree.ORG_ID_TEMPLATE.formatted(tenantId, 1));
        //分级循环查询直到找到所有的子级
        while (!loopIds.isEmpty()) {
            List<String> parentIds = new ArrayList<>(loopIds);
            loopIds.clear();
            for (String parentId : parentIds) {
                String[] ps = parentId.split("_");
                var vo = new OaOrgQueryVO();
                vo.setType(ps[0]);
                vo.setId(ps[1]);
                String orgStr = oaBizClient.queryOrg(vo);
                OaOrgListDTO dto = JsonUtils.readValue(orgStr, OaOrgListDTO.class);
                if (dto == null || dto.getStatus() != null && !dto.getStatus()) {
                    throw new ServerError("服务通信错误，请重试");
                }
                int order = 0;
                for (OaOrgDTO d : dto.getDatas()) {
                    order++;
                    d.setMyOrder(order);
                    //记录映射
                    String typeId = OA_ORG_KEY.formatted(d.getType(), d.getId());
                    d.setMyRelateId(typeId);
                    typeIdDict.put(typeId, d);
                    if (Boolean.TRUE.equals(d.getIsParent())) {
                        loopIds.add(typeId);
                    }
                    if (relateIdMap.containsKey(typeId)) {
                        d.setMyId(relateIdMap.get(typeId));
                        d.setMyNew(false);
                    } else {
                        d.setMyNew(true);
                        if (d.getType().equals(OaConstants.ORG_TYPE_DEPARTMENT)) {
                            d.setMyId(orgTreeService.generateId(OrgNodeTypeEnum.DEPT.getValue(), tenantId));
                        } else {
                            d.setMyId(orgTreeService.generateId(OrgNodeTypeEnum.ORG.getValue(), tenantId));
                        }
                        relateIdMap.put(typeId, d.getMyId());
                    }
                    d.setMyIdPath(parentIdPathMap.get(parentId) + G.ID_PATH_SPLITTER + d.getMyId());
                    d.setMyParentId(relateIdMap.get(parentId));
                    parentIdPathMap.put(d.getId(), d.getMyIdPath());
                }
            }
        }
        return typeIdDict;
    }

    /**
     * 从OA同步楚禹公司的部门
     * 除项目部之外的组织，从OA同步，同步逻辑如下：
     * 1. 首先分层查询OA中完整的组织树（OA不支持全量查询），在内存中以列表形式缓存
     * 2. 查询统一平台楚禹组织列表，收集relate_id与id的映射
     * 3. 计算2与1的差集，即为删除的组织，需要做批量逻辑删除
     * 3. 计算1与2的差集，即为新增的组织，批量插入统一平台组织树
     * 5. 1与2的交集，即为需要更新的组织，逐条更新（可以检查核心字段是否有变化，无变化则无需更新）
     *
     * @param oaOrgId 楚禹公司在OA的组织id
     * @param tenantId 楚禹公司在统一平台中的租户id
     */
    public Map<String, String> syncOrg(String oaOrgId, String tenantId) {
        //oa的组织id与我方组织id的映射（含部门）
        Map<String, String> oaId2Id = new HashMap<>();
        //目前已存在的，从OA同步的部门或子公司（排除项目部，项目部的relate_id是项目编号）
        //统一平台里面手动添加的忽略
        List<OrgTree> exists = orgTreeService.list(
                new QueryWrapper<OrgTree>().eq(OrgTree.COL_TENANT_ID, tenantId)
                        .ne(OrgTree.COL_NODE_CATEGORY, OrgNodeCategoryEnum.PROJECT.getValue())
                        .isNotNull(OrgTree.COL_RELATE_ID));
        for (OrgTree existsOrg : exists) {
            oaId2Id.put(existsOrg.getRelateId(), existsOrg.getId());
        }
        //楚禹公司的映射
        String rootId = OA_ORG_KEY.formatted(OaConstants.ORG_TYPE_SUBCOMPANY, oaOrgId);
        oaId2Id.put(rootId, OrgTree.ORG_ID_TEMPLATE.formatted(tenantId, 1));
        Map<String, OaOrgDTO> typeIdDict = queryFullOrg(oaOrgId, oaId2Id, tenantId);
        //待删除
        Set<String> toDeleteRelateIds = new HashSet<>(oaId2Id.keySet());
        toDeleteRelateIds.removeAll(typeIdDict.keySet());
        toDeleteRelateIds.remove(rootId);
        Set<String> toDeleteIds = new HashSet<>();
        for (String relateId : toDeleteRelateIds) {
            toDeleteIds.add(oaId2Id.get(relateId));
        }
        //删除不存在的组织部门和相关数据
        if (!toDeleteIds.isEmpty()) {
            orgTreeService.update(new UpdateWrapper<OrgTree>().eq(OrgTree.COL_TENANT_ID, tenantId)
                    .in(OrgTree.COL_ID, toDeleteIds)
                    .set(OrgTree.COL_DELETE_TIME, System.currentTimeMillis())
                    .set(OrgTree.COL_UPDATE_TIME, LocalDateTime.now())
                    .set(OrgTree.COL_UPDATE_USER, "0"));
            userOrgMapper.delete(new QueryWrapper<UserOrg>()
                    .in(UserOrg.COL_NODE_ID, toDeleteIds)
                    .eq(UserOrg.COL_TENANT_ID, tenantId));
            permUnitUserMapper.delete(new QueryWrapper<PermUnitUser>()
                    .in(PermUnitUser.COL_ORG_ID, toDeleteIds)
                    .eq(PermUnitUser.COL_TENANT_ID, tenantId));
        }
        List<OrgTree> toInsert = new ArrayList<>();
        List<OrgTree> toUpdate = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (OaOrgDTO dto : typeIdDict.values()) {
            OrgTree tree = new OrgTree();
            tree.setId(dto.getMyId());
            tree.setNodeName(dto.getName());
            tree.setShortName(dto.getName());
            tree.setIdPath(dto.getMyIdPath());
            tree.setParentId(dto.getMyParentId());
            tree.setNodeType(dto.getType().equals(OaConstants.ORG_TYPE_DEPARTMENT) ?
                    OrgNodeTypeEnum.DEPT.getValue() : OrgNodeTypeEnum.ORG.getValue());
            tree.setNodeCategory(dto.getType().equals(OaConstants.ORG_TYPE_DEPARTMENT) ?
                    OrgNodeCategoryEnum.COMPANY.getValue() : OrgNodeCategoryEnum.SUB_COMPANY.getValue());
            tree.setExistType("1".equals(dto.getIsVirtual()) ? OrgTree.EXIST_TYPE_VIRTUAL : OrgTree.EXIST_TYPE_ENTITY);
            tree.setShowOrder(dto.getMyOrder());
            tree.setTenantId(tenantId);
            tree.setRelateId(dto.getMyRelateId());
            tree.setCreateTime(now);
            tree.setUpdateTime(now);
            if (dto.isMyNew()) {
                toInsert.add(tree);
            } else {
                toUpdate.add(tree);
            }
        }
        if (!toUpdate.isEmpty()) oaSyncMapper.batchUpdateOrg(toUpdate);
        if (!toInsert.isEmpty()) oaSyncMapper.batchInsertOrg(toInsert);
        log.info("sync oa org, delete:{}, add:{}, update:{}", toDeleteIds.size(), toInsert.size(), toUpdate.size());
        //同步用户的时候可以用来反查
        return oaId2Id;
    }

    private List<OaPersonDTO> fetchOaPerson(String oaOrgId) {
        OaPersonQueryVO vo = new OaPersonQueryVO();
        vo.setSubcompanyid1(oaOrgId);
        String sessionStr = oaBizClient.queryPersonSession(vo);
        OaTableSessionDTO session = JsonUtils.readValue(sessionStr, OaTableSessionDTO.class);
        if (session == null || (session.getStatus() != null && !session.getStatus())) {
            throw new ServerError("无法同步用户");
        }
        OaQueryTableVO queryTableVO = new OaQueryTableVO();
        queryTableVO.setDataKey(session.getSessionkey());
        String s = oaBizClient.queryTableCount(queryTableVO);
        OaTableCountDTO countDTO = JsonUtils.readValue(s, OaTableCountDTO.class);
        if (countDTO == null || countDTO.getCount() == null || countDTO.getCount() == 0) {
            log.info("no oa user to sync");
            return Collections.emptyList();
        }
        OaPageSizeModifyVO pageSizeModifyVO = new OaPageSizeModifyVO();
        pageSizeModifyVO.setDataKey(session.getSessionkey());
        pageSizeModifyVO.setPageSize(countDTO.getCount());
        //修改session分页大小
        oaBizClient.modifyTablePageSize(pageSizeModifyVO);
        String personListStr = oaBizClient.queryTableData(queryTableVO);
        OaPersonListDTO personList = JsonUtils.readValue(personListStr, OaPersonListDTO.class);
        if (personList == null || !personList.getStatus()) {
            throw new ServerError("获取OA用户失败");
        }
        //合并主子账号
        Map<String, OaPersonDTO> oaIdPersonMap = personList.getDatas().stream().collect(
                Collectors.toMap(OaPersonDTO::getId, dto -> dto));
        List<OaPersonDTO> resp = new ArrayList<>();
        for (OaPersonDTO data : personList.getDatas()) {
            if (data.isMainAccount()) {
                //FIXME: 领导班子手机号脱敏了，需要想办法拿到完整的手机号
                if (StringUtils.isNotBlank(data.getMobile()) && !data.getMobile().contains("*")) {
                    resp.add(data);
                }
            } else {
                OaPersonDTO mainAccount = oaIdPersonMap.get(data.getBelongto());
                mainAccount.getSubAccounts().add(data);
            }
        }
        return resp;
    }

    private void fillUserOrg(OaPersonDTO person, String userId, String tenantId,
                             Map<String, String> typeIdDict, List<UserOrg> userOrgs) {
        UserOrg userOrg = new UserOrg();
        userOrg.setId(UlidCreator.getUlid().toString());
        userOrg.setUserId(userId);
        userOrg.setTenantId(tenantId);
        userOrg.setOrgId(typeIdDict.get(person.getOrgRelateId()));
        userOrg.setNodeId(typeIdDict.get(person.getRelateId()));
        userOrg.setMainJob(1);
        userOrgs.add(userOrg);
        for (OaPersonDTO subAccount : person.getSubAccounts()) {
            UserOrg subUserOrg = new UserOrg();
            subUserOrg.setUserId(userId);
            subUserOrg.setTenantId(tenantId);
            subUserOrg.setOrgId(typeIdDict.get(subAccount.getOrgRelateId()));
            subUserOrg.setNodeId(typeIdDict.get(subAccount.getRelateId()));
            subUserOrg.setMainJob(subAccount.getSubcompanyid1().equals(person.getSubcompanyid1()) ? 1 : 0);
            userOrgs.add(subUserOrg);
        }
    }

    /**
     * 从OA按组织同步人员
     * 1. 获取OA中全量的人员（包括关联的组织）
     * 2. 获取数据库中全量的OA同步的成员，根据OA主账号ID映射，确认增、删、改。
     *  OA子账号影响user_org表的任职和perm_unit_user表里面的授权，需要注意的是后者按组织（而非部门），因此需要计算组织变动。
     *  对于user_org表，可以直接清空update用户后重新插入，perm_unit_user则计算出移除的用户组织映射后删除对应的权限即可。
     * 3. OA的用户系统核心是一个主职多个兼职，使用不同的id，待办、消息都是拆开的，所以我们需要记录所有的用户id才能完成数据的同步。
     *
     * @param oaOrgId 楚禹公司在OA的组织id
     * @param tenantId 楚禹公司在统一平台的租户id
     */
    public void syncUser(String oaOrgId, String tenantId, Map<String, String> typeIdDict) {
        List<OaPersonDTO> personList = fetchOaPerson(oaOrgId);
        if (CollectionUtils.isEmpty(personList)) {
            return;
        }
        Map<String, OaPersonDTO> oaIdPersonMap = personList.stream().collect(
                Collectors.toMap(OaPersonDTO::getId, dto -> dto));
        //获取已有的所有同步过来的用户，判断用户是否更新了手机号（情况比较罕见）
        List<User> users = oaSyncMapper.listOaUsers(tenantId);
        Map<String, User> oaIdUserMap = new HashMap<>();
        //用户oa主账号id和用户id的双向映射
        BiMap<String, String> oaId2Id = HashBiMap.create();
        for (User user : users) {
            if (user.getSrcId().contains(",")) {
                String[] split = user.getSrcId().split(",");
                oaIdUserMap.put(split[0], user);
                oaId2Id.put(split[0], user.getId());
            } else {
                oaIdUserMap.put(user.getSrcId(), user);
                oaId2Id.put(user.getSrcId(), user.getId());
            }
        }
        //不存在的oaId，直接删除用户
        Set<String> toDeleteOaIds = new HashSet<>(oaIdUserMap.keySet());
        toDeleteOaIds.removeAll(oaIdPersonMap.keySet());
        Set<String> toDeleteIds = new HashSet<>();
        for (String toDeleteOaId : toDeleteOaIds) {
            toDeleteIds.add(oaIdUserMap.get(toDeleteOaId).getId());
        }
        if (!toDeleteIds.isEmpty()) {
            userService.update(new UpdateWrapper<User>()
                    .eq(User.COL_TENANT_ID, tenantId)
                    .in(User.COL_ID, toDeleteIds)
                    .set(User.COL_DELETE_TIME, System.currentTimeMillis())
                    .set(User.COL_UPDATE_TIME, LocalDateTime.now())
                    .set(User.COL_UPDATE_USER, "0"));
            userOrgMapper.delete(new QueryWrapper<UserOrg>()
                    .in(UserOrg.COL_USER_ID, toDeleteIds));
            permUnitUserMapper.delete(new QueryWrapper<PermUnitUser>()
                    .in(PermUnitUser.COL_USER_ID, toDeleteIds));
            log.info("oa sync user, delete user count:{}", toDeleteIds.size());
        }
        //需要更新用户资料的用户
        Set<String> toUpdateOaIds = new HashSet<>(oaIdUserMap.keySet());
        toUpdateOaIds.retainAll(oaIdPersonMap.keySet());
        Set<String> toUpdateIds = toUpdateOaIds.stream().map(
                oaId2Id::get).collect(Collectors.toSet());
        List<UserOrg> toUpsertUserOrgs = new ArrayList<>();
        //逐个更新，一般不会太多
        int updateUserCnt = 0;
        for (String oid : toUpdateOaIds) {
            User user = oaIdUserMap.get(oid);
            OaPersonDTO person = oaIdPersonMap.get(oid);
            boolean update = false;
            if (!user.getPhone().equals(person.getMobile())) {
                user.setPhone(person.getMobile());
                user.setAccount(person.getMobile());
                update = true;
            }
            if (!user.getRealName().equals(person.getLastname())) {
                user.setRealName(person.getLastname());
                update = true;
            }
            if (!Objects.equals(user.getEmail(), person.getEmail())) {
                user.setEmail(person.getEmail());
                update = true;
            }
            String srcIds = person.getSrcIds();
            if (!user.getSrcId().equals(srcIds)) {
                user.setSrcId(srcIds);
                update = true;
            }
            if (update) {
                user.setUpdateTime(LocalDateTime.now());
                user.setUpdateUser("0");
                try {
                    userService.updateById(user);
                } catch (DuplicateKeyException e) {
                    log.error("update user error:{}", user);
                    continue;
                }
                updateUserCnt++;
            }
            fillUserOrg(person, user.getId(), tenantId, typeIdDict, toUpsertUserOrgs);
        }
        log.info("oa sync user, update user count:{}", updateUserCnt);
        if (!toUpdateIds.isEmpty()) {
            //已有用户的任职统计
            Map<String, Set<String>> userOrgMap = new HashMap<>();
            //oa用户的任职统计
            Map<String, Set<String>> userOaOrgMap = new HashMap<>();
            List<UserOrg> userOrgs = oaSyncMapper.fetchUserOrgs(toUpdateIds);
            for (UserOrg userOrg : userOrgs) {
                userOrgMap.computeIfAbsent(userOrg.getUserId(),
                        k -> new HashSet<>()).add(userOrg.getOrgId());
            }
            for (OaPersonDTO dto : personList) {
                String orgRelateId = OA_ORG_KEY.formatted(
                        OaConstants.ORG_TYPE_SUBCOMPANY, dto.getSubcompanyid1());
                userOaOrgMap.computeIfAbsent(oaId2Id.get(dto.getId()),
                        k -> new HashSet<>()).add(typeIdDict.get(orgRelateId));
            }
            //对比任职公司有变化的人，移除其权限
            for (Map.Entry<String, Set<String>> entry : userOrgMap.entrySet()) {
                entry.getValue().removeAll(userOaOrgMap.get(entry.getKey()));
                if (!entry.getValue().isEmpty()) {
                    permUnitUserMapper.delete(new UpdateWrapper<PermUnitUser>()
                            .eq(PermUnitUser.COL_USER_ID, entry.getKey())
                            .in(PermUnitUser.COL_ORG_ID, entry.getValue()));
                    log.info("oa sync user, delete perm for user {} in org {}", entry.getKey(), entry.getValue());
                }
            }
            //删除存量用户的所有任职，重新插入
            userOrgMapper.delete(new QueryWrapper<UserOrg>()
                    .in(UserOrg.COL_USER_ID, toUpdateIds));
            log.info("oa sync user, delete user org count:{}", toUpdateIds.size());
        }

        //新增的用户
        Set<String> toInsertOaIds = new HashSet<>(oaIdPersonMap.keySet());
        toInsertOaIds.removeAll(toUpdateOaIds);
        Set<String> newIds = new HashSet<>();
        if (!toInsertOaIds.isEmpty()) {
            List<User> toInsert = new ArrayList<>();
            for (String oid : toInsertOaIds) {
                OaPersonDTO person = oaIdPersonMap.get(oid);
                User user = new User();
                user.setId(UlidCreator.getUlid().toString());
                newIds.add(user.getId());
                user.setPhone(person.getMobile());
                user.setAccount(person.getMobile());
                user.setTenantId(tenantId);
                user.setPasswd(userService.createPass(user.getPhone()));
                user.setRealName(person.getLastname());
                user.setEmail(person.getEmail());
                user.setSrcType(G.USER_SOURCE_OA);
                user.setSrcId(person.getSrcIds());
                user.setForbidden(0);
                user.setCreateUser("0");
                user.setUpdateUser("0");
                toInsert.add(user);
                fillUserOrg(person, user.getId(), tenantId, typeIdDict, toUpsertUserOrgs);
            }
            userMapper.insertIgnore(toInsert);
            //确认一下哪些插入失败了
            Set<String> inserted = oaSyncMapper.ensureIds(newIds);
            log.info("oa sync user, insert user count:{}, success count:{}", toInsert.size(), inserted.size());
            if (inserted.size() < newIds.size()) {
                //没有插入成功的用户，需要移除掉任职关系
                newIds.removeAll(inserted);
                toUpsertUserOrgs.removeIf(uo -> newIds.contains(uo.getUserId()));
            }
        }
        if (!toUpsertUserOrgs.isEmpty()) {
            oaSyncMapper.upsertUserOrgs(toUpsertUserOrgs);
            log.info("oa sync user, upsert user org count:{}", toUpsertUserOrgs.size());
        }
    }
}
