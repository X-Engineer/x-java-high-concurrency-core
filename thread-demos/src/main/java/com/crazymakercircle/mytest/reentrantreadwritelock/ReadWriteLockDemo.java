package com.crazymakercircle.mytest.reentrantreadwritelock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @project: x-java-high-concurrency-core
 * @ClassName: ReadWriteLockDemo
 * @author: nzcer
 * @creat: 2023/3/15 13:45
 * @description: 实现一个读写缓存的操作，MyPlainCache 当前没有任何加锁的操作
 */
class MyPlainCache {
    /**
     * 下面的代码是没有加锁的，这样就会造成线程在进行写入操作的时候，被其它线程频繁打断，从而不具备原子性，
     * 这个时候，我们就需要用到读写锁来解决了
     */
    private volatile Map<String, Object> map = new HashMap<>();

    public void put(String key, Object value) {
        System.out.println(Thread.currentThread().getName() + "\t正在写入: " + key);
        try {
            // 模拟网络拥塞，延迟 0.3 秒
            TimeUnit.MILLISECONDS.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        map.put(key, value);
        System.out.println(Thread.currentThread().getName() + "\t写入完成");
    }

    public Object get(String key) {
        System.out.println(Thread.currentThread().getName() + "\t正在读取...");
        try {
            // 模拟网络拥塞，延迟 0.3 秒
            TimeUnit.MILLISECONDS.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + "\t读取完成: " + map.get(key));
        return map.get(key);
    }
}

class MyReadWriteLock {
    /**
     * 读写锁
     * 多个线程 同时读一个资源类没有任何问题，所以为了满足并发量，读取共享资源应该可以同时进行
     * 但是，如果一个线程想去写共享资源，就不应该再有其它线程可以对该资源进行读或写
     */
    // 多线程访问共享遍历，必须保持可见性，因此使用volatile修饰
    private volatile Map<String, Object> map = new HashMap<>();
    /**
     * 创建一个读写锁
     * 它是一个读写融为一体的锁，在使用的时候，需要转换
     */
    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public void put(String key, Object value) {
        // 创建一个写锁
        rwLock.writeLock().lock();

        try {
            System.out.println(Thread.currentThread().getName() + "\t正在写入: " + key);
            try {
                // 模拟网络拥塞，延迟 0.3 秒
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            map.put(key, value);
            System.out.println(Thread.currentThread().getName() + "\t写入完成");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放写锁
            rwLock.writeLock().unlock();
        }
    }

    public Object get(String key) {
        // 获取读锁

        rwLock.readLock().lock();

        try {
            System.out.println(Thread.currentThread().getName() + "\t正在读取...");
            try {
                // 模拟网络拥塞，延迟 0.3 秒
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + "\t读取完成: " + map.get(key));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放读锁
            rwLock.readLock().unlock();
        }
        return map.get(key);
    }
}

public class ReadWriteLockDemo {
    public static void main(String[] args) {
        //plainReadWrite();
        lockReadWrite();
    }

    public static void plainReadWrite() {
        MyPlainCache myPlainCache = new MyPlainCache();
        // 五个线程写
        int threadNum = 5;
        for (int i = 0; i < threadNum; i++) {
            // lambda表达式内部必须是final
            final int curThreadNumber = i;
            new Thread(() -> {
                myPlainCache.put(curThreadNumber + "", curThreadNumber);
            }, String.valueOf(curThreadNumber)).start();
        }
        // 五个线程读
        for (int i = 0; i < threadNum; i++) {
            final int curThreadNum = i;
            new Thread(() -> {
                myPlainCache.get(curThreadNum + "");
            }, String.valueOf(curThreadNum)).start();
        }
        /**
         * 结果如下
         * 4	正在写入: 4
         * 3	正在写入: 3
         * 2	正在写入: 2
         * 0	正在写入: 0
         * 0	正在读取...
         * 1	正在写入: 1
         * 1	正在读取...
         * 2	正在读取...
         * 3	正在读取...
         * 4	正在读取...
         * 0	读取完成: null
         * 2	读取完成: 2
         * 3	读取完成: null
         * 0	写入完成
         * 2	写入完成
         * 4	读取完成: null
         * 3	写入完成
         * 4	写入完成
         * 1	写入完成
         * 1	读取完成: null
         */
    }

    public static void lockReadWrite() {
        MyReadWriteLock myReadWriteLock = new MyReadWriteLock();
        // 五个线程写
        int threadNum = 5;
        for (int i = 0; i < threadNum; i++) {
            // lambda表达式内部必须是final
            final int curThreadNumber = i;
            new Thread(() -> {
                myReadWriteLock.put(curThreadNumber + "", curThreadNumber);
            }, String.valueOf(curThreadNumber)).start();
        }
        // 五个线程读
        for (int i = 0; i < threadNum; i++) {
            final int curThreadNum = i;
            new Thread(() -> {
                myReadWriteLock.get(curThreadNum + "");
            }, String.valueOf(curThreadNum)).start();
        }
        /**
         * 读锁和写锁的区别在于，写锁一次只能一个线程进入，执行写操作，而读锁是多个线程能够同时进入，进行读取的操作
         * 运行结果我们可以看出，写入操作是一个一个线程进行执行的，并且中间不会被打断，而读操作的时候，是同时5个线程进入，然后并发读取操作
         * 结果如下
         * 0	正在写入: 0
         * 0	写入完成
         * 1	正在写入: 1
         * 1	写入完成
         * 2	正在写入: 2
         * 2	写入完成
         * 3	正在写入: 3
         * 3	写入完成
         * 4	正在写入: 4
         * 4	写入完成
         * 2	正在读取...
         * 1	正在读取...
         * 0	正在读取...
         * 3	正在读取...
         * 4	正在读取...
         * 0	读取完成: 0
         * 4	读取完成: 4
         * 1	读取完成: 1
         * 2	读取完成: 2
         * 3	读取完成: 3
         */
    }
}