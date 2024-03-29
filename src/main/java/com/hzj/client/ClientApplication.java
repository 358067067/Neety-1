package com.hzj.client;

import com.alibaba.fastjson.JSON;
import com.hzj.contract.HelloService;
import com.hzj.protocol.RequestMessagePacketEncoder;
import com.hzj.protocol.ResponseMessagePacket;
import com.hzj.protocol.ResponseMessagePacketDecoder;
import com.hzj.protocol.serializer.FastJsonSerializer;
import com.hzj.server.ServerApplication;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;

@Slf4j
public class ClientApplication implements Runnable {

    public static void main(String[] args) throws Exception {
        new Thread(new ClientApplication()).start();
        while (true) {
            System.in.read();
            HelloService service = ContractProxyFactory.ofPoxy(HelloService.class);
            String result = service.sayHello("doge2");
            log.info(result);
        }
    }

    @Override
    public void run() {
        int port = 9092;
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(workerGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));
                    ch.pipeline().addLast(new LengthFieldPrepender(4));
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    ch.pipeline().addLast(new RequestMessagePacketEncoder(FastJsonSerializer.X));
                    ch.pipeline().addLast(new ResponseMessagePacketDecoder());
                    ch.pipeline().addLast(new SimpleChannelInboundHandler<ResponseMessagePacket>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, ResponseMessagePacket packet) throws Exception {
                            Object targetPayload = packet.getPayload();
                            if (targetPayload instanceof ByteBuf) {
                                ByteBuf byteBuf = (ByteBuf) targetPayload;
                                int readableByteLength = byteBuf.readableBytes();
                                byte[] bytes = new byte[readableByteLength];
                                byteBuf.readBytes(bytes);
                                targetPayload = FastJsonSerializer.X.decode(bytes, String.class);
                                byteBuf.release();
                            }
                            packet.setPayload(targetPayload);
                            log.info("接受到来自服务端的响应消息,消息内容{}", JSON.toJSONString(packet));
                        }
                    });
                }
            });
            ChannelFuture future = null;
            try {
                future = bootstrap.connect("localhost", port).sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 保存channel实例，不考虑重连
            ClientChannelHolder.CHANNEL_REFERENCE.set(future.channel());
            // 构造契约接口代理类实例
//            HelloService service = ContractProxyFactory.ofPoxy(HelloService.class);
//            String result = service.sayHello("doge2");
//            log.info(result);
            try {
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
