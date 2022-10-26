package com.crazymakercircle.thread;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import net.openhft.affinity.AffinityLock;
import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;
import org.junit.Test;

import java.util.concurrent.*;

import static net.openhft.affinity.AffinityStrategies.SAME_CORE;
import static net.openhft.affinity.AffinityStrategies.SAME_SOCKET;

public class CpuAffinityDemo {


    @Test
    public void testEmptyLoop0() throws InterruptedException {

        // do some work while locked to a CPU.
        // 空自旋
        while (true) {
        }


    }

    @Test
    public void testEmptyLoop() throws InterruptedException {

        try (AffinityLock affinityLock = AffinityLock.acquireLock(5)) {
            // do some work while locked to a CPU.
            // 空自旋
            while (true) {

            }

        }

    }

    @Test
    public void testEmptyLoop2() throws InterruptedException {

        try (AffinityLock affinityLock = AffinityLock.acquireCore()) {
            // do some work while locked to a CPU.
            // 空自旋
            while (true) {
            }

        }

    }

    @Test
    public void testAffinityThreadFactory() throws InterruptedException {

        //线程工厂
        AffinityThreadFactory affinityThreadFactory = new AffinityThreadFactory("affinityWorker");

        ExecutorService affinityPool = Executors.newFixedThreadPool(4,
                affinityThreadFactory);
        for (int i = 0; i < 12; i++)
            affinityPool.submit(new Callable<Void>() {
                @Override
                public Void call() throws InterruptedException {
                    Thread.sleep(100);
                    return null;
                }
            });
        Thread.sleep(200);
        System.out.println("\nThe assignment of CPUs is\n" + AffinityLock.dumpLocks());
        affinityPool.shutdown();
        affinityPool.awaitTermination(1, TimeUnit.SECONDS);

    }


    @Test
    public void testAffinityThreadFactory2() throws InterruptedException {
        AffinityThreadFactory affinityThreadFactory = new AffinityThreadFactory("affinityWorker", SAME_CORE, SAME_SOCKET);

        ExecutorService affinityPool = Executors.newFixedThreadPool(4,
                affinityThreadFactory);
        for (int i = 0; i < 12; i++)
            affinityPool.submit(new Callable<Void>() {
                @Override
                public Void call() throws InterruptedException {
                    Thread.sleep(100);
                    return null;
                }
            });
        Thread.sleep(200);
        System.out.println("\nThe assignment of CPUs is\n" + AffinityLock.dumpLocks());
        affinityPool.shutdown();
        affinityPool.awaitTermination(1, TimeUnit.SECONDS);

    }


}
