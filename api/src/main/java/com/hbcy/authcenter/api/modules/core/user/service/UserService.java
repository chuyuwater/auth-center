package com.hbcy.authcenter.api.modules.core.user.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbcy.authcenter.api.modules.core.user.dao.UserMapper;
import com.hbcy.authcenter.api.modules.core.user.model.User;
import com.hbcy.common.db.convertor.IUserService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2025-12-26 09:25
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User> implements IUserService {

    public static final String NAME_CACHE_KEY = "portal:user:name";
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String getName(String userId) {
        Object s = stringRedisTemplate.opsForHash().get(NAME_CACHE_KEY, userId);
        if (s == null) {
            String name = baseMapper.selectNameById(userId);
            if (name != null) {
                stringRedisTemplate.opsForHash().put(NAME_CACHE_KEY, userId, name);
            }
            return name;
        }
        return s.toString();
    }

    private void cleanNameCache(String userId) {
        stringRedisTemplate.opsForHash().delete(NAME_CACHE_KEY, userId);
    }

    private void cleanNameCache(List<String> userIds) {
        stringRedisTemplate.opsForHash().delete(NAME_CACHE_KEY, userIds.toArray());
    }
}
