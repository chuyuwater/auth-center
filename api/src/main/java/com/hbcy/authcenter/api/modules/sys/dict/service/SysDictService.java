package com.hbcy.authcenter.api.modules.sys.dict.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.f4b6a3.ulid.UlidCreator;
import com.google.common.base.Splitter;
import com.hbcy.authcenter.api.common.bean.NodeMoveVO;
import com.hbcy.authcenter.api.common.constants.G;
import com.hbcy.authcenter.api.modules.sys.dict.dao.SysDictMapper;
import com.hbcy.authcenter.api.modules.sys.dict.model.SysDict;
import com.hbcy.authcenter.api.modules.sys.dict.vo.DictCreateVO;
import com.hbcy.authcenter.api.modules.sys.dict.vo.DictQueryVO;
import com.hbcy.authcenter.api.modules.sys.dict.vo.DictUpdateVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ClientError;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.json.JsonUtils;
import com.hbcy.common.base.tree.TreeNode;
import com.hbcy.common.base.util.BeanCopyUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 姚泰然
 * @date 2025-12-22 13:45
 */
@Service
public class SysDictService extends ServiceImpl<SysDictMapper, SysDict> {
    //缓存有效期
    public static final Duration DICT_EXPIRE = Duration.ofMinutes(10);
    public static final String DICT_CACHE_KEY_LIST = "portal:sys:dict:list:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private void cleanCache(String featCode) {
        stringRedisTemplate.delete(DICT_CACHE_KEY_LIST + featCode);
    }

    public SysDict createSysDict(DictCreateVO vo) {
        SysDict dict = new SysDict();
        SysDict parent = checkExist(vo.getParentId());
        if (!parent.getFeatCode().isBlank()) {
            //父节点不是分组，需要确认分组对应的是list
            SysDict group = checkExist("", parent.getFeatCode());
            if (group == null) {
                throw new ParamError("分组不存在");
            } else {
                //与分组的dictType一致
                dict.setDictType(group.getDictType());
            }
            if (!group.getDictType().equals(SysDict.DICT_TYPE_TREE)) {
                throw new ParamError("字典类型不是树形结构");
            } else {
                dict.setDictType(SysDict.DICT_TYPE_TREE);
            }
        }
        BeanCopyUtils.copy(vo, dict);
        String userId = UserContextUtils.getUserId();
        dict.setId(UlidCreator.getUlid().toString());
        dict.setAppId(parent.getAppId());
        dict.setFeatCode(parent.getValueStr());
        dict.setIdPath(parent.getIdPath() + G.ID_PATH_SPLITTER + dict.getId());
        dict.setCreateUser(userId);
        dict.setUpdateUser(userId);
        try {
            baseMapper.append(dict);
        } catch (DuplicateKeyException e) {
            throw new ClientError("同一个字典分组下，字典编码和字典名称都要唯一");
        }
        cleanCache(parent.getValueStr());
        return dict;
    }

    @Transactional
    public SysDict updateSysDict(String id, DictUpdateVO vo) {
        SysDict dict = this.getById(id);
        if (dict == null) {
            throw new ClientError("指定字典项不存在");
        }
        BeanCopyUtils.copy(vo, dict);
        dict.setUpdateTime(LocalDateTime.now());
        dict.setUpdateUser(UserContextUtils.getUserId());
        try {
            this.updateById(dict);
        } catch (DuplicateKeyException e) {
            throw new ClientError("同一个字典分组下，字典编码和字典名称都要唯一");
        }
        cleanCache(dict.getFeatCode());
        return dict;
    }

    public void deleteSysDict(String id) {
        SysDict dict = baseMapper.selectById(id);
        if (dict == null) {
            return;
        }
        baseMapper.update(new UpdateWrapper<SysDict>()
                .like(SysDict.COL_ID_PATH, dict.getIdPath())
                .set(SysDict.COL_DELETE_TIME, System.currentTimeMillis())
                .set(SysDict.COL_UPDATE_USER, UserContextUtils.getUserId()));
        cleanCache(dict.getFeatCode());
    }

    private SysDict checkExist(String parentId) {
        SysDict parent = getById(parentId);
        if (parent == null) {
            throw new ClientError("父节点不存在");
        }
        return parent;
    }

    public SysDict checkExist(String featCode, String valueStr) {
        return this.getOne(new QueryWrapper<SysDict>()
                .eq(SysDict.COL_FEAT_CODE, featCode)
                .eq(SysDict.COL_VALUE_STR, valueStr));
    }


    /**
     * 列表状字典的全量获取，或树状字典的逐级获取
     *
     * @param featCode 字典类型编码
     * @param parentId 父节点id，传入空，则返回全量列表
     * @return 字典项列表
     */
    public List<SysDict> listDictByFeatCode(String featCode, String parentId) {
        if (parentId == null) parentId = "";
        String cached = (String) stringRedisTemplate.opsForHash().get(DICT_CACHE_KEY_LIST + featCode, parentId);
        if (StringUtils.isNotBlank(cached)) {
            return JsonUtils.readValue(cached, new TypeReference<>() {
            });
        }
        List<SysDict> list = baseMapper.selectList(new QueryWrapper<SysDict>()
                .eq(SysDict.COL_FEAT_CODE, featCode)
                .eq(SysDict.COL_FORBIDDEN, 0)
                .eq(StringUtils.isNotBlank(parentId), SysDict.COL_PARENT_ID, parentId)
                .orderByAsc(SysDict.COL_SHOW_ORDER));
        stringRedisTemplate.opsForHash().put(DICT_CACHE_KEY_LIST + featCode, parentId,
                JsonUtils.toJsonStr(list));
        stringRedisTemplate.opsForHash().expire(DICT_CACHE_KEY_LIST + featCode, DICT_EXPIRE, List.of(parentId));
        return list;
    }

    /**
     * 移动字典项
     * 只能在本级内调整显示顺序
     * @param vo 移动详情
     */
    @Transactional(rollbackFor = Exception.class)
    public void move(NodeMoveVO vo) {
        SysDict node = baseMapper.selectById(vo.getNodeId());
        if (node == null) {
            throw new ParamError("指定字典项不存在");
        }
        //字典不能修改parentId，所以无需检查
        if (vo.getPrevId() == null) {
            return;
        }
        SysDict prevNode = null;
        if (StringUtils.isNotBlank(vo.getPrevId())) {
            prevNode = baseMapper.selectById(vo.getPrevId());
            if (prevNode == null) {
                throw new ParamError("前节点不存在");
            }
            if (!prevNode.getParentId().equals(node.getParentId())) {
                throw new ParamError("前节点和当前节点的父节点不一致");
            }
        }
        int targetIdx = prevNode == null ? 1 : prevNode.getShowOrder() + 1;
        baseMapper.updateShowOrder(node.getParentId(), targetIdx);
        SysDict toUpdate = new SysDict();
        toUpdate.setId(node.getId());
        toUpdate.setShowOrder(targetIdx);
        baseMapper.updateById(toUpdate);
        cleanCache(node.getFeatCode());
    }

    public List<TreeNode<SysDict>> getChildrenAsTree(DictQueryVO vo) {
        SysDict group = checkExist("", vo.getFeatCode());
        if (group == null) {
            return List.of();
        }
        List<SysDict> records = baseMapper.selectList(new QueryWrapper<SysDict>()
                .likeRight(SysDict.COL_ID_PATH, group.getId() + G.ID_PATH_SPLITTER)
                .and(StringUtils.isNotBlank(vo.getKeyword()),
                        qw -> qw.like(SysDict.COL_VALUE_CN, vo.getKeyword()).or()
                                .like(SysDict.COL_VALUE_STR, vo.getKeyword())));
        if (CollectionUtils.isEmpty(records)) {
            return List.of();
        }
        //需要追溯到根节点（不含分组）
        Set<String> ids = new HashSet<>();
        for (SysDict r : records) {
            ids.addAll(Splitter.on(G.ID_PATH_SPLITTER).splitToList(r.getIdPath()));
        }
        ids.remove(group.getId());
        if (ids.size() > records.size()) {
            records = baseMapper.selectByIds(ids);
        }
        Map<String, List<SysDict>> childrenMap = records.stream()
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
        TreeNode<SysDict> tree = new TreeNode<>(new SysDict());
        buildTree(tree, childrenMap);
        return tree.getChildren();
    }

    private void buildTree(TreeNode<SysDict> current, Map<String, List<SysDict>> childrenMap) {
        String id = current.getData().getId();
        for (SysDict d : childrenMap.getOrDefault(id, List.of())) {
            TreeNode<SysDict> node = new TreeNode<>(d);
            current.addChild(node);
            buildTree(node, childrenMap);
        }
    }
}
