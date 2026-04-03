package com.hbcy.authcenter.api.modules.minor.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbcy.authcenter.api.modules.minor.user.dao.UserCustomStyleMapper;
import com.hbcy.authcenter.api.modules.minor.user.model.UserCustomStyle;
import com.hbcy.authcenter.api.modules.minor.user.vo.UserCustomStyleCreateVO;
import com.hbcy.authcenter.api.modules.sys.dict.model.SysDict;
import com.hbcy.authcenter.api.modules.sys.dict.service.SysDictService;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.util.BeanCopyUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author 姚泰然
 * @date 2026-04-03 14:43
 */
@Service
public class UserCustomStyleService extends ServiceImpl<UserCustomStyleMapper, UserCustomStyle> {
    private static final String USER_CUSTOM_STYLE_DICT = "USER_CUSTOM_STYLE";

    @Resource
    private SysDictService sysDictService;

    /**
     * 当前用户自定义风格列表
     * @param item 自定义项编码，可选；传值时仅返回该自定义项的数据
     * @return 当前登录用户的自定义风格列表
     */
    public List<UserCustomStyle> list4User(String item) {
        return baseMapper.selectList(new QueryWrapper<UserCustomStyle>()
                .eq(UserCustomStyle.COL_USER_ID, UserContextUtils.getUserId())
                .eq(StringUtils.isNotBlank(item), UserCustomStyle.COL_ITEM, item)
                .orderByAsc(UserCustomStyle.COL_ID));
    }

    /**
     * 指定配置项查询
     * @param item 配置项
     * @return 指定配置项的配置，不存在时返回null
     */
    public UserCustomStyle getItem(String item) {
        return getCurrentUserStyle(item);
    }


    @Transactional(rollbackFor = Exception.class)
    public UserCustomStyle upsert(UserCustomStyleCreateVO vo) {
        validateStyle(vo.getItem(), vo.getSetting());
        UserCustomStyle entity = new UserCustomStyle();
        BeanCopyUtils.copy(vo, entity);
        entity.setUserId(UserContextUtils.getUserId());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        try {
            baseMapper.upsert(entity);
        } catch (DuplicateKeyException e) {
            throw new ParamError("当前用户已存在相同自定义项");
        }
        return entity;
    }

    public void delete(String item) {
        UserCustomStyle entity = getCurrentUserStyle(item);
        if (entity == null) {
            return;
        }
        baseMapper.deleteById(entity.getId());
    }

    private UserCustomStyle getCurrentUserStyle(String item) {
        return baseMapper.selectOne(new QueryWrapper<UserCustomStyle>()
                .eq(UserCustomStyle.COL_ITEM, item)
                .eq(UserCustomStyle.COL_USER_ID, UserContextUtils.getUserId()));
    }

    private void validateStyle(String item, String setting) {
        SysDict itemDict = sysDictService.checkExist(USER_CUSTOM_STYLE_DICT, item);
        if (itemDict == null) {
            throw new ParamError("指定自定义项不存在");
        }
        List<SysDict> children = sysDictService.listDictByFeatCode(item, itemDict.getId());
        if (CollectionUtils.isEmpty(children)) {
            return;
        }
        boolean matched = children.stream()
                .anyMatch(dict -> Objects.equals(dict.getValueStr(), setting));
        if (!matched) {
            throw new ParamError("当前自定义项的setting必须使用字典预定义值");
        }
    }
}
