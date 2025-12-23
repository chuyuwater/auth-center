package com.hbcy.authcenter.api.modules.core.app.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbcy.authcenter.api.modules.core.app.dao.AppMapper;
import com.hbcy.authcenter.api.modules.core.app.dao.TenantAppMapper;
import com.hbcy.authcenter.api.modules.core.app.model.App;
import com.hbcy.authcenter.api.modules.core.app.model.TenantApp;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantMapper;
import com.hbcy.authcenter.api.modules.core.tenant.model.Tenant;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantAppBindStatusUpdateVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 租户绑定应用
 *
 * @author 姚泰然
 * @date 2025-12-23 17:24
 */
@Service
public class TenantAppService extends ServiceImpl<TenantAppMapper, TenantApp> {
    @Resource
    private AppMapper appMapper;
    @Resource
    private TenantMapper tenantMapper;

    /**
     * 创建租户绑定应用
     *
     * @param tenantId 租户id
     * @param appId    应用id
     */
    public void createBinding(String tenantId, String appId) {
        App app = appMapper.selectById(appId);
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (app == null || tenant == null) {
            throw new ParamError("租户或应用不存在");
        }
        if (app.getForbidden() == 1) {
            throw new ParamError("应用已被禁用");
        }
        if (tenant.getForbidden() == 1) {
            throw new ParamError("租户已被禁用");
        }
        if (StringUtils.isNotBlank(app.getBindingTenant()) && !app.getBindingTenant().equals(tenantId)) {
            throw new ParamError("应用已绑定其他租户，不可修改");
        }
        TenantApp tenantApp = new TenantApp();
        tenantApp.setTenantId(tenantId);
        tenantApp.setAppId(appId);
        tenantApp.setForbidden(0);
        tenantApp.setCreateUser(UserContextUtils.getUserId());
        tenantApp.setUpdateUser(UserContextUtils.getUserId());
        try {
            save(tenantApp);
        } catch (DuplicateKeyException e) {
            throw new ParamError("租户已绑定该应用");
        }
    }

    /**
     * 切换绑定状态
     */
    public void switchBindingStatus(TenantAppBindStatusUpdateVO vo) {
        TenantApp binding = getById(vo.getBindingId());
        if (StringUtils.isBlank(binding.getAppId())) {
            throw new ParamError("绑定关系不存在");
        }
        if (binding.getForbidden().equals(vo.getForbidden())) {
            return;
        }
        binding.setForbidden(vo.getForbidden());
        binding.setUpdateUser(UserContextUtils.getUserId());
        this.updateById(binding);
    }
}
