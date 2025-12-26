package com.hbcy.authcenter.api.modules.core.user.service;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbcy.authcenter.api.common.constants.G;
import com.hbcy.authcenter.api.common.enums.OrgNodeTypeEnum;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.api.modules.core.org.service.OrgTreeService;
import com.hbcy.authcenter.api.modules.core.user.dao.UserMapper;
import com.hbcy.authcenter.api.modules.core.user.model.User;
import com.hbcy.authcenter.api.modules.core.user.model.UserOrg;
import com.hbcy.authcenter.api.modules.core.user.vo.*;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.error.PermissionError;
import com.hbcy.common.base.error.ServerError;
import com.hbcy.common.base.pojo.BatchDeleteVO;
import com.hbcy.common.base.util.BeanCopyUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2025-12-26 09:25
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserOrgService userOrgService;
    @Resource
    private OrgTreeService orgTreeService;

    private void cleanNameCache(String userId) {
        stringRedisTemplate.opsForHash().delete(G.USER_NAME_CACHE_KEY, userId);
    }

    private void cleanNameCache(List<String> userIds) {
        stringRedisTemplate.opsForHash().delete(G.USER_NAME_CACHE_KEY, userIds.toArray());
    }


    private void checkAnyExist(CreateUserVO vo) {
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
    public String createUser(CreateUserVO vo) {
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
        // 设置默认密码
        user.setPasswd(passwordEncoder.encode(RandomUtil.randomString(6)));
        //TODO: 短信、邮件发送密码
        user.setCreateUser(op);
        user.setUpdateUser(op);
        user.setTenantId(tenantId);
        user.setDefaultOrg(orgId);
        try {
            save(user);
        } catch (DuplicateKeyException e) {
            throw new ParamError("用户的账号、手机号或邮箱已存在，请检查");
        }
        // 关联组织
        userOrgService.addUserNode(user.getId(), node);
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

    public void updateUser(String userId, UpdateUserVO vo) {
        var user = checkUser(userId);
        BeanCopyUtils.copy(vo, user);
        user.setUpdateUser(UserContextUtils.getUserId());
        cleanNameCache(userId);
        updateById(user);
    }

    public void deleteUser(String userId) {
        User user = getById(userId);
        if (user == null) {
            return;
        }
        if (!user.getTenantId().equals(UserContextUtils.getTenantId())) {
            throw new PermissionError();
        }
        removeById(userId);
    }

    public void forbidUser(ForbidUserVO vo) {
        var user = checkUser(vo.getUserId());
        user.setForbidden(vo.getForbidden());
        user.setUpdateUser(UserContextUtils.getUserId());
        updateById(user);
    }

    public void adminResetPasswd(AdminResetPasswdVO vo) {
        var user = getById(vo.getUserId());
        user.setPasswd(passwordEncoder.encode(vo.getPassword()));
        user.setUpdateUser(UserContextUtils.getUserId());
        updateById(user);
    }

    public void userResetPasswd(UserResetPasswdVO vo) {
        User user = getById(UserContextUtils.getUserId());
        if (!passwordEncoder.matches(vo.getOldPasswd(), user.getPasswd())) {
            throw new ParamError("旧密码不正确");
        }
        User toUpdate = new User();
        toUpdate.setId(user.getId());
        toUpdate.setPasswd(passwordEncoder.encode(vo.getPassword()));
        toUpdate.setUpdateUser(UserContextUtils.getUserId());
        updateById(user);
    }

    public void switchDefaultOrg(SwitchDefaultOrgVO vo) {
        User user;
        if (StringUtils.isBlank(vo.getUserId())) {
            vo.setUserId(UserContextUtils.getUserId());
            user = getById(vo.getUserId());
        } else {
            user = checkUser(vo.getUserId());
        }
        // 检查用户是否在该组织下
        boolean any = userOrgService.exists(new QueryWrapper<UserOrg>()
                .eq(UserOrg.COL_USER_ID, vo.getUserId())
                .eq(UserOrg.COL_ORG_ID, vo.getOrgId()));
        if (!any) {
            throw new ServerError("用户不在此组织中");
        }
        User toUpdate = new User();
        toUpdate.setId(user.getId());
        toUpdate.setDefaultOrg(vo.getOrgId());
        toUpdate.setUpdateUser(UserContextUtils.getUserId());
        updateById(toUpdate);
    }

    public void deleteUsers(@Valid BatchDeleteVO vo) {
        var tenantId = UserContextUtils.getTenantId();
        remove(new QueryWrapper<User>()
                .eq(User.COL_TENANT_ID, tenantId)
                .in(User.COL_ID, vo.getIds()));
    }
}
