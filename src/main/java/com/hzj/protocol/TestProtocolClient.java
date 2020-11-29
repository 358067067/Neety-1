package com.hzj.protocol;

import com.alibaba.fastjson.JSON;
import com.hzj.protocol.serializer.FastJsonSerializer;
import com.hzj.protocol.serializer.Serializer;
import com.hzj.utils.SerialNumberUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestProtocolClient {
    public static void main(String[] args) throws Exception {
        int port = 9092;
        EventLoopGroup workGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(workGroup);
            bootstrap.channel(NioSocketChannel.class);
            //建立应用层心跳监听
            bootstrap.option(ChannelOption.SO_KEEPALIVE, Boolean.TRUE);
            //禁用nagle算法，降低通信延迟（因为TCP默认使用nagle策略，每次发送都尽量是足够大的数据）
            bootstrap.option(ChannelOption.TCP_NODELAY, Boolean.TRUE);
            //设置pipeline
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel sc) throws Exception {
                    sc.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));
                    sc.pipeline().addLast(new LengthFieldPrepender(4));
                    sc.pipeline().addLast(new RequestMessagePacketEncoder(FastJsonSerializer.X));
                    sc.pipeline().addLast(new ResponseMessagePacketDecoder());
                    sc.pipeline().addLast(new SimpleChannelInboundHandler<ResponseMessagePacket>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, ResponseMessagePacket packet) throws Exception {
                            Object targetPayLoad = packet.getPayload();
                            if (targetPayLoad instanceof ByteBuf) {
                                ByteBuf byteBuf = (ByteBuf) targetPayLoad;
                                int readableByteLength = byteBuf.readableBytes();
                                byte[] bytes = new byte[readableByteLength];
                                byteBuf.readBytes(bytes);
                                targetPayLoad = FastJsonSerializer.X.decode(bytes, String.class);
                                byteBuf.release();
                            }
                            packet.setPayload(targetPayLoad);
                            log.info("接收到来自服务端的响应，消息内容：{}", JSON.toJSONString(packet));
                        }
                    });
                }
            });
            //启动通信
            ChannelFuture future = bootstrap.connect("localhost", port).sync();
            log.info("启动NettyClient[{}]成功...", port);
            Channel channel = future.channel();
            RequestMessagePacket packet = new RequestMessagePacket();
            packet.setMagicNumber(ProtocolConstant.MAGIC_NUMBER);
            packet.setVersion(ProtocolConstant.VERSION);
            packet.setSerialNumber(SerialNumberUtils.X.generateSerialNumber());
            packet.setMessageType(MessageType.REQUEST);
            packet.setInterfaceName("com.hzj.service.HelloService");
            packet.setMethodName("sayHello");
            packet.setMethodArgumentSignatures(new String[]{"java.lang.String"});
            packet.setMethodArguments(new Object[]{"doge"});
            channel.writeAndFlush(packet);
            future.channel().closeFuture().sync();
        } finally {
            workGroup.shutdownGracefully();
        }
    }
}
