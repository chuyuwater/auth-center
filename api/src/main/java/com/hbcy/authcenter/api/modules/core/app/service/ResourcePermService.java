package com.hbcy.authcenter.api.modules.core.app.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.common.bean.EventDispatcher;
import com.hbcy.authcenter.api.modules.core.app.dao.ResourcePermMapper;
import com.hbcy.authcenter.api.modules.core.app.model.ResourcePerm;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourcePermCreateVO;
import com.hbcy.authcenter.api.modules.core.app.vo.ResourcePermQueryVO;
import com.hbcy.authcenter.api.modules.core.perm.dao.PermUnitResourceMapper;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnitResource;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantAppResourceMapper;
import com.hbcy.authcenter.api.modules.core.tenant.model.TenantAppResource;
import com.hbcy.authcenter.gateway.dto.EventResPermChanged;
import com.hbcy.authcenter.sdk.constants.EventConstants;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.util.BeanCopyUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 资源权限相关业务逻辑
 *
 * @author 姚泰然
 * @date 2025-12-24
 */
@Service
public class ResourcePermService extends ServiceImpl<ResourcePermMapper, ResourcePerm> {
    @Resource
    private TenantAppResourceMapper tenantAppResourceMapper;
    @Resource
    private PermUnitResourceMapper permUnitResourceMapper;
    @Resource
    private EventDispatcher eventDispatcher;


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
        delete(rp.getAppId(), Set.of(id));
    }

    /**
     * 删除资源权限点
     */
    public void delete(String appId, Collection<String> permIds) {
        baseMapper.deleteByIds(permIds);
        //删除租户应用最大授权
        tenantAppResourceMapper.delete(new QueryWrapper<TenantAppResource>()
                .eq(TenantAppResource.COL_APP_ID, appId)
                .in(TenantAppResource.COL_PERM_ID, permIds));
        //删除已有的角色/策略授权
        permUnitResourceMapper.delete(new QueryWrapper<PermUnitResource>()
                .in(PermUnitResource.COL_PERM_ID, permIds));
    }

    /**
     * 批量创建res关联的权限点
     *
     * @param resId    资源id
     * @param subPerms 权限点
     */
    public void batchCreate(String resId, List<ResourcePermCreateVO> subPerms) {
        List<ResourcePerm> toInsert = new ArrayList<>();
        for (ResourcePermCreateVO vo : subPerms) {
            ResourcePerm subPerm = new ResourcePerm();
            BeanCopyUtils.copy(vo, subPerm);
            subPerm.setId(UlidCreator.getUlid().toString());
            subPerm.setResId(resId);
            subPerm.setCreateUser(UserContextUtils.getUserId());
            subPerm.setUpdateUser(UserContextUtils.getUserId());
            toInsert.add(subPerm);
        }
        try {
            baseMapper.insert(toInsert);
        } catch (DuplicateKeyException e) {
            throw new ParamError("API路径和方法组合已存在");
        }
    }

    /**
     * 全量覆盖权限
     *
     * @param resId    资源id
     * @param subPerms 权限点
     */
    @Transactional(rollbackFor = Exception.class)
    public void overwrite(String resId, List<ResourcePermCreateVO> subPerms, String appId) {
        List<ResourcePerm> exists = baseMapper.selectList(new QueryWrapper<ResourcePerm>()
                .eq(ResourcePerm.COL_RES_ID, resId));
        Set<String> existsIds = exists.stream().map(ResourcePerm::getId).collect(Collectors.toSet());
        Set<String> subIds = subPerms.stream().map(ResourcePermCreateVO::getId).collect(Collectors.toSet());
        boolean isChanged = false;
        //计算出被删除的条目
        existsIds.removeAll(subIds);
        if (!existsIds.isEmpty()) {
            delete(resId, existsIds);
            isChanged = true;
        }
        //新增的条目
        List<ResourcePermCreateVO> toCreate = new ArrayList<>();
        for (ResourcePermCreateVO vo : subPerms) {
            if (StringUtils.isBlank(vo.getId())) {
                toCreate.add(vo);
            } else {
                ResourcePerm entity = getById(vo.getId());
                if (entity == null) {
                    toCreate.add(vo);
                } else {
                    if (!entity.getResId().equals(resId)) {
                        throw new ParamError("权限归属资源id错误，请刷新重试");
                    }
                    //确认更新
                    if (!vo.getPermCode().equals(entity.getPermCode())
                            || !vo.getApiMethod().equals(entity.getApiMethod())
                            || !vo.getApiPath().equals(entity.getApiPath())) {
                        entity.setPermCode(vo.getPermCode());
                        entity.setApiMethod(vo.getApiMethod());
                        entity.setApiPath(vo.getApiPath());
                        entity.setUpdateUser(UserContextUtils.getUserId());
                        //逐个更新（一般没几条）
                        updateById(entity);
                        isChanged = true;
                    }
                }
            }
        }
        if (!toCreate.isEmpty()) {
            batchCreate(resId, toCreate);
            isChanged = true;
        }
        if (isChanged) {
            //权限资源变更
            eventDispatcher.dispatch(
                    appId,
                    EventConstants.KAFKA_RESOURCE_PERM_CHANGED,
                    new EventResPermChanged().setAppId(appId).setResId(resId));
        }
    }
}
