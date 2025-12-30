package com.hbcy.authcenter.api.modules.core.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.hbcy.authcenter.api.common.constants.G;
import com.hbcy.authcenter.api.common.enums.OrgNodeTypeEnum;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.api.modules.core.org.service.OrgTreeService;
import com.hbcy.authcenter.api.modules.core.perm.dao.PermUnitUserMapper;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnitUser;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantMapper;
import com.hbcy.authcenter.api.modules.core.tenant.model.Tenant;
import com.hbcy.authcenter.api.modules.core.user.dao.UserMapper;
import com.hbcy.authcenter.api.modules.core.user.dto.OrgUserDTO;
import com.hbcy.authcenter.api.modules.core.user.dto.UserExportDTO;
import com.hbcy.authcenter.api.modules.core.user.dto.UserOrgDTO;
import com.hbcy.authcenter.api.modules.core.user.dto.UserQueryResultDTO;
import com.hbcy.authcenter.api.modules.core.user.model.User;
import com.hbcy.authcenter.api.modules.core.user.model.UserOrg;
import com.hbcy.authcenter.api.modules.core.user.utils.PasswordUtils;
import com.hbcy.authcenter.api.modules.core.user.vo.*;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.error.PermissionError;
import com.hbcy.common.base.error.ServerError;
import com.hbcy.common.base.pojo.BatchDeleteVO;
import com.hbcy.common.base.pojo.PageResp;
import com.hbcy.common.base.util.BeanCopyUtils;
import com.hbcy.common.db.model.PageRespEx;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 姚泰然
 * @date 2025-12-26 09:25
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    private static final String ALLOWED_ACCOUNT_SYMBOLS = "_";
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserOrgService userOrgService;
    @Resource
    private OrgTreeService orgTreeService;
    @Resource
    private TenantMapper tenantMapper;
    @Resource
    private PermUnitUserMapper permUnitUserMapper;

    /**
     * 辅助判断：是否是纯文字（排除掉空格和常见的各种标点符号）
     */
    private static boolean isPureText(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            // 排除空格
            if (Character.isWhitespace(c)) return false;
            // 排除标点符号和特殊符号 (Character.isLetter 能够识别中文、日文等文字)
            if (!Character.isLetter(c)) return false;
        }
        return true;
    }

    private void cleanNameCache(String userId) {
        stringRedisTemplate.opsForHash().delete(G.USER_NAME_CACHE_KEY, userId);
    }

    private void cleanNameCache(List<String> userIds) {
        stringRedisTemplate.opsForHash().delete(G.USER_NAME_CACHE_KEY, userIds.toArray());
    }

    private void checkAnyExist(UserCreateVO vo) {
        var tenantId = UserContextUtils.getTenantId();
        Long cnt = baseMapper.selectCount(new QueryWrapper<User>()
                .eq(User.COL_TENANT_ID, tenantId)
                .eq(User.COL_ACCOUNT, vo.getAccount()));
        if (cnt > 0) {
            throw new ParamError("账号已存在");
        }
        cnt = baseMapper.selectCount(new QueryWrapper<User>()
                .eq(User.COL_TENANT_ID, tenantId)
                .eq(User.COL_PHONE, vo.getPhone()));
        if (cnt > 0) {
            throw new ParamError("手机号已存在");
        }
        if (StringUtils.isNotBlank(vo.getEmail())) {
            cnt = baseMapper.selectCount(new QueryWrapper<User>()
                    .eq(User.COL_TENANT_ID, tenantId)
                    .eq(User.COL_EMAIL, vo.getEmail()));
            if (cnt > 0) {
                throw new ParamError("邮箱已存在");
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public String createUser(UserCreateVO vo) {
        checkAnyExist(vo);
        String tenantId = UserContextUtils.getTenantId();
        String op = UserContextUtils.getUserId();
        OrgTree node = orgTreeService.getById(vo.getNodeId());
        if (node == null || !node.getTenantId().equals(tenantId)) {
            throw new ParamError("组织选择错误");
        }
        String orgId = node.getId();
        if (node.getNodeType().equals(OrgNodeTypeEnum.DEPT.getValue())) {
            orgId = OrgTreeService.findDeptDirectOrg(node.getIdPath());
        }
        User user = new User();
        BeanCopyUtils.copy(vo, user);
        // 默认密码为手机号
        user.setPasswd(passwordEncoder.encode(vo.getPhone()));
        //TODO: 改为随机密码+短信、邮件发送密码
        user.setCreateUser(op);
        user.setUpdateUser(op);
        user.setTenantId(tenantId);
        try {
            save(user);
        } catch (DuplicateKeyException e) {
            throw new ParamError("用户的账号、手机号或邮箱已存在，请检查");
        }
        // 关联组织
        userOrgService.addUserNode(user.getId(), orgId, vo.getNodeId(), true);
        return user.getId();
    }

    private User checkUser(String userId) {
        User user = getById(userId);
        if (user == null) {
            throw new ParamError("用户不存在");
        }
        if (!user.getTenantId().equals(UserContextUtils.getTenantId())) {
            throw new PermissionError();
        }
        return user;
    }

    public void updateUser(String userId, UserUpdateVO vo) {
        var user = checkUser(userId);
        if (vo.getRealName() != null && !vo.getRealName().equals(user.getRealName())) {
            cleanNameCache(userId);
        }
        BeanCopyUtils.copy(vo, user);
        user.setUpdateUser(UserContextUtils.getUserId());
        updateById(user);
    }

    public void deleteUser(String userId) {
        User user = getById(userId);
        if (user == null) {
            return;
        }
        if (user.getForbidden() == 0) {
            throw new ParamError("删除用户前需要先禁用用户");
        }
        String tenantId = UserContextUtils.getTenantId();
        if (!user.getTenantId().equals(tenantId)) {
            throw new PermissionError();
        }
        Tenant tenant = tenantMapper.selectById(user.getTenantId());
        if (tenant == null) {
            throw new ServerError("租户被移除，请联系管理员");
        }
        if (userId.equals(tenant.getAdminId())) {
            throw new ParamError("无法删除租户默认管理员");
        }
        cleanNameCache(userId);
        //删除所有角色授权
        permUnitUserMapper.delete(new QueryWrapper<PermUnitUser>()
                .eq(PermUnitUser.COL_USER_ID, userId));
        removeById(userId);
    }

    public void forbidUser(UserForbidVO vo) {
        var user = checkUser(vo.getUserId());
        user.setForbidden(vo.getForbidden());
        user.setUpdateUser(UserContextUtils.getUserId());
        updateById(user);
    }

    public void adminResetPasswd(AdminResetPasswdVO vo) {
        var user = checkUser(vo.getUserId());
        if (!PasswordUtils.isValid(vo.getPassword())) {
            throw new ParamError("密码强度要求：至少8位，且包含大小写字母、数字和符号中的至少3项");
        }
        user.setPasswd(passwordEncoder.encode(vo.getPassword()));
        user.setUpdateUser(UserContextUtils.getUserId());
        updateById(user);
    }

    public void userResetPasswd(UserResetPasswdVO vo) {
        User user = getById(UserContextUtils.getUserId());
        if (!passwordEncoder.matches(vo.getOldPasswd(), user.getPasswd())) {
            throw new ParamError("旧密码不正确");
        }
        if (!PasswordUtils.isValid(vo.getPassword())) {
            throw new ParamError("密码强度要求：至少8位，且包含大小写字母、数字和符号中的至少3项");
        }
        User toUpdate = new User();
        toUpdate.setId(user.getId());
        toUpdate.setPasswd(passwordEncoder.encode(vo.getPassword()));
        toUpdate.setUpdateUser(UserContextUtils.getUserId());
        updateById(user);
    }


    public void deleteUsers(@Valid BatchDeleteVO vo) {
        var tenantId = UserContextUtils.getTenantId();
        boolean exists = exists(new QueryWrapper<User>()
                .eq(User.COL_FORBIDDEN, "0")
                .in(User.COL_ID, vo.getIds()));
        if (exists) {
            throw new ParamError("删除用户前需要先禁用用户");
        }
        cleanNameCache(vo.getIds());
        remove(new QueryWrapper<User>()
                .eq(User.COL_TENANT_ID, tenantId)
                .in(User.COL_ID, vo.getIds()));
        //删除所有角色授权
        permUnitUserMapper.delete(new QueryWrapper<PermUnitUser>()
                .eq(PermUnitUser.COL_TENANT_ID, tenantId)
                .in(PermUnitUser.COL_USER_ID, vo.getIds()));
    }

    public UserQueryResultDTO getUser(String userId) {
        checkUser(userId);
        UserQueryVO vo = new UserQueryVO();
        vo.setUserId(userId);
        PageResp<UserQueryResultDTO> resp = queryUser(vo);
        if (resp.getTotal() == 0) {
            return null;
        }
        return resp.getList().get(0);
    }

    public Set<String> guessKeywordType(String keyword) {
        Set<String> result = new HashSet<>();
        if (keyword == null || keyword.isEmpty()) {
            return result;
        }
        boolean hasDigit = false;
        boolean hasLetter = false;
        boolean hasSymbol = false; // 下划线等允许在账号中的符号
        boolean hasInvalidForAccount = false; // 账号不该有的字符（如中文、空格、特殊标点）

        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);

            if (c >= '0' && c <= '9') {
                hasDigit = true;
            } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                hasLetter = true;
            } else if (ALLOWED_ACCOUNT_SYMBOLS.indexOf(c) != -1) {
                hasSymbol = true;
            } else {
                // 既不是数字字母，也不是账号允许的符号，也不是@
                // 这通常意味着是中文、空格或其他特殊符号
                hasInvalidForAccount = true;
            }
        }

        // 1. 只有数字 (不能有符号、字母、@、或其它)
        if (hasDigit && !hasLetter && !hasSymbol && !hasInvalidForAccount) {
            result.add("PHONE");
            result.add("ACCOUNT");
            return result;
        }

        // 2. 包含 [数字、字母、符号]
        if (!hasInvalidForAccount && (hasDigit || hasLetter || hasSymbol)) {
            result.add("ACCOUNT");
            return result;
        }

        // 3. NAME: 纯文字
        if (isPureText(keyword)) {
            result.add("NAME");
        }
        return result;
    }

    private void checkParams(UserQueryVO vo) {
        if (StringUtils.isNotBlank(vo.getNodeId())) {
            //过滤了组织
            OrgTree org = orgTreeService.getById(vo.getNodeId());
            if (org == null) {
                throw new ParamError("指定组织不存在");
            }
            if (!org.getTenantId().equals(UserContextUtils.getTenantId())) {
                throw new PermissionError();
            }
            if (vo.getLevel() == UserQueryVO.LEVEL_ALL) {
                vo.setIdPathPrefix(org.getIdPath());
                vo.setNodeId(null);
            } else if (vo.getLevel() == UserQueryVO.LEVEL_FOLLOWER) {
                vo.setIdPathPrefix(org.getIdPath() + G.ID_PATH_SPLITTER);
                vo.setNodeId(null);
            }
        }
        if (StringUtils.isNotBlank(vo.getKeyword())) {
            vo.setKeywordType(guessKeywordType(vo.getKeyword()));
        }
        vo.setTenantId(UserContextUtils.getTenantId());
    }

    /**
     * 查询用户，以用户为基准汇总
     *
     * @param vo 查询条件
     * @return 列表结果
     */
    public PageResp<UserQueryResultDTO> queryUser(UserQueryVO vo) {
        checkParams(vo);
        Page<UserQueryResultDTO> dbPage = vo.getDbPage();
        Page<UserQueryResultDTO> page = baseMapper.queryUser(dbPage, vo);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return new PageResp<>();
        }
        List<String> userIds = page.getRecords().stream().map(UserQueryResultDTO::getId).toList();
        //然后查询每个人的所有任职组织及其概况
        List<UserOrgDTO> userOrgs = userOrgService.listUserOrgs(userIds);
        //根据orgId查询orgName
        Set<String> orgIds = new HashSet<>();
        for (UserOrgDTO userOrg : userOrgs) {
            orgIds.addAll(Splitter.on(G.ID_PATH_SPLITTER).splitToList(userOrg.getIdPath()));
        }
        Map<String, String> idNameMap = orgTreeService.getOrgNameMap(orgIds, vo.isUseFullName());
        //回填userOrg
        for (UserOrgDTO userOrg : userOrgs) {
            List<String> nameParts = Splitter.on(G.ID_PATH_SPLITTER).splitToList(userOrg.getIdPath()).stream()
                    .map(k -> idNameMap.getOrDefault(k, "")).toList();
            userOrg.setNamePath(Joiner.on("/").join(Lists.reverse(nameParts)));
        }
        //按用户分组映射
        Map<String, List<UserOrgDTO>> userOrgMap = userOrgs.stream().collect(
                Collectors.groupingBy(UserOrgDTO::getUserId));
        Tenant tenant = tenantMapper.selectById(UserContextUtils.getTenantId());
        for (UserQueryResultDTO dto : page.getRecords()) {
            List<UserOrgDTO> orgs = userOrgMap.get(dto.getId());
            if (orgs != null) {
                orgs.sort(Comparator.comparing(UserOrgDTO::getMainJob).reversed());
            }
            dto.setOrgList(orgs);
            dto.setTenantAdmin(tenant.getAdminId().equals(dto.getId()));
        }
        return new PageRespEx<>(page);
    }

    /**
     * 查询用户，以组织为基准
     * 用于选人窗口
     *
     * @param vo 筛选条件
     * @return 分页结果
     */
    public PageResp<OrgUserDTO> filterUser4Select(UserQueryVO vo) {
        checkParams(vo);
        Page<?> page = vo.getDbPage();
        Page<OrgUserDTO> result = baseMapper.filterUser4Select(page, vo);
        //填充namePath
        Set<String> nodeIds = new HashSet<>();
        for (OrgUserDTO r : result.getRecords()) {
            nodeIds.addAll(Splitter.on(G.ID_PATH_SPLITTER).splitToList(r.getIdPath()));
        }
        Map<String, String> idNameMap = orgTreeService.getOrgNameMap(nodeIds, vo.isUseFullName());
        //回填userOrg
        for (OrgUserDTO r : result.getRecords()) {
            List<String> nameParts = Splitter.on(G.ID_PATH_SPLITTER).splitToList(r.getIdPath()).stream()
                    .map(k -> idNameMap.getOrDefault(k, "")).toList();
            r.setNamePath(Joiner.on("/").join(Lists.reverse(nameParts)));
        }
        return new PageRespEx<>(result);
    }

    /**
     * 批量导入用户
     *
     * @param dataList 列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchInsert(List<UserImportVO> dataList) {
        String tenantId = UserContextUtils.getTenantId();
        Set<String> orgNameSet = new HashSet<>();
        Set<String> accountSet = new HashSet<>();
        Set<String> phoneSet = new HashSet<>();
        Set<String> emailSet = new HashSet<>();
        for (UserImportVO d : dataList) {
            //NOTE: 批量导入的时候，只能指定组织，不能指定部门
            orgNameSet.add(d.getOrgName());
            if (accountSet.contains(d.getAccount())) {
                throw new ParamError("表格中存在重复的账号:%s", d.getAccount());
            }
            accountSet.add(d.getAccount());
            if (phoneSet.contains(d.getPhone())) {
                throw new ParamError("表格中存在重复的手机号:%s", d.getPhone());
            }
            phoneSet.add(d.getPhone());
            if (StringUtils.isNotBlank(d.getEmail())) {
                if (emailSet.contains(d.getEmail())) {
                    throw new ParamError("表格中存在重复的邮箱:%s", d.getEmail());
                }
                emailSet.add(d.getEmail());
            }
        }
        //先校验组织名
        List<OrgTree> orgList = orgTreeService.list(new QueryWrapper<OrgTree>()
                .eq(OrgTree.COL_TENANT_ID, tenantId)
                .eq(OrgTree.COL_NODE_TYPE, OrgNodeTypeEnum.ORG.getValue())
                .in(OrgTree.COL_NODE_NAME, orgNameSet));
        if (orgList.size() != orgNameSet.size()) {
            throw new ParamError("部分组织名错误，请检查");
        }
        //转成组织名和组织ID的映射表
        var orgNameIdMap = orgList.stream().collect(
                Collectors.toMap(OrgTree::getNodeName, OrgTree::getId));
        //账号、手机号、邮箱都要验重
        List<User> existUsers = baseMapper.selectList(new QueryWrapper<User>()
                .or()
                .in(User.COL_ACCOUNT, accountSet)
                .in(User.COL_PHONE, phoneSet)
                .in(User.COL_EMAIL, emailSet));
        if (!CollectionUtils.isEmpty(existUsers)) {
            StringBuilder sb = new StringBuilder();
            sb.append("以下手机号对应的用户已存在:");
            existUsers.forEach(user -> sb.append(user.getPhone()).append(","));
            throw new ParamError(sb.toString());
        }
        //数据准备
        String createUser = UserContextUtils.getUserId();
        List<User> users = new ArrayList<>();
        List<UserOrg> userOrgs = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (UserImportVO d : dataList) {
            //用户
            User u = new User();
            BeanCopyUtils.copy(d, u);
            u.setId(UlidCreator.getUlid().toString());
            u.setCreateUser(createUser);
            u.setUpdateUser(createUser);
            u.setTenantId(tenantId);
            u.setCreateTime(now);
            u.setUpdateTime(now);
            //TODO:改为随机密码+短信通知
            u.setPasswd(passwordEncoder.encode(d.getPhone()));
            users.add(u);
            //用户与组织的关联
            UserOrg uo = new UserOrg();
            uo.setId(UlidCreator.getUlid().toString());
            uo.setUserId(u.getId());
            uo.setOrgId(orgNameIdMap.get(d.getOrgName()));
            uo.setTenantId(tenantId);
            uo.setMainJob(1);
            uo.setCreateUser(createUser);
            uo.setUpdateUser(createUser);
            uo.setCreateTime(now);
            uo.setUpdateTime(now);
            userOrgs.add(uo);
        }
        try {
            //批量插入用户
            baseMapper.insert(users);
            //批量插入关联关系
            userOrgService.saveBatch(userOrgs);
        } catch (DuplicateKeyException e) {
            throw new ParamError("用户数据重复，请检查");
        }
    }

    public List<UserExportDTO> export(UserQueryVO vo) {
        checkParams(vo);
        Page<?> dbPage = vo.getDbPage();
        Page<UserQueryResultDTO> result = baseMapper.queryUser(dbPage, vo);
        //全量导出
        dbPage.setSize(-1L);
        List<UserExportDTO> users = new ArrayList<>();
        Set<String> userIds = new HashSet<>();
        result.getRecords().forEach(u -> {
            UserExportDTO copy = BeanCopyUtils.copy(u, UserExportDTO.class);
            userIds.add(u.getId());
            users.add(copy);
        });
        List<UserOrgDTO> userMainOrgs = userOrgService.listUserOrgs(userIds, true);
        Map<String, String> orgNameMap = userMainOrgs.stream().collect(
                Collectors.toMap(UserOrgDTO::getUserId, UserOrgDTO::getOrgName));
        for (UserExportDTO user : users) {
            user.setOrgName(orgNameMap.get(user.getOrgName()));
        }
        return users;
    }
}
