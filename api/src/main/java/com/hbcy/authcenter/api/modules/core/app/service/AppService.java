package com.hbcy.authcenter.api.modules.core.app.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbcy.authcenter.api.common.bean.NodeMoveVO;
import com.hbcy.authcenter.api.modules.core.app.dao.AppMapper;
import com.hbcy.authcenter.api.modules.core.app.model.App;
import com.hbcy.authcenter.api.modules.core.app.vo.AppCreateVO;
import com.hbcy.authcenter.api.modules.core.app.vo.AppForbiddenVO;
import com.hbcy.authcenter.api.modules.core.app.vo.AppQueryVO;
import com.hbcy.authcenter.api.modules.core.app.vo.AppUpdateVO;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantAppMapper;
import com.hbcy.authcenter.api.modules.core.tenant.model.TenantApp;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.util.BeanCopyUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 应用相关业务逻辑
 *
 * @author 姚泰然
 * @date 2025-12-23
 */
@Service
public class AppService extends ServiceImpl<AppMapper, App> {
    @Resource
    private TenantAppMapper tenantAppMapper;

    private void checkNameExist(String nameCn) {
        App one = this.getOne(new QueryWrapper<App>().eq(App.COL_NAME_CN, nameCn), false);
        if (one != null) {
            throw new ParamError("应用名称已存在");
        }
    }

    public App create(AppCreateVO vo) {
        if (getById(vo.getId()) != null) {
            throw new ParamError("应用编号已存在");
        }
        checkNameExist(vo.getNameCn());

        App app = new App();
        BeanCopyUtils.copy(vo, app);
        if (vo.isMultiTenancy()) {
            app.setBindingTenant("");
        } else {
            app.setBindingTenant(App.BINDING_PLACEHOLDER);
        }
        app.setCreateUser(UserContextUtils.getUserId());
        app.setUpdateUser(UserContextUtils.getUserId());
        try {
            baseMapper.append(app);
        } catch (DuplicateKeyException e) {
            throw new ParamError("应用id不能重复（含被删除的应用）");
        }
        return app;
    }

    public App update(AppUpdateVO vo, String id) {
        App app = getById(id);
        if (app == null) {
            throw new ParamError("指定应用不存在");
        }
        // If name is changed, check uniqueness
        if (!Strings.CS.equals(app.getNameCn(), vo.getNameCn())) {
            checkNameExist(vo.getNameCn());
        }

        BeanCopyUtils.copy(vo, app);
        app.setUpdateTime(LocalDateTime.now());
        app.setUpdateUser(UserContextUtils.getUserId());
        try {
            this.updateById(app);
        } catch (DuplicateKeyException e) {
            throw new ParamError("应用名称重复");
        }
        return app;
    }

    @Transactional(rollbackFor = Exception.class)
    public void switchStatus(AppForbiddenVO vo) {
        App app = getById(vo.getAppId());
        if (app == null) {
            throw new ParamError("指定应用不存在");
        }
        if (app.getForbidden() != null && app.getForbidden().intValue() == vo.getForbidden()) {
            return;
        }
        App toUpdate = new App();
        toUpdate.setId(vo.getAppId());
        toUpdate.setForbidden(vo.getForbidden());
        toUpdate.setUpdateUser(UserContextUtils.getUserId());
        this.updateById(toUpdate);
        //对应应用授权状态级联变化
        tenantAppMapper.switchAppStatus(
                vo.getForbidden(), vo.getAppId(), UserContextUtils.getTenantId());
    }

    public List<App> list(AppQueryVO vo) {
        return baseMapper.selectList(new QueryWrapper<App>()
                .eq(vo.getForbidden() != null, App.COL_FORBIDDEN, vo.getForbidden())
                .and(StringUtils.isNotBlank(vo.getKeyword()),
                        qw -> qw.like(App.COL_NAME_CN, vo.getKeyword())
                                .or()
                                .like(App.COL_ID, vo.getKeyword()))

                .orderByAsc(App.COL_SHOW_ORDER));
    }

    @Transactional(rollbackFor = Exception.class)
    public void move(NodeMoveVO vo) {
        vo.check();
        App node = baseMapper.selectById(vo.getNodeId());
        if (node == null) {
            throw new ParamError("指定应用已被删除");
        }
        int targetIdx = 0;
        if (StringUtils.isNotBlank(vo.getPrevId())) {
            App app = baseMapper.selectById(vo.getPrevId());
            if (app == null) {
                throw new ParamError("前置节点已被删除，请刷新重试");
            }
            targetIdx = app.getShowOrder() + 1;
        }
        baseMapper.move(targetIdx);
        App toUpdate = new App();
        toUpdate.setId(vo.getNodeId());
        toUpdate.setShowOrder(targetIdx);
        updateById(toUpdate);
    }

    public void delete(String id) {
        App app = getById(id);
        if (app == null) {
            return;
        }
        if (tenantAppMapper.exists(new QueryWrapper<TenantApp>().eq(TenantApp.COL_APP_ID, id))) {
            throw new ParamError("该应用下已存在关联配置，请先解除所有关联后再执行删除操作！");
        }
        app.setUpdateUser(UserContextUtils.getUserId());
        baseMapper.update(new UpdateWrapper<App>()
                .eq(App.COL_ID, id)
                .set(App.COL_UPDATE_USER, UserContextUtils.getUserId())
                .set(App.COL_DELETE_TIME, System.currentTimeMillis())
                .set(App.COL_UPDATE_TIME, LocalDateTime.now()));
    }
}
