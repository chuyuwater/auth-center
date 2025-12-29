package com.hbcy.authcenter.api.modules.core.app.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbcy.authcenter.api.modules.core.app.dao.ResourcePermMapper;
import com.hbcy.authcenter.api.modules.core.app.dao.ResourceTreeMapper;
import com.hbcy.authcenter.api.modules.core.app.model.ResourcePerm;
import com.hbcy.authcenter.api.modules.core.app.model.ResourceTree;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourcePermCreateVO;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourcePermQueryVO;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourcePermUpdateVO;
import com.hbcy.authcenter.api.modules.core.perm.dao.PermUnitResourceMapper;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnitResource;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantAppResourceMapper;
import com.hbcy.authcenter.api.modules.core.tenant.model.TenantAppResource;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.util.BeanCopyUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 资源权限相关业务逻辑
 *
 * @author 姚泰然
 * @date 2025-12-24
 */
@Service
public class ResourcePermService extends ServiceImpl<ResourcePermMapper, ResourcePerm> {
    @Resource
    private ResourceTreeMapper resourceTreeMapper;
    @Resource
    private TenantAppResourceMapper tenantAppResourceMapper;
    @Resource
    private PermUnitResourceMapper permUnitResourceMapper;
    @Resource
    private ResourcePermService self;

    /**
     * 创建资源权限点
     *
     * @param vo 权限信息
     * @return 创建后的权限信息
     */
    public ResourcePerm create(ResourcePermCreateVO vo) {
        ResourceTree tree = resourceTreeMapper.selectById(vo.getResId());
        if (tree == null) {
            throw new ParamError("关联的菜单资源不存在");
        }
        ResourcePerm entity = new ResourcePerm();
        BeanCopyUtils.copy(vo, entity);
        entity.setAppId(tree.getAppId());
        entity.setCreateUser(UserContextUtils.getUserId());
        entity.setUpdateUser(UserContextUtils.getUserId());
        try {
            save(entity);
        } catch (DuplicateKeyException e) {
            throw new ParamError("API路径和方法组合已存在");
        }
        return entity;
    }

    /**
     * 更新资源权限点
     *
     * @param vo 更新信息
     * @param id 权限ID
     * @return 更新后的权限信息
     */
    public ResourcePerm update(ResourcePermUpdateVO vo, String id) {
        ResourcePerm entity = getById(id);
        if (entity == null) {
            throw new ParamError("指定权限不存在");
        }
        BeanCopyUtils.copy(vo, entity);
        entity.setUpdateUser(UserContextUtils.getUserId());
        updateById(entity);
        return entity;
    }

    /**
     * 查询资源权限列表
     *
     * @param vo 查询条件
     * @return 权限列表
     */
    public List<ResourcePerm> list(ResourcePermQueryVO vo) {
        return baseMapper.selectList(new QueryWrapper<ResourcePerm>()
                .likeRight(StringUtils.isNotBlank(vo.getPermCode()), ResourcePerm.COL_PERM_CODE, vo.getPermCode())
                .eq(StringUtils.isNotBlank(vo.getResId()), ResourcePerm.COL_RES_ID, vo.getResId())
                .eq(StringUtils.isNotBlank(vo.getAppId()), ResourcePerm.COL_APP_ID, vo.getAppId())
                .orderByAsc(ResourcePerm.COL_API_METHOD));
    }

    public Set<String> filterAppPermIds(String appId, Set<String> supplyIds) {
        return baseMapper.filterAppPermIds(appId, supplyIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        ResourcePerm rp = getById(id);
        if (rp == null) {
            return;
        }
        self.delete(rp.getAppId(), Set.of(id));
    }

    /**
     * 删除资源权限点
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String appId, Collection<String> permIds) {
        baseMapper.deleteByIds(permIds);
        //删除应用最大授权
        tenantAppResourceMapper.delete(new QueryWrapper<TenantAppResource>()
                .eq(TenantAppResource.COL_APP_ID, appId)
                .in(TenantAppResource.COL_PERM_ID, permIds));
        //删除已有的角色/策略授权
        permUnitResourceMapper.delete(new QueryWrapper<PermUnitResource>()
                .in(PermUnitResource.COL_PERM_ID, permIds));
    }
}
