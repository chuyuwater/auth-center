package com.hbcy.authcenter.api.modules.core.user.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbcy.authcenter.api.common.constants.G;
import com.hbcy.authcenter.api.modules.core.user.dao.UserMapper;
import com.hbcy.authcenter.api.modules.core.user.model.User;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2025-12-26 09:25
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private void cleanNameCache(String userId) {
        stringRedisTemplate.opsForHash().delete(G.USER_NAME_CACHE_KEY, userId);
    }

    private void cleanNameCache(List<String> userIds) {
        stringRedisTemplate.opsForHash().delete(G.USER_NAME_CACHE_KEY, userIds.toArray());
    }
}
