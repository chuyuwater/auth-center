package com.hbcy.authcenter.api.modules.sys.dict.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hbcy.authcenter.api.modules.core.app.dao.AppMapper;
import com.hbcy.authcenter.api.modules.core.app.model.App;
import com.hbcy.authcenter.api.modules.sys.dict.dao.SysDictMapper;
import com.hbcy.authcenter.api.modules.sys.dict.model.SysDict;
import com.hbcy.authcenter.api.modules.sys.dict.vo.DictGroupCreateVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.util.BeanCopyUtils;
import jakarta.annotation.Resource;
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
        App app = appMapper.selectById(vo.getAppId());
        if (app == null) {
            throw new ParamError("app不存在");
        }
        SysDict dict = new SysDict();
        BeanCopyUtils.copy(vo, dict);
        //分组的featCode为空字符串
        dict.setFeatCode("");
        dict.setParentId("");
        dict.setCreateUser(UserContextUtils.getUserId());
        try {
            sysDictMapper.insert(dict);
        } catch (DuplicateKeyException e) {
            throw new ParamError("字典类型编码或中文名已存在");
        }
        return dict;
    }

    @Transactional(rollbackFor = Exception.class)
    public SysDict updateGroup(String id, DictGroupCreateVO vo) {
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
        dict.setDeleteTime(deleteTime);
        dict.setUpdateUser(updater);
        sysDictMapper.updateById(dict);
        sysDictMapper.update(new UpdateWrapper<SysDict>()
                .eq(SysDict.COL_PARENT_ID, id)
                .set(SysDict.COL_DELETE_TIME, deleteTime)
                .set(SysDict.COL_UPDATE_USER, updater));
    }

    public List<SysDict> queryGroups(String appId) {

    }
}
