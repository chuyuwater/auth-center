package com.hbcy.authcenter.api.modules.core.user.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbcy.authcenter.api.common.enums.OrgNodeTypeEnum;
import com.hbcy.authcenter.api.modules.core.org.dao.OrgTreeMapper;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.api.modules.core.org.service.OrgTreeService;
import com.hbcy.authcenter.api.modules.core.user.dao.UserOrgMapper;
import com.hbcy.authcenter.api.modules.core.user.model.UserOrg;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.error.PermissionError;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * @author 姚泰然
 * @date 2025-12-26 20:47
 */
@Service
public class UserOrgService extends ServiceImpl<UserOrgMapper, UserOrg> {
    @Resource
    private OrgTreeMapper orgTreeMapper;

    public void addUserNode(String userId, String nodeId) {
        OrgTree node = orgTreeMapper.selectById(nodeId);
        if (node == null) {
            throw new ParamError("指定组织/部门不存在");
        }
        String tenantId = UserContextUtils.getTenantId();
        if (!tenantId.equals(node.getTenantId())) {
            throw new PermissionError();
        }
        addUserNode(userId, node);
    }

    public void addUserNode(String userId, OrgTree node) {
        String orgId = node.getId();
        String deptId = "";
        boolean isDept = node.getNodeType().equals(OrgNodeTypeEnum.DEPT.getValue());
        if (isDept) {
            deptId = node.getId();
            orgId = OrgTreeService.findDeptDirectOrg(node.getIdPath());
        }
        UserOrg userOrg = new UserOrg();
        userOrg.setUserId(userId);
        userOrg.setOrgId(orgId);
        userOrg.setDeptId(deptId);
        userOrg.setTenantId(UserContextUtils.getTenantId());
        userOrg.setCreateUser(UserContextUtils.getUserId());
        userOrg.setUpdateUser(UserContextUtils.getUserId());
        try {
            save(userOrg);
        } catch (DuplicateKeyException e) {
            throw new ParamError("该任职已存在");
        }
    }
}
