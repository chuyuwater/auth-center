package com.hbcy.authcenter.api.modules.core.app.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbcy.authcenter.api.modules.core.app.dao.AppMapper;
import com.hbcy.authcenter.api.modules.core.app.dao.TenantAppMapper;
import com.hbcy.authcenter.api.modules.core.app.dto.AppCardDTO;
import com.hbcy.authcenter.api.modules.core.app.model.App;
import com.hbcy.authcenter.api.modules.core.app.model.TenantApp;
import com.hbcy.authcenter.api.modules.core.app.vo.BindOrgTreeVO;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantMapper;
import com.hbcy.authcenter.api.modules.core.tenant.model.Tenant;
import com.hbcy.authcenter.api.modules.core.tenant.vo.TenantAppGrantStatusUpdateVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
    @Transactional(rollbackFor = Exception.class)
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
        String currentBinding = app.getBindingTenant();
        if (App.BINDING_PLACEHOLDER.equals(currentBinding)) {
            //单租户应用绑定租户
            app.setBindingTenant(tenantId);
            app.setUpdateUser(UserContextUtils.getUserId());
            appMapper.updateById(app);
        } else if (!"".equals(currentBinding) && !tenantId.equals(currentBinding)) {
            throw new ParamError("单租户应用已绑定其他租户");
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
            throw new ParamError("租户已授权该应用");
        }
    }

    /**
     * 切换授权状态
     */
    public void switchGrantStatus(TenantAppGrantStatusUpdateVO vo) {
        TenantApp binding = getById(vo.getBindingId());
        if (StringUtils.isBlank(binding.getAppId())) {
            throw new ParamError("授权关系不存在");
        }
        if (binding.getForbidden().equals(vo.getForbidden())) {
            return;
        }
        binding.setForbidden(vo.getForbidden());
        binding.setUpdateUser(UserContextUtils.getUserId());
        this.updateById(binding);
    }

    /**
     * 租户已授权的应用ID清单
     * 不含被禁用的
     *
     * @param tenantId 租户id
     * @return 应用id列表
     */
    public List<String> listBindAppIds(String tenantId) {
        return baseMapper.selectList(new QueryWrapper<TenantApp>()
                        .eq(TenantApp.COL_TENANT_ID, tenantId)
                        .eq(TenantApp.COL_FORBIDDEN, 0))
                .stream().map(TenantApp::getAppId).collect(Collectors.toList());
    }

    /**
     * 查看租户已授权的应用列表
     */
    public List<AppCardDTO> listGrantApps(String tenantId) {
        return baseMapper.listBindApps(tenantId);
    }

    /**
     * 租户为应用绑定组织树
     */
    public void bindingOrgTree(BindOrgTreeVO vo) {
        //TODO: 需要等用户、组织相关功能完成后再添加。注意组织树一旦绑定就不可以修改了
    }
}
