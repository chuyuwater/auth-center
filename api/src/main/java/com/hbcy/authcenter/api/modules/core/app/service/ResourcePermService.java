package com.hbcy.authcenter.api.modules.core.app.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import com.google.common.base.Splitter;
import com.hbcy.authcenter.api.common.bean.EventDispatcher;
import com.hbcy.authcenter.api.modules.core.app.dao.ResourcePermApiMapper;
import com.hbcy.authcenter.api.modules.core.app.dao.ResourcePermMapper;
import com.hbcy.authcenter.api.modules.core.app.model.ResourcePerm;
import com.hbcy.authcenter.api.modules.core.app.model.ResourcePermApi;
import com.hbcy.authcenter.api.modules.core.app.vo.*;
import com.hbcy.authcenter.api.modules.core.perm.dao.PermUnitResourceMapper;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnitResource;
import com.hbcy.authcenter.api.modules.core.tenant.dao.TenantAppResourceMapper;
import com.hbcy.authcenter.api.modules.core.tenant.model.TenantAppResource;
import com.hbcy.authcenter.gateway.dto.EventResPermChanged;
import com.hbcy.authcenter.global.constants.EventConstants;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.util.BeanCopyUtils;
import com.hbcy.common.db.utils.DbExceptionParser;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
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
    @Resource
    private ResourcePermApiMapper resourcePermApiMapper;
    @Resource
    private SqlSessionTemplate sqlSessionTemplate;

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
                .orderByAsc(ResourcePerm.COL_ID));
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
        // 删除关联的api配置
        resourcePermApiMapper.deleteByPermIds(permIds);
        // 删除租户应用最大授权
        tenantAppResourceMapper.delete(new QueryWrapper<TenantAppResource>()
                .eq(TenantAppResource.COL_APP_ID, appId)
                .in(TenantAppResource.COL_PERM_ID, permIds));
        // 删除已有的角色/策略授权
        permUnitResourceMapper.delete(new QueryWrapper<PermUnitResource>()
                .in(PermUnitResource.COL_PERM_ID, permIds));
    }

    /**
     * 校验单个API配置项
     */
    private void checkApiItem(ResourcePermApiVO apiVO) {
        String apiPath = apiVO.getApiPath();
        if (StringUtils.isBlank(apiPath)) {
            throw new ParamError("API路径不能为空");
        }
        List<String> parts = Splitter.on("/").splitToList(apiPath);
        if (parts.size() < 3) {
            throw new ParamError("API路径过短");
        }
        int firstIndex = apiPath.indexOf("**");
        if (firstIndex >= 0) {
            if (!"**".equals(parts.get(parts.size() - 1)) || firstIndex != apiPath.length() - 2) {
                throw new ParamError("API路径中，**只能放在末尾");
            }
        }
    }

    /**
     * 校验权限VO中的APIs列表
     */
    public void checkPerm(ResourcePermUpdateVO vo) {
        if (CollectionUtils.isEmpty(vo.getApis())) {
            return;
        }
        for (ResourcePermApiVO apiVO : vo.getApis()) {
            checkApiItem(apiVO);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ResourcePerm update(String id, ResourcePermUpdateVO vo) {
        checkPerm(vo);
        ResourcePerm rp = baseMapper.selectById(id);
        if (rp == null) {
            throw new ParamError("指定ID不存在");
        }
        doUpdate(vo, id);
        return rp;
    }

    public void doUpdate(ResourcePermUpdateVO vo, String id) {
        try {
            baseMapper.update(new UpdateWrapper<ResourcePerm>()
                    .eq(ResourcePerm.COL_ID, id)
                    .set(ResourcePerm.COL_PERM_NAME, vo.getPermName())
                    .set(ResourcePerm.COL_PERM_CODE, vo.getPermCode())
                    .set(ResourcePerm.COL_UPDATE_USER, UserContextUtils.getUserId())
                    .set(ResourcePerm.COL_UPDATE_TIME, LocalDateTime.now()));
        } catch (DuplicateKeyException e) {
            throw new ParamError("权限码已存在");
        }
        // 覆盖写入api配置：先删除旧记录，再插入新记录
        resourcePermApiMapper.deleteByPermIds(List.of(id));
        List<ResourcePermApiVO> apis = vo.getApis();
        if (!CollectionUtils.isEmpty(apis)) {
            insertPermApis(List.of(new PermAPiBatchCreateVO(id, apis)));
        }
    }

    /**
     * 批量创建res关联的权限点
     *
     * @param appId    应用id
     * @param resId    资源id
     * @param subPerms 权限点
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchCreate(String appId, String resId, List<ResourcePermCreateVO> subPerms) {
        if (CollectionUtils.isEmpty(subPerms)) {
            return;
        }
        List<ResourcePerm> toInsert = new ArrayList<>();
        for (ResourcePermCreateVO vo : subPerms) {
            checkPerm(vo);
            ResourcePerm subPerm = new ResourcePerm();
            BeanCopyUtils.copy(vo, subPerm);
            subPerm.setId(UlidCreator.getUlid().toString());
            subPerm.setResId(resId);
            subPerm.setAppId(appId);
            subPerm.setCreateUser(UserContextUtils.getUserId());
            subPerm.setUpdateUser(UserContextUtils.getUserId());
            toInsert.add(subPerm);
        }
        try {
            baseMapper.insert(toInsert);
        } catch (DuplicateKeyException e) {
            throw new ParamError("权限码已存在");
        } catch (MybatisPlusException e) {
            DbExceptionParser.checkDup(e, "权限码已存在");
        }
        // 批量插入api配置
        List<PermAPiBatchCreateVO> batchCreateVOS = new ArrayList<>();
        for (int i = 0; i < subPerms.size(); i++) {
            ResourcePermCreateVO vo = subPerms.get(i);
            String permId = toInsert.get(i).getId();
            if (!CollectionUtils.isEmpty(vo.getApis())) {
                batchCreateVOS.add(new PermAPiBatchCreateVO(permId, vo.getApis()));
            }
        }
        if (!batchCreateVOS.isEmpty()) {
            insertPermApis(batchCreateVOS);
        }
        // 权限资源变更
        eventDispatcher.dispatch(
                appId,
                EventConstants.RESOURCE_PERM_CHANGED,
                new EventResPermChanged().setAppId(appId).setResId(resId));
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
        Set<String> subIds = new HashSet<>();
        if (subPerms == null) {
            subPerms = new ArrayList<>();
        }
        if (!subPerms.isEmpty()) {
            subIds = subPerms.stream()
                    .map(ResourcePermCreateVO::getId)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
        }
        boolean isChanged = false;
        // 计算出被删除的条目
        existsIds.removeAll(subIds);
        if (!existsIds.isEmpty()) {
            delete(appId, existsIds);
            isChanged = true;
        }
        // 新增的条目
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
                    // 比对apis列表是否变化
                    boolean apisChange = isApisChanged(vo.getId(), vo.getApis());
                    if (apisChange || !vo.getPermName().equals(entity.getPermName())
                            || !vo.getPermCode().equals(entity.getPermCode())) {
                        // 逐个更新（一般没几条）
                        doUpdate(vo, vo.getId());
                        if (apisChange) {
                            // 只有api变化影响网关
                            isChanged = true;
                        }
                    }
                }
            }
        }
        if (!toCreate.isEmpty()) {
            batchCreate(appId, resId, toCreate);
            isChanged = true;
        }
        if (isChanged) {
            // 权限资源变更
            eventDispatcher.dispatch(
                    appId,
                    EventConstants.RESOURCE_PERM_CHANGED,
                    new EventResPermChanged().setAppId(appId).setResId(resId));
        }
    }

    /**
     * 比对提交的apis列表与数据库中现有的apis是否一致
     */
    private boolean isApisChanged(String permId, List<ResourcePermApiVO> newApis) {
        List<ResourcePermApi> existApis = resourcePermApiMapper.selectList(
                new QueryWrapper<ResourcePermApi>().eq(ResourcePermApi.COL_PERM_ID, permId));
        int newSize = CollectionUtils.isEmpty(newApis) ? 0 : newApis.size();
        if (existApis.size() != newSize) {
            return true;
        }
        if (newSize == 0) {
            return false;
        }
        // 用 method+path 的组合做集合比对
        Set<String> existSet = existApis.stream()
                .map(a -> a.getApiMethod() + ":" + a.getApiPath())
                .collect(Collectors.toSet());
        for (ResourcePermApiVO vo : newApis) {
            if (!existSet.contains(vo.getApiMethod() + ":" + vo.getApiPath())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将api配置批量插入到resource_perm_api表
     */
    private void insertPermApis(List<PermAPiBatchCreateVO> vo) {
        List<ResourcePermApi> toInsert = new ArrayList<>();
        String userId = UserContextUtils.getUserId();
        for (PermAPiBatchCreateVO createVO : vo) {
            String permId = createVO.getPermId();
            int i = 0;
            for (ResourcePermApiVO apiVO : createVO.getApis()) {
                ResourcePermApi api = new ResourcePermApi();
                api.setId(UlidCreator.getUlid().toString());
                api.setPermId(permId);
                api.setApiMethod(apiVO.getApiMethod());
                api.setApiPath(apiVO.getApiPath());
                api.setShowOrder(i++);
                api.setCreateUser(userId);
                toInsert.add(api);
            }
        }
        try {
            resourcePermApiMapper.insert(toInsert);
        } catch (MybatisPlusException e) {
            DbExceptionParser.checkDup(e, "api方法+路径的组合必须全局唯一");
        } catch (DuplicateKeyException e) {
            throw new ParamError("api方法+路径的组合必须全局唯一");
        }
    }
}
