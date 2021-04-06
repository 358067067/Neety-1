package com.hzj.client;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hzj.protocol.MessageType;
import com.hzj.protocol.ProtocolConstant;
import com.hzj.protocol.RequestMessagePacket;
import com.hzj.utils.SerialNumberUtils;
import io.netty.channel.Channel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentMap;

/**
 * 契约动态代理工厂（工具类）
 */
public class ContractProxyFactory {
    private static final RequestArgumentExtractor EXTRACTOR = new DefaultRequestArgumentExtractor();
    private static final ConcurrentMap<Class<?>, Object> CACHE = Maps.newConcurrentMap();

    public static <T> T ofPoxy(Class<T> interfaceKlcass) {
        return (T) CACHE.computeIfAbsent(interfaceKlcass,x -> {
            return Proxy.newProxyInstance(interfaceKlcass.getClassLoader(), new Class[]{interfaceKlcass},(target, method, args) -> {
                RequestArgumentExtractInput input = new RequestArgumentExtractInput();
                input.setInterfaceKlass(interfaceKlcass);
                input.setMethod(method);
                RequestArgumentExtractOutput output = EXTRACTOR.extract(input);
                // 封装请求参数
                RequestMessagePacket packet = new RequestMessagePacket();
                packet.setMagicNumber(ProtocolConstant.MAGIC_NUMBER);
                packet.setVersion(ProtocolConstant.VERSION);
                packet.setSerialNumber(SerialNumberUtils.X.generateSerialNumber());
                packet.setMessageType(MessageType.REQUEST);
                packet.setInterfaceName(output.getInterfaceName());
                packet.setMethodName(output.getMethodName());
                packet.setMethodArgumentSignatures(output.getMethodArgumentSignatures().toArray(new String[0]));
                packet.setMethodArguments(args);
                Channel channel = ClientChannelHolder.CHANNEL_REFERENCE.get();
                // 发起请求
                channel.writeAndFlush(packet);
                // 这里方法返回值需要进行同步处理,相对复杂,后面专门开一篇文章讲解,暂时统一返回字符串
                // 如果契约接口的返回值类型不是字符串,这里方法返回后会抛出异常
                return String.format("[%s#%s]调用成功,发送了[%s]到NettyServer[%s]", output.getInterfaceName(),
                        output.getMethodName(), JSON.toJSONString(packet), channel.remoteAddress());
            });
        });
    }
}
