package com.hbcy.authcenter.api.modules.intgr.oa.service;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.common.constants.G;
import com.hbcy.authcenter.api.common.enums.OrgNodeCategoryEnum;
import com.hbcy.authcenter.api.common.enums.OrgNodeTypeEnum;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.api.modules.core.org.service.OrgTreeService;
import com.hbcy.authcenter.api.modules.core.perm.dao.PermUnitUserMapper;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnitUser;
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
import com.hbcy.common.web.api.NamedId;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    public static final String OA_ORG_KEY = "%s_%s";
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
    @Value("${oa.client-id:}")
    private String oaOauthClientId;

    @Resource
    private PermUnitUserMapper permUnitUserMapper;
    @Resource
    private UserOrgMapper userOrgMapper;

    @PostConstruct
    public void init() {
        rsa = new RSA(null, spk);
    }

    public OaAccessDataDTO getOaAccessHeaders() {
        String uid = UserContextUtils.getUserId();
        User user = userService.getById(uid);
        if (user == null || user.getForbidden() == 1) {
            throw new PermissionError();
        }
        if (StringUtils.isBlank(user.getSrcId())) {
            throw new ParamError("用户未绑定oa账号");
        }
        return getOaAccessHeaders(user.getSrcId());
    }

    /**
     * 结合服务端认证和OAUTH跳转
     * @param requestId 待办关联流程的requestId
     * @return 地址和header
     */
    public OaAccessDataDTO accessWorkflow(String requestId) {
        OaAccessDataDTO dto = getOaAccessHeaders();
        String page = UriComponentsBuilder.fromUriString(oaUri)
                .path("/spa/workflow/static4form/index.html#/main/workflow/req")
                .queryParam("requestid", requestId).toUriString();
        String target = UriComponentsBuilder.fromUriString(oaUri)
                .path("/sso/oauth2.0/authorize")
                .queryParam("client_id", oaOauthClientId)
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", page).toUriString();
        dto.setUri(target);
        return dto;
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
        //oa的组织id与我方组织id的双向映射
        Map<String, String> oaId2Id = new HashMap<>();
        //目前已存在的，从OA同步的部门或子公司
        //统一平台里面手动添加的忽略
        List<OrgTree> exists = orgTreeService.list(
                new QueryWrapper<OrgTree>().eq(OrgTree.COL_TENANT_ID, tenantId)
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

    /**
     * 从OA按组织同步人员
     * 1. 获取OA中全量的人员（包括关联的组织）
     * 2. 根据OA的手机号查询统一平台人员，过滤出没有用户id的人，为这些人更新用户id（on duplicate key update的方式批量更新）
     *    此时可以统计出本次同步影响的所有存量用户id（统一平台侧）
     * 3. 以insert...on duplicate update的方式插入人员，如果存在则更新手机号或其他字段(此时冲突的就是用户id了）
     * 4. 逻辑删除oa中不存在的且src_id不为空人员，以及授权
     * 5. 删除2中存量用户的任职关系(main_job=1)，然后在user_org中以insert ignore的方式批量插入用户与部门的关联关系
     *
     * @param oaOrgId 楚禹公司在OA的组织id
     * @param tenantId 楚禹公司在统一平台的租户id
     */
    public void syncUser(String oaOrgId, String tenantId, Map<String, String> typeIdDict) {
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
            return;
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
        //FIXME: 领导班子手机号脱敏了，需要想办法拿到完整的手机号
        personList.getDatas().removeIf(
                dto -> StringUtils.isBlank(dto.getMobile()) || dto.getMobile().contains("*"));
        Map<String, String> phoneSrcIdMap = new HashMap<>();
        for (OaPersonDTO dto : personList.getDatas()) {
            phoneSrcIdMap.put(dto.getMobile(), dto.getId());
        }
        //如果手机号已经存在，但是没有用户id，则填充用户id，一般不会太多
        List<User> missIdUsers = userService.list(new QueryWrapper<User>()
                .eq(User.COL_TENANT_ID, tenantId)
                .in(User.COL_PHONE, phoneSrcIdMap.keySet())
                .isNull(User.COL_SRC_ID));
        if (!missIdUsers.isEmpty()) {
            for (User user : missIdUsers) {
                user.setSrcType(G.USER_SOURCE_OA);
                user.setSrcId(phoneSrcIdMap.get(user.getPhone()));
            }
            oaSyncMapper.fillSourceId(missIdUsers);
            log.info("fill user oa count:{}", missIdUsers.size());
        }
        List<NamedId> userPhoneIds = oaSyncMapper.listUserPhone(tenantId);
        Map<String, String> phoneIdMap = userPhoneIds.stream().
                collect(Collectors.toMap(NamedId::getItemName, NamedId::getItemId));
        List<UserOrg> userOrgs = new ArrayList<>();
        Set<String> updatedUsers = new HashSet<>();
        //获取所有有用户id的用户
        List<User> toUpsert = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (OaPersonDTO data : personList.getDatas()) {
            User user = new User();
            String existId = phoneIdMap.get(data.getMobile());
            if (existId == null) {
                user.setId(UlidCreator.getUlid().toString());
            } else {
                user.setId(existId);
                updatedUsers.add(existId);
            }
            user.setPhone(data.getMobile());
            user.setTenantId(tenantId);
            user.setPasswd(userService.createPass(user.getPhone()));
            user.setRealName(data.getLastname());
            user.setEmail(StringUtils.isBlank(data.getEmail()) ? null : data.getEmail());
            user.setSrcType(G.USER_SOURCE_OA);
            user.setSrcId(data.getId());
            user.setAccount(user.getPhone());
            user.setUpdateTime(now);
            toUpsert.add(user);
            UserOrg userOrg = new UserOrg();
            userOrg.setId(UlidCreator.getUlid().toString());
            userOrg.setUserId(user.getId());
            String relateId = OA_ORG_KEY.formatted(OaConstants.ORG_TYPE_DEPARTMENT, data.getDepartmentid());
            String orgRelateId = OA_ORG_KEY.formatted(OaConstants.ORG_TYPE_SUBCOMPANY, data.getSubcompanyid1());
            if (StringUtils.isBlank(data.getDepartmentid())) {
                relateId = orgRelateId;
            }
            userOrg.setNodeId(typeIdDict.get(relateId));
            userOrg.setOrgId(typeIdDict.get(orgRelateId));
            userOrg.setTenantId(tenantId);
            userOrg.setMainJob(1);
            userOrgs.add(userOrg);
        }
        //逻辑删除之前同步过来的、现在手机号已经不存在于OA中的用户
        userService.update(new UpdateWrapper<User>()
                .eq(User.COL_TENANT_ID, tenantId)
                .notIn(User.COL_PHONE, phoneIdMap.keySet())
                .isNotNull(User.COL_SRC_ID)
                .set(User.COL_DELETE_TIME, System.currentTimeMillis())
                .set(User.COL_UPDATE_TIME, LocalDateTime.now())
                .set(User.COL_UPDATE_USER, "0"));
        //更新用户信息
        if (!toUpsert.isEmpty()) oaSyncMapper.upsertUsers(toUpsert);
        //更新用户部门
        if (!updatedUsers.isEmpty()) {
            userOrgMapper.delete(new QueryWrapper<UserOrg>()
                    .eq(UserOrg.COL_TENANT_ID, tenantId)
                    .in(UserOrg.COL_USER_ID, updatedUsers)
                    .eq(UserOrg.COL_MAIN_JOB, 1));
        }
        if (!userOrgs.isEmpty()) oaSyncMapper.upsertUserOrgs(userOrgs);
    }
}
