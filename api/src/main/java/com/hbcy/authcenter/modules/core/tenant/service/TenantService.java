package com.hbcy.authcenter.modules.core.tenant.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbcy.authcenter.modules.core.tenant.dao.TenantMapper;
import com.hbcy.authcenter.modules.core.tenant.model.Tenant;
import com.hbcy.authcenter.modules.core.tenant.utils.TenantIdUtils;
import com.hbcy.authcenter.modules.core.tenant.vo.TenantForbiddenVO;
import com.hbcy.authcenter.modules.core.tenant.vo.TenantQueryVO;
import com.hbcy.authcenter.modules.core.tenant.vo.TenantUpsertVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.db.model.PageResp;
import com.hbcy.common.redis.RedisIdGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * @author 姚泰然
 * @date 2025-12-23 14:38
 */
@Service
public class TenantService extends ServiceImpl<TenantMapper, Tenant> {

    public static final String TENANT_KEY = "portal:tenant:";
    @Resource
    private RedisIdGenerator redisIdGenerator;

    private void checkExist(String nameCn) {
        Tenant one = this.getOne(new QueryWrapper<Tenant>().eq(Tenant.COL_NAME_CN, nameCn), false);
        if (one != null) {
            throw new ParamError("租户名称已存在");
        }
    }

    public Tenant create(TenantUpsertVO vo) {
        checkExist(vo.getNameCn());
        String calcedId = redisIdGenerator.generateId(TENANT_KEY, this::count, TenantIdUtils::convertToTitle);
        Tenant tenant = new Tenant();
        tenant.setId(calcedId);
        BeanUtils.copyProperties(vo, tenant);
        tenant.setCreateUser(UserContextUtils.getUserId());
        tenant.setUpdateUser(UserContextUtils.getUserId());
        try {
            this.save(tenant);
        } catch (DuplicateKeyException e) {
            throw new ParamError("请重试");
        }
        return tenant;
    }

    public Tenant update(TenantUpsertVO vo, String id) {
        Tenant tenant = getById(id);
        if (tenant == null) {
            throw new ParamError("指定租户不存在");
        }
        BeanUtils.copyProperties(vo, tenant);
        tenant.setUpdateUser(UserContextUtils.getUserId());
        try {
            this.updateById(tenant);
        } catch (DuplicateKeyException e) {
            throw new ParamError("租户名称重复");
        }
        return tenant;
    }

    public void switchStatus(TenantForbiddenVO vo) {
        Tenant tenant = getById(vo.getTenantId());
        if (tenant == null) {
            throw new ParamError("指定租户不存在");
        }
        if (tenant.getForbidden().equals(vo.getForbidden())) {
            return;
        }
        Tenant toUpdate = new Tenant();
        toUpdate.setId(vo.getTenantId());
        toUpdate.setForbidden(vo.getForbidden());
        toUpdate.setUpdateUser(UserContextUtils.getUserId());
        this.updateById(tenant);
    }

    public PageResp<Tenant> list(TenantQueryVO vo) {
        Page<Tenant> dbPage = vo.getDbPage();
        Page<Tenant> resp = baseMapper.selectPage(dbPage, new QueryWrapper<Tenant>()
                .eq(vo.getForbidden() != null, Tenant.COL_FORBIDDEN, vo.getForbidden())
                .like(StringUtils.isNotBlank(vo.getNameCn()), Tenant.COL_NAME_CN, vo.getNameCn()));
        return new PageResp<>(resp);
    }

    public void delete(String id) {
        Tenant tenant = getById(id);
        if (tenant == null) {
            return;
        }
        tenant.setUpdateUser(UserContextUtils.getUserId());
        //逻辑删除
        this.removeById(tenant);
    }
}
