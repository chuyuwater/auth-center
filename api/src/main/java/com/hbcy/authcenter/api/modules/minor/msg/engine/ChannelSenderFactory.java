package com.hbcy.authcenter.api.modules.minor.msg.engine;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 渠道发送工厂
 * 根据 channelType + provider 查找对应的 ChannelSender 实现
 */
@Component
public class ChannelSenderFactory {
    private final Map<String, ChannelSender> senderMap;

    public ChannelSenderFactory(List<ChannelSender> senders) {
        this.senderMap = senders.stream()
                .collect(Collectors.toMap(
                        s -> s.getChannelType() + ":" + s.getProvider(),
                        Function.identity()
                ));
    }

    /**
     * 获取渠道发送器
     *
     * @param channelType 渠道类型
     * @param provider    服务商编码
     * @return 对应的发送器，不存在时返回null
     */
    public ChannelSender getSender(String channelType, String provider) {
        return senderMap.get(channelType + ":" + provider);
    }
}
