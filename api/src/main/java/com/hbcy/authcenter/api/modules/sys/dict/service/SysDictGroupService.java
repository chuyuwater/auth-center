package com.hbcy.authcenter.api.modules.sys.dict.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.f4b6a3.ulid.UlidCreator;
import com.hbcy.authcenter.api.modules.core.app.dao.AppMapper;
import com.hbcy.authcenter.api.modules.core.app.model.App;
import com.hbcy.authcenter.api.modules.sys.dict.dao.SysDictMapper;
import com.hbcy.authcenter.api.modules.sys.dict.model.SysDict;
import com.hbcy.authcenter.api.modules.sys.dict.vo.DictGroupCreateVO;
import com.hbcy.authcenter.api.modules.sys.dict.vo.DictGroupUpdateVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.util.BeanCopyUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2026-01-15 09:05
 */
@Service
public class SysDictGroupService {
    @Resource
    private SysDictMapper sysDictMapper;
    @Resource
    private AppMapper appMapper;

    public SysDict createGroup(DictGroupCreateVO vo) {
        if (StringUtils.isNotBlank(vo.getAppId())) {
            // 不为空的时候校验，为空字符串表示"通用类"字典，如行政区划，性别等
            App app = appMapper.selectById(vo.getAppId());
            if (app == null) {
                throw new ParamError("app不存在");
            }
        }
        SysDict dict = new SysDict();
        BeanCopyUtils.copy(vo, dict);
        //分组的featCode为空字符串
        dict.setId(UlidCreator.getUlid().toString());
        dict.setFeatCode("");
        dict.setParentId("");
        dict.setIdPath(dict.getId());
        dict.setCreateUser(UserContextUtils.getUserId());
        try {
            sysDictMapper.insert(dict);
        } catch (DuplicateKeyException e) {
            throw new ParamError("字典类型编码或中文名已存在");
        }
        return dict;
    }

    @Transactional(rollbackFor = Exception.class)
    public SysDict updateGroup(String id, DictGroupUpdateVO vo) {
        SysDict dict = sysDictMapper.selectById(id);
        if (dict == null) {
            throw new ParamError("字典类型不存在");
        }
        String oldValueStr = dict.getValueStr();
        BeanCopyUtils.copy(vo, dict);
        dict.setUpdateUser(UserContextUtils.getUserId());
        try {
            sysDictMapper.updateById(dict);
        } catch (DuplicateKeyException e) {
            throw new ParamError("字典类型编码或中文名已存在");
        }
        //如果valueStr改了（编码变化），下级子节点的featCode也要改
        if (!oldValueStr.equals(vo.getValueStr())) {
            sysDictMapper.update(new UpdateWrapper<SysDict>()
                    .eq(SysDict.COL_PARENT_ID, id)
                    .set(SysDict.COL_FEAT_CODE, vo.getValueStr()));
        }
        return dict;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(String id) {
        SysDict dict = sysDictMapper.selectById(id);
        if (dict == null) {
            return;
        }
        String updater = UserContextUtils.getUserId();
        Long deleteTime = System.currentTimeMillis();
        sysDictMapper.update(new UpdateWrapper<SysDict>()
                .likeRight(SysDict.COL_ID_PATH, dict.getId())
                .set(SysDict.COL_DELETE_TIME, deleteTime)
                .set(SysDict.COL_UPDATE_USER, updater));
    }

    public List<SysDict> queryGroups(String appId) {
        return sysDictMapper.selectList(new QueryWrapper<SysDict>()
                .eq(StringUtils.isNotBlank(appId), SysDict.COL_APP_ID, appId)
                .eq(SysDict.COL_PARENT_ID, ""));
    }
}
