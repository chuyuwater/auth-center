package com.hbcy.authcenter.api.modules.minor.msg;

import com.hbcy.authcenter.api.modules.minor.msg.engine.MsgSeqGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 编号生成器测试
 */
class MsgSeqGeneratorTest {

    private MsgSeqGenerator generator;
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOps;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        generator = new MsgSeqGenerator();
        // 通过反射注入
        try {
            var field = MsgSeqGenerator.class.getDeclaredField("stringRedisTemplate");
            field.setAccessible(true);
            field.set(generator, redisTemplate);
        } catch (Exception e) {
            fail("反射注入失败: " + e.getMessage());
        }
    }

    @Test
    void generate_defaultTenant_usesAPrefix() {
        when(valueOps.increment(anyString())).thenReturn(1L);
        String result = generator.generate("0", MsgSeqGenerator.TYPE_CHANNEL);
        assertEquals("A-MessageCh-000001", result);
    }

    @Test
    void generate_numericTenant_usesTPrefix() {
        when(valueOps.increment(anyString())).thenReturn(5L);
        String result = generator.generate("123", MsgSeqGenerator.TYPE_TEMPLATE);
        assertEquals("T1-MessageTem-000005", result);
    }

    @Test
    void generate_alphaTenant_usesUpperPrefix() {
        when(valueOps.increment(anyString())).thenReturn(10L);
        String result = generator.generate("abc", MsgSeqGenerator.TYPE_SCHEME);
        assertEquals("A-MessageSch-000010", result);
    }

    @Test
    void generate_largeNumber_padsTo6Digits() {
        when(valueOps.increment(anyString())).thenReturn(123456L);
        String result = generator.generate("0", MsgSeqGenerator.TYPE_RULE);
        assertEquals("A-Rule-123456", result);
    }
}
