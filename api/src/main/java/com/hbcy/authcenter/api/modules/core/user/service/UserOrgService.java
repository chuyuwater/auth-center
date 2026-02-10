package com.hbcy.authcenter.api.modules.core.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.common.enums.OrgNodeTypeEnum;
import com.hbcy.authcenter.api.modules.core.org.dao.OrgTreeMapper;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.api.modules.core.org.service.OrgTreeService;
import com.hbcy.authcenter.api.modules.core.perm.dao.PermUnitUserMapper;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnitUser;
import com.hbcy.authcenter.api.modules.core.user.dao.UserMapper;
import com.hbcy.authcenter.api.modules.core.user.dao.UserOrgMapper;
import com.hbcy.authcenter.api.modules.core.user.dto.UserOrgDTO;
import com.hbcy.authcenter.api.modules.core.user.model.User;
import com.hbcy.authcenter.api.modules.core.user.model.UserOrg;
import com.hbcy.authcenter.api.modules.core.user.vo.SwitchDefaultOrgVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.error.PermissionError;
import com.hbcy.common.base.error.ServerError;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author 姚泰然
 * @date 2025-12-26 20:47
 */
@Service
public class UserOrgService extends ServiceImpl<UserOrgMapper, UserOrg> {
    @Resource
    private OrgTreeMapper orgTreeMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private PermUnitUserMapper permUnitUserMapper;


    @Transactional
    public void addUserNode(String userId, List<String> nodeIds) {
        String tenantId = UserContextUtils.getTenantId();
        String createUser = UserContextUtils.getUserId();
        List<OrgTree> nodes = orgTreeMapper.selectList(new QueryWrapper<OrgTree>()
                .eq(OrgTree.COL_TENANT_ID, tenantId)
                .in(OrgTree.COL_ID, nodeIds));
        if (nodes.size() < nodeIds.size()) {
            throw new ParamError("组织选择错误");
        }
        String mainOrg = getMainJobOrg(userId);
        List<UserOrg> userOrgs = new ArrayList<>();
        for (OrgTree node : nodes) {
            String orgId = node.getId();
            if (node.getNodeType().equals(OrgNodeTypeEnum.DEPT.getValue())) {
                orgId = OrgTreeService.findDeptDirectOrg(node.getIdPath());
            }
            if (orgId == null) {
                throw new ParamError("组织选择错误");
            }
            UserOrg userOrg = new UserOrg();
            userOrg.setId(UlidCreator.getUlid().toString());
            userOrg.setOrgId(orgId);
            userOrg.setNodeId(node.getId());
            userOrg.setUserId(userId);
            userOrg.setTenantId(node.getTenantId());
            userOrg.setMainJob(orgId.equals(mainOrg) ? 1 : 0);
            userOrg.setCreateUser(createUser);
            userOrg.setUpdateUser(createUser);
            userOrgs.add(userOrg);
        }
        baseMapper.insertIgnore(userOrgs);
    }

    public String getMainJobOrg(String userId) {
        UserOrg userOrg = baseMapper.selectOne(new QueryWrapper<UserOrg>()
                .eq(UserOrg.COL_USER_ID, userId), false);
        return userOrg == null ? null : userOrg.getOrgId();
    }

    public void addUserNode(String userId, String orgId, OrgTree node, Boolean mainJob) {
        UserOrg userOrg = new UserOrg();
        userOrg.setUserId(userId);
        userOrg.setOrgId(orgId);
        userOrg.setNodeId(node.getId());
        if (mainJob != null) {
            //只有在创建用户时，才能设置为主职
            userOrg.setMainJob(mainJob ? 1 : 0);
        } else {
            //其他时候根据已有数据判断，无法在添加的时候切换主职
            String mainJobOrg = getMainJobOrg(userId);
            userOrg.setMainJob(orgId.equals(mainJobOrg) ? 1 : 0);
        }
        userOrg.setTenantId(node.getTenantId());
        userOrg.setCreateUser(UserContextUtils.getUserId());
        userOrg.setUpdateUser(UserContextUtils.getUserId());
        try {
            save(userOrg);
        } catch (DuplicateKeyException e) {
            throw new ParamError("该任职已存在");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void switchDefaultOrg(SwitchDefaultOrgVO vo) {
        User user;
        if (StringUtils.isBlank(vo.getUserId())) {
            vo.setUserId(UserContextUtils.getUserId());
        } else {
            user = userMapper.selectById(vo.getUserId());
            if (!user.getTenantId().equals(UserContextUtils.getTenantId())) {
                throw new PermissionError();
            }
        }
        // 检查用户是否在该组织下
        boolean any = this.exists(new QueryWrapper<UserOrg>()
                .eq(UserOrg.COL_USER_ID, vo.getUserId())
                .eq(UserOrg.COL_ORG_ID, vo.getOrgId()));
        if (!any) {
            throw new ServerError("用户不在此组织中");
        }
        baseMapper.updateMainJob(vo.getUserId(), vo.getOrgId());
        baseMapper.updatePartJob(vo.getUserId(), vo.getOrgId());
    }

    public List<UserOrgDTO> listUserOrgs(Collection<String> userIds, boolean onlyMain) {
        return baseMapper.listUserOrgs(userIds, onlyMain, null);
    }

    public List<UserOrgDTO> listUserOrgs(Collection<String> userIds) {
        return baseMapper.listUserOrgs(userIds, false, null);
    }

    public List<UserOrgDTO> listUserOrgNodes(Set<String> userIds) {
        return baseMapper.listUserOrgNodes(userIds, false);
    }

    /**
     * 将用户从组织/部门中移除
     *
     * @param nodeId 关联关系的id
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeUserOrg(String nodeId) {
        UserOrg userOrg = baseMapper.selectOne(new QueryWrapper<UserOrg>()
                .eq(UserOrg.COL_USER_ID, UserContextUtils.getUserId())
                .eq(UserOrg.COL_NODE_ID, nodeId));
        if (userOrg == null) {
            return;
        }
        if (!userOrg.getTenantId().equals(UserContextUtils.getTenantId())) {
            throw new PermissionError();
        }
        if (userOrg.getMainJob() == 1) {
            //至少保留1个主职部门的任职
            long count = count(new QueryWrapper<UserOrg>()
                    .eq(UserOrg.COL_USER_ID, userOrg.getUserId())
                    .eq(UserOrg.COL_MAIN_JOB, 1));
            if (count == 1) {
                throw new ParamError("至少保留1个主职组织的任职");
            }
        }
        //如果删除之后用户与当前组织的关系为空，则删除该用户在组织中的所有赋权
        long cnt = count(new QueryWrapper<UserOrg>()
                .eq(UserOrg.COL_USER_ID, userOrg.getUserId())
                .eq(UserOrg.COL_ORG_ID, userOrg.getOrgId()));
        if (cnt == 1) {
            permUnitUserMapper.delete(new QueryWrapper<PermUnitUser>()
                    .eq(PermUnitUser.COL_USER_ID, userOrg.getUserId())
                    .eq(PermUnitUser.COL_ORG_ID, userOrg.getOrgId()));
        }
        removeById(userOrg.getId());
    }
}
