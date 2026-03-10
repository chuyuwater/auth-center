package com.hbcy.authcenter.api.modules.core.perm.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbcy.authcenter.api.common.constants.G;
import com.hbcy.authcenter.api.common.enums.TreeQueryLevelEnum;
import com.hbcy.authcenter.api.modules.core.perm.dao.PermUnitGroupMapper;
import com.hbcy.authcenter.api.modules.core.perm.dao.PermUnitMapper;
import com.hbcy.authcenter.api.modules.core.perm.dao.PermUnitResourceMapper;
import com.hbcy.authcenter.api.modules.core.perm.dao.PermUnitUserMapper;
import com.hbcy.authcenter.api.modules.core.perm.dto.PermUnitAppDTO;
import com.hbcy.authcenter.api.modules.core.perm.dto.PermUnitDTO;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnit;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnitGroup;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnitResource;
import com.hbcy.authcenter.api.modules.core.perm.model.PermUnitUser;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitCreateVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitForbidVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitQueryVO;
import com.hbcy.authcenter.api.modules.core.perm.vo.PermUnitUpdateVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.error.PermissionError;
import com.hbcy.common.base.pojo.BatchDeleteVO;
import com.hbcy.common.base.pojo.PageResp;
import com.hbcy.common.base.util.BeanCopyUtils;
import com.hbcy.common.db.model.PageRespEx;
import com.hbcy.common.redis.RedisIdGenerator;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermUnitService extends ServiceImpl<PermUnitMapper, PermUnit> {
    public static final String BIZ_KEY = "portal:id:role:%s:";
    @Resource
    private RedisIdGenerator redisIdGenerator;
    @Resource
    private PermUnitUserMapper permUnitUserMapper;
    @Resource
    private PermUnitGroupMapper permUnitGroupMapper;
    @Resource
    private PermUnitResourceMapper permUnitResourceMapper;

    @Transactional(rollbackFor = Exception.class)
    public PermUnit create(PermUnitCreateVO vo) {
        String tenantId = UserContextUtils.getTenantId();
        PermUnit entity = new PermUnit();
        PermUnitGroup groupId = permUnitGroupMapper.selectById(vo.getBelongTo());
        if (groupId == null || !groupId.getTenantId().equals(tenantId)) {
            throw new ParamError("指定分组不存在");
        }
        BeanCopyUtils.copy(vo, entity);
        String id = redisIdGenerator.generateId(BIZ_KEY.formatted(tenantId),
                () -> baseMapper.selectCount(new QueryWrapper<PermUnit>()
                        .eq(PermUnit.COL_TENANT_ID, tenantId)),
                key -> PermUnit.ROLE_ID_TEMPLATE.formatted(tenantId, key));
        entity.setId(id);
        entity.setTenantId(tenantId);
        entity.setCreateUser(UserContextUtils.getUserId());
        entity.setUpdateUser(UserContextUtils.getUserId());
        try {
            save(entity);
        } catch (DuplicateKeyException e) {
            throw new ParamError("同一分组下名称不能重复");
        }
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public PermUnit update(PermUnitUpdateVO vo, String id) {
        PermUnit entity = getById(id);
        if (entity == null) {
            throw new ParamError("指定ID不存在");
        }
        String tenantId = UserContextUtils.getTenantId();
        if (!entity.getTenantId().equals(tenantId)) {
            throw new PermissionError();
        }
        if (!vo.getBelongTo().equals(entity.getBelongTo())) {
            PermUnitGroup groupId = permUnitGroupMapper.selectById(vo.getBelongTo());
            if (groupId == null || !groupId.getTenantId().equals(tenantId)) {
                throw new ParamError("指定分组不存在");
            }
        }
        BeanCopyUtils.copy(vo, entity);
        entity.setUpdateUser(UserContextUtils.getUserId());
        entity.setUpdateTime(LocalDateTime.now());
        try {
            updateById(entity);
        } catch (DuplicateKeyException e) {
            throw new ParamError("同一分组下名称不能重复");
        }
        return entity;
    }

    public PageResp<PermUnitDTO> list(PermUnitQueryVO vo) {
        Page<PermUnitDTO> dbPage = vo.getDbPage();
        String tenantId = UserContextUtils.getTenantId();
        vo.setTenantId(tenantId);
        if (StringUtils.isNotBlank(vo.getBelongTo()) && vo.getLevel() > 0) {
            PermUnitGroup permUnitGroup = permUnitGroupMapper.selectById(vo.getBelongTo());
            if (permUnitGroup == null) return new PageResp<>();
            if (Objects.equals(vo.getLevel(), TreeQueryLevelEnum.CHILD.getCode())) {
                vo.setGroupIdPath(permUnitGroup.getIdPath() + G.ID_PATH_SPLITTER);
            } else {
                vo.setGroupIdPath(permUnitGroup.getIdPath());
            }
            vo.setBelongTo(null);
        }
        Page<PermUnitDTO> page = baseMapper.listPermUnit(dbPage, vo);
        if (!page.getRecords().isEmpty()) {
            List<String> list = page.getRecords().stream().map(PermUnitDTO::getId).toList();
            List<PermUnitAppDTO> unitApps = permUnitResourceMapper.selectUnitApp(list);
            Map<String, Set<String>> appNames = unitApps.stream()
                    .collect(Collectors.groupingBy(
                            PermUnitAppDTO::getUnitId,
                            Collectors.mapping(PermUnitAppDTO::getAppName, Collectors.toSet())
                    ));
            page.getRecords().forEach(dto -> dto.setAppNames(appNames.get(dto.getId())));
        }
        return new PageRespEx<>(page);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        BatchDeleteVO vo = new BatchDeleteVO();
        vo.setIds(List.of(id));
        batchDelete(vo);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(BatchDeleteVO vo) {
        batchDelete(vo);
    }

    private void batchDelete(BatchDeleteVO vo) {
        String tenantId = UserContextUtils.getTenantId();
        List<PermUnit> permUnits = baseMapper.selectByIds(vo.getIds());
        if (permUnits.isEmpty()) {
            return;
        }
        for (PermUnit permUnit : permUnits) {
            if (permUnit.getForbidden().equals(0)) {
                throw new ParamError("删除前请先禁用");
            }
            if (!permUnit.getTenantId().equals(tenantId)) {
                throw new PermissionError();
            }
        }
        permUnitUserMapper.delete(new QueryWrapper<PermUnitUser>()
                .in(PermUnitUser.COL_UNIT_ID, vo.getIds()));
        permUnitResourceMapper.delete(new QueryWrapper<PermUnitResource>()
                .in(PermUnitResource.COL_UNIT_ID, vo.getIds()));
        baseMapper.update(new UpdateWrapper<PermUnit>()
                .in(PermUnit.COL_ID, vo.getIds())
                .set(PermUnit.COL_UPDATE_USER, UserContextUtils.getUserId())
                .set(PermUnit.COL_DELETE_TIME, System.currentTimeMillis()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void forbid(@Valid PermUnitForbidVO vo) {
        PermUnit pu = baseMapper.selectById(vo.getUnitId());
        if (pu == null) {
            throw new ParamError("权限单元不存在");
        }
        if (pu.getForbidden().equals(vo.getForbidden())) {
            return;
        }
        PermUnit toUpdate = new PermUnit();
        toUpdate.setId(vo.getUnitId());
        toUpdate.setForbidden(vo.getForbidden());
        toUpdate.setUpdateUser(UserContextUtils.getUserId());
        baseMapper.updateById(toUpdate);
        //级联更新授权用户，避免查询的时候join表过多
        permUnitUserMapper.update(new UpdateWrapper<PermUnitUser>()
                .eq(PermUnitUser.COL_UNIT_ID, vo.getUnitId())
                .set(PermUnitUser.COL_FORBIDDEN, vo.getForbidden()));
    }
}
