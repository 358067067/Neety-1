package com.hzj.codec;

import com.hzj.type.RequestMessagePacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class RequestMessagePacketEncoder extends MessageToByteEncoder<RequestMessagePacket> {

    private final Serializer serializer;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RequestMessagePacket packet, ByteBuf out) throws Exception {
        //魔数
        out.writeInt(packet.getMagicNumber());
        //版本
        out.writeInt(packet.getVersion());
        //流水号(非基本类型，序列化和反序列化前，一定要先写入读取序列的长度！）
        out.writeInt(packet.getSerialNumber().length());
        out.writeCharSequence(packet.getSerialNumber(), CharsetUtil.UTF_8);
        //消息类型
        out.writeByte(packet.getMessageType().getType());
        //附件大小
        Map<String, String> attachments = packet.getAttachments();
        out.writeInt(attachments.size());
        //附件内容
        attachments.forEach((k, v) ->{
            out.writeInt(k.length());
            out.writeCharSequence(k, CharsetUtil.UTF_8);
            out.writeInt(v.length());
            out.writeCharSequence(v, CharsetUtil.UTF_8);
        });
        //接口类全名
        out.writeInt(packet.getInterfaceName().length());
        out.writeCharSequence(packet.getInterfaceName(), CharsetUtil.UTF_8);
        //方法名
        out.writeInt(packet.getMethodName().length());
        out.writeCharSequence(packet.getMethodName(), CharsetUtil.UTF_8);
        //方法参数签名(String[]类型) - 非必须
        if (null != packet.getMethodArguments()) {
            int len = packet.getMethodArguments().length;
            out.writeInt(len);
            for (int i = 0; i < len; i++) {
                String methodArgumentSignature = packet.getMethodArgumentSignatures()[i];
                out.writeInt(methodArgumentSignature.length());
                out.writeCharSequence(methodArgumentSignature, CharsetUtil.UTF_8);
            }
        } else {
            out.writeInt(0);
        }
        // 方法参数(Object[]类型) - 非必须
        if (null != packet.getMethodArguments()) {
            int len = packet.getMethodArguments().length;
            // 方法参数数组长度
            out.writeInt(len);
            for (int i = 0; i < len; i++) {
                byte[] bytes = serializer.encode(packet.getMethodArguments()[i]);
                out.writeInt(bytes.length);
                out.writeBytes(bytes);
            }
        } else {
            out.writeInt(0);
        }
    }
}
