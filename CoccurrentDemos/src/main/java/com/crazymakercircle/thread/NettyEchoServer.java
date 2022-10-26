package com.crazymakercircle.thread;

import com.crazymakercircle.util.Logger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;

import java.util.concurrent.ThreadFactory;

/**
 * create by 尼恩 @ 疯狂创客圈
 **/
public class NettyEchoServer {

    private final int serverPort;
    ServerBootstrap b = new ServerBootstrap();

    public NettyEchoServer(int port) {
        this.serverPort = port;
    }

    public void runServer() {


        //创建AffinityThreadFactory
        ThreadFactory bossThreadFactory = new AffinityThreadFactory("bossThread",
                AffinityStrategies.DIFFERENT_CORE,
                 AffinityStrategies.ANY);

        //建立两个EventloopGroup用来处理连接和消息
        EventLoopGroup bossLoopGroup = new NioEventLoopGroup(1,bossThreadFactory);


        //创建AffinityThreadFactory
        ThreadFactory workerThreadFactory = new AffinityThreadFactory("workerThread",
                AffinityStrategies.DIFFERENT_CORE,
                AffinityStrategies.ANY);


        //将AffinityThreadFactory加入workerGroup
        EventLoopGroup workerLoopGroup = new NioEventLoopGroup(2, workerThreadFactory);

        try {
            //1 设置reactor 线程组
            b.group(bossLoopGroup, workerLoopGroup);
            //2 设置nio类型的channel
            b.channel(NioServerSocketChannel.class);
            //3 设置监听端口
            b.localAddress(serverPort);
            //4 设置通道的参数
//            b.option(ChannelOption.SO_KEEPALIVE, true);
//            b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            b.option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT);

            b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            b.childOption(ChannelOption.SO_KEEPALIVE, true);


            //5 装配子通道流水线
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                //有连接到达时会创建一个channel
                protected void initChannel(SocketChannel ch) throws Exception {
                    // pipeline管理子通道channel中的Handler
                    // 向子channel流水线添加一个handler处理器
//                    ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
//                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    ch.pipeline().addLast(NettyEchoServerHandler.INSTANCE);
                }
            });
            // 6 开始绑定server
            // 通过调用sync同步方法阻塞直到绑定成功
            ChannelFuture channelFuture = b.bind();
            channelFuture.addListener((future)->{
                if(future.isSuccess())
                {
                    Logger.info(" ========》反应器线程 回调 服务器启动成功，监听端口: " +
                            channelFuture.channel().localAddress());

                }
            });
//            channelFuture.sync();
            Logger.info(" 调用线程执行的，服务器启动成功，监听端口: " +
                    channelFuture.channel().localAddress());

            // 7 等待通道关闭的异步任务结束
            // 服务监听通道会一直等待通道关闭的异步任务结束
            ChannelFuture closeFuture = channelFuture.channel().closeFuture();
            closeFuture.sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 8 优雅关闭EventLoopGroup，
            // 释放掉所有资源包括创建的线程
            workerLoopGroup.shutdownGracefully();
            bossLoopGroup.shutdownGracefully();
        }

    }
    /**
     * create by 尼恩 @ 疯狂创客圈
     **/
    @ChannelHandler.Sharable
    static class NettyEchoServerHandler extends ChannelInboundHandlerAdapter {
         static final NettyEchoServerHandler INSTANCE = new NettyEchoServerHandler();

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

            ByteBuf in = (ByteBuf) msg;

            Logger.info("msg type: " + (in.hasArray()?"堆内存":"直接内存"));

            int len = in.readableBytes();
            byte[] arr = new byte[len];
            in.getBytes(0, arr);
//        in.readByte()
            Logger.info("server received: " + new String(arr, "UTF-8"));

            //写回数据，异步任务
            Logger.info("写回前，msg.refCnt:" + (in.refCnt()));

            ChannelFuture f = ctx.writeAndFlush(msg);
//        ChannelFuture f = ctx.pipeline().writeAndFlush(msg);
//        ChannelFuture f = ctx.channel().pipeline().writeAndFlush(msg);


            f.addListener((ChannelFuture futureListener) -> {
                Logger.info("写回后，msg.refCnt:" + in.refCnt());
            });
//        ctx.fireChannelRead(msg);
        }
    }
    public static void main(String[] args) throws InterruptedException {
        new NettyEchoServer(9999).runServer();
    }
}