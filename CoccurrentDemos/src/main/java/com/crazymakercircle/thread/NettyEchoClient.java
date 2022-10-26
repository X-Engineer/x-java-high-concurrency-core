package com.crazymakercircle.thread;

import com.crazymakercircle.util.Logger;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.Scanner;

/**
 * create by 尼恩 @ 疯狂创客圈
 **/
public class NettyEchoClient {

    private int serverPort;
    private String serverIp;
    Bootstrap b = new Bootstrap();

    public NettyEchoClient(String ip, int port) {
        this.serverPort = port;
        this.serverIp = ip;
    }

    public void runClient() {
        //创建reactor 线程组
        EventLoopGroup workerLoopGroup = new NioEventLoopGroup();

        try {
            //1 设置reactor 线程组
            b.group(workerLoopGroup);
            //2 设置nio类型的channel
            b.channel(NioSocketChannel.class);
            //3 设置监听端口
            b.remoteAddress(serverIp, serverPort);
            //4 设置通道的参数
            b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

            //5 装配通道流水线
            b.handler(new ChannelInitializer<SocketChannel>() {
                //有连接到达时会创建一个channel
                protected void initChannel(SocketChannel ch) throws Exception {
                    // pipeline管理子通道channel中的Handler
                    // 向子channel流水线添加一个handler处理器
                    //                    ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
//                    ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                    ch.pipeline().addLast(NettyEchoClientHandler.INSTANCE);
                }
            });
            ChannelFuture f = null;

            boolean connected = false;
            while (!connected) {

                f = b.connect();
                f.addListener((ChannelFuture futureListener) ->
                {
                    if (futureListener.isSuccess()) {
                        Logger.info("EchoClient客户端连接成功!");

                    } else {
                        Logger.info("EchoClient客户端连接失败!");
                    }

                });

                // 阻塞,直到连接完成
//            f.sync();

                f.awaitUninterruptibly();


                if (f.isCancelled()) {
                    Logger.tcfo("用户取消连接:");
                    return;
                    // Connection attempt cancelled by user
                } else if (f.isSuccess()) {
                    connected = true;

                }

            }


//            else {
//                // Connection established successfully
//            }
            Channel channel = f.channel();

            Scanner scanner = new Scanner(System.in);
            Logger.tcfo("请输入发送内容:");


            GenericFutureListener sendCallBack = new GenericFutureListener() {

                @Override
                public void operationComplete(Future future) throws Exception {
                    if (future.isSuccess()) {
                        Logger.info("发送成功!");

                    } else {
                        Logger.info("发送失败!");
                    }
                }
            };


            while (scanner.hasNext()) {
                //获取输入的内容
                String next = scanner.next();
                byte[] bytes = (" >>" + next).getBytes("UTF-8");
                //发送ByteBuf
                ByteBuf buffer = channel.alloc().buffer();
                buffer.writeBytes(bytes);
//
//                channel.write(buffer);
//                buffer.retain();
//
//                channel.write(buffer);
//                buffer.retain();

                ChannelFuture writeAndFlushFuture = channel.writeAndFlush(buffer);
                writeAndFlushFuture.addListener(sendCallBack);
                Logger.tcfo("请输入发送内容:");

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 优雅关闭EventLoopGroup，
            // 释放掉所有资源包括创建的线程
            workerLoopGroup.shutdownGracefully();
        }

    }


    @ChannelHandler.Sharable
    static class NettyEchoClientHandler extends ChannelInboundHandlerAdapter {
        static final NettyEchoClientHandler INSTANCE = new NettyEchoClientHandler();

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf in = (ByteBuf) msg;
            int len = in.readableBytes();
            byte[] arr = new byte[len];
            in.getBytes(0, arr);
            Logger.info("client received: " + new String(arr, "UTF-8"));
            in.release();

//        ctx.fireChannelRead(in);
        }
    }
    public static void main(String[] args) throws InterruptedException {
            new NettyEchoClient("127.0.0.1", 9999).runClient();
    }
}