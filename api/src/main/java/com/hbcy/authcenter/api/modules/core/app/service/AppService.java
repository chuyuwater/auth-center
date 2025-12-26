package com.hbcy.authcenter.api.modules.core.app.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbcy.authcenter.api.modules.core.app.dao.AppMapper;
import com.hbcy.authcenter.api.modules.core.app.model.App;
import com.hbcy.authcenter.api.modules.core.app.vo.AppCreateVO;
import com.hbcy.authcenter.api.modules.core.app.vo.AppForbiddenVO;
import com.hbcy.authcenter.api.modules.core.app.vo.AppQueryVO;
import com.hbcy.authcenter.api.modules.core.app.vo.AppUpdateVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.pojo.PageResp;
import com.hbcy.common.base.util.BeanCopyUtils;
import com.hbcy.common.db.model.PageRespEx;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 应用相关业务逻辑
 *
 * @author 姚泰然
 * @date 2025-12-23
 */
@Service
public class AppService extends ServiceImpl<AppMapper, App> {

    private void checkNameExist(String nameCn) {
        App one = this.getOne(new QueryWrapper<App>().eq(App.COL_NAME_CN, nameCn), false);
        if (one != null) {
            throw new ParamError("应用名称已存在");
        }
    }

    public App create(AppCreateVO vo) {
        if (getById(vo.getId()) != null) {
            throw new ParamError("应用ID已存在");
        }
        checkNameExist(vo.getNameCn());

        App app = new App();
        BeanCopyUtils.copy(vo, app);
        app.setCreateUser(UserContextUtils.getUserId());
        app.setUpdateUser(UserContextUtils.getUserId());
        try {
            this.save(app);
        } catch (DuplicateKeyException e) {
            throw new ParamError("请重试");
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
        app.setUpdateUser(UserContextUtils.getUserId());
        try {
            this.updateById(app);
        } catch (DuplicateKeyException e) {
            throw new ParamError("应用名称重复");
        }
        return app;
    }

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
    }

    public PageResp<App> list(AppQueryVO vo) {
        Page<App> dbPage = vo.getDbPage();
        Page<App> resp = baseMapper.selectPage(dbPage, new QueryWrapper<App>()
                .eq(vo.getForbidden() != null, App.COL_FORBIDDEN, vo.getForbidden())
                .like(StringUtils.isNotBlank(vo.getNameCn()), App.COL_NAME_CN, vo.getNameCn())
                .orderByAsc(App.COL_SHOW_ORDER));
        return new PageRespEx<>(resp);
    }

    public void delete(String id) {
        App app = getById(id);
        if (app == null) {
            return;
        }
        app.setUpdateUser(UserContextUtils.getUserId());
        // Logic delete
        this.removeById(app);
    }
}
