package com.hbcy.authcenter.api.modules.sys.dict.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbcy.authcenter.api.modules.sys.dict.dao.SysDictMapper;
import com.hbcy.authcenter.api.modules.sys.dict.model.SysDict;
import com.hbcy.authcenter.api.modules.sys.dict.vo.DictQueryVO;
import com.hbcy.authcenter.api.modules.sys.dict.vo.DictUpsertVO;
import com.hbcy.common.base.error.ClientError;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.tree.TreeNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 姚泰然
 * @date 2025-12-22 13:45
 */
@Service
public class SysDictService extends ServiceImpl<SysDictMapper, SysDict> {
    //缓存有效期
    private static final String CACHE_NAME = "@5m";

    @Caching(
            evict = {
                    @CacheEvict(value = CACHE_NAME, key = "'authcenter:sys:dict:list:' + #vo.featCode"),
                    @CacheEvict(value = CACHE_NAME, key = "'authcenter:sys:dict:valueMap:' + #vo.featCode")
            }
    )
    public SysDict createSysDict(DictUpsertVO vo) {
        checkExist(vo.getParentId());
        SysDict sysDict = new SysDict();
        BeanUtils.copyProperties(vo, sysDict);
        try {
            this.save(sysDict);
        } catch (DuplicateKeyException e) {
            throw new ClientError("字典编码和字典值组合已存在");
        }
        return sysDict;
    }

    @Caching(
            evict = {
                    @CacheEvict(value = CACHE_NAME, key = "'authcenter:sys:dict:list:' + #vo.featCode"),
                    @CacheEvict(value = CACHE_NAME, key = "'authcenter:sys:dict:valueMap:' + #vo.featCode")
            }
    )
    @Transactional
    public SysDict updateSysDict(String id, DictUpsertVO vo) {
        SysDict sysDict = this.getById(id);
        if (sysDict == null) {
            throw new ClientError("指定字典项不存在");
        }
        BeanUtils.copyProperties(vo, sysDict);
        try {
            this.updateById(sysDict);
        } catch (DuplicateKeyException e) {
            throw new ClientError("字典编码和字典值组合已存在");
        }
        return sysDict;
    }

    @Caching(
            evict = {
                    @CacheEvict(value = CACHE_NAME, key = "'authcenter:sys:dict:list:' + #featCode"),
                    @CacheEvict(value = CACHE_NAME, key = "'authcenter:sys:dict:valueMap:' + #featCode")
            }
    )
    public void deleteSysDict(String featCode, String id) {
        this.removeById(id);
    }

    private void checkExist(String parentId) {
        if (StringUtils.isNotBlank(parentId)) {
            SysDict parent = getById(parentId);
            if (parent == null) {
                throw new ClientError("父级字典项不存在");
            }
        }
    }

    public boolean checkExist(String featCode, String valueStr) {
        SysDict one = this.getOne(new QueryWrapper<SysDict>()
                .eq(SysDict.COL_FEAT_CODE, featCode)
                .eq(SysDict.COL_VALUE_STR, valueStr));
        return one != null;
    }


    @Cacheable(value = CACHE_NAME, key = "'authcenter:sys:dict:list:' + #featCode")
    public List<SysDict> getSysDictsByFeatCode(String featCode) {
        return this.list(new QueryWrapper<SysDict>()
                .eq(SysDict.COL_FEAT_CODE, featCode)
                .orderByAsc(SysDict.COL_SHOW_ORDER));
    }

    @Cacheable(value = CACHE_NAME, key = "'authcenter:sys:dict:valueMap:' + #featCode")
    public Map<String, String> getDictValueMapByFeatCode(String featCode) {
        List<SysDict> list = this.list(new QueryWrapper<SysDict>().eq(SysDict.COL_FEAT_CODE, featCode));
        return list.stream().collect(Collectors.toMap(SysDict::getValueStr, SysDict::getValueCn));
    }

    public List<SysDict> getChildrenRecursively(String parentId) {
        return baseMapper.selectChildrenRecursively(parentId);
    }

    public List<SysDict> getChildrenRecursively(String parentFeatCode, String parentValueStr) {
        SysDict parent = this.getOne(new QueryWrapper<SysDict>()
                .eq(SysDict.COL_FEAT_CODE, parentFeatCode)
                .eq(SysDict.COL_VALUE_STR, parentValueStr));
        if (parent == null) {
            return List.of();
        }
        return getChildrenRecursively(parent.getId());
    }

    public TreeNode<SysDict> getChildrenAsTree(DictQueryVO vo) {
        List<SysDict> list = null;
        SysDict root;
        if (StringUtils.isNotBlank(vo.getParentId())) {
            root = getById(vo.getParentId());
            list = getChildrenRecursively(vo.getParentId());
        } else if (StringUtils.isNotBlank(vo.getFeatCode()) && StringUtils.isNotBlank(vo.getValueStr())) {
            root = this.getOne(new QueryWrapper<SysDict>()
                    .eq(SysDict.COL_FEAT_CODE, vo.getFeatCode())
                    .eq(SysDict.COL_VALUE_STR, vo.getValueStr()));
            list = getChildrenRecursively(vo.getFeatCode(), vo.getValueStr());
        } else {
            root = null;
        }
        if (root == null) {
            throw new ParamError("specified parent not exist");
        }
        Map<String, List<SysDict>> childrenMap = list.stream()
                .collect(Collectors.groupingBy(
                        SysDict::getParentId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                l -> {
                                    l.sort(Comparator.comparingInt(SysDict::getShowOrder));
                                    return l;
                                }
                        )
                ));
        TreeNode<SysDict> tree = new TreeNode<>(root);
        buildTree(tree, childrenMap);
        return tree;
    }

    private void buildTree(TreeNode<SysDict> current, Map<String, List<SysDict>> childrenMap) {
        for (SysDict d : childrenMap.get(current.getData().getId())) {
            TreeNode<SysDict> node = new TreeNode<>(d);
            current.addChild(node);
            buildTree(node, childrenMap);
        }
    }
}
