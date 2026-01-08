package com.hbcy.authcenter.api.modules.core.tenant.service;

import com.hbcy.authcenter.api.common.bean.EventDispatcher;
import com.hbcy.authcenter.api.modules.core.tenant.model.Tenant;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantAdminUpdateVO;
import com.hbcy.authcenter.api.modules.core.user.model.User;
import com.hbcy.authcenter.api.modules.core.user.service.UserService;
import com.hbcy.authcenter.global.constants.EventConstants;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @author 姚泰然
 * @date 2025-12-31 14:31
 */
@Service
public class TenantMaintainService {
    @Resource
    private TenantService tenantService;
    @Resource
    private UserService userService;
    @Resource
    private EventDispatcher eventDispatcher;

    /**
     * 修改默认管理员信息
     * 供租户使用
     *
     * @param vo 新的管理员
     */
    public void updateDefaultAdmin(TenantAdminUpdateVO vo) {
        User user = userService.getById(vo.getUserId());
        String tenantId = UserContextUtils.getTenantId();
        if (!user.getTenantId().equals(tenantId)) {
            throw new ParamError("用户不属于当前租户");
        }
        Tenant tenant = tenantService.getById(tenantId);
        if (tenant.getAdminId().equals(vo.getUserId())) {
            return;
        }
        Tenant toUpdate = new Tenant();
        toUpdate.setId(tenantId);
        toUpdate.setAdminId(user.getId());
        toUpdate.setContactPhone(user.getPhone());
        toUpdate.setContactUser(user.getRealName());
        toUpdate.setUpdateUser(UserContextUtils.getUserId());
        tenantService.save(toUpdate);
        eventDispatcher.dispatch(
                tenantId,
                EventConstants.TENANT_ADMIN_CHANGED,
                toUpdate
        );
    }

}
