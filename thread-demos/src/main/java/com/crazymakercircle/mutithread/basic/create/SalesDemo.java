package com.crazymakercircle.mutithread.basic.create;

import com.crazymakercircle.util.Print;

import java.util.concurrent.atomic.AtomicInteger;

import static com.crazymakercircle.util.ThreadUtil.getCurThreadName;
import static com.crazymakercircle.util.ThreadUtil.sleepMilliSeconds;

/**
 * 通过实现 Runnable 接口的方式创建线程目标类更加适合多个线程的代码逻辑去共享计算和处理同一个资源的场景。
 * 以下是具体的例子。
 * Created by 尼恩@疯狂创客圈.
 */

public class SalesDemo {
    /**
     * 商品数量
     */
    public static final int MAX_AMOUNT = 5;

    /**
     * 商店商品的销售线程，每条线程异步销售4次
     */
    static class StoreGoods extends Thread {
        StoreGoods(String name) {
            super(name);
        }

        private int goodsAmount = MAX_AMOUNT;

        @Override
        public void run() {
            for (int i = 0; i <= MAX_AMOUNT; i++) {
                if (this.goodsAmount > 0) {
                    Print.cfo(getCurThreadName() + " 卖出一件，还剩："
                            + (--goodsAmount));
                    sleepMilliSeconds(10);

                }
            }
            Print.cfo(getCurThreadName() + " 运行结束.");
        }
    }

    //商场商品的target销售目标类，一个商品最多销售4次，可以多人销售
    static class MallGoods implements Runnable {
        //多人销售, 可能导致数据出错，使用原子数据类型保障数据安全
        private AtomicInteger goodsAmount = new AtomicInteger(MAX_AMOUNT);

        @Override
        public void run() {
            for (int i = 0; i <= MAX_AMOUNT; i++) {
                if (this.goodsAmount.get() > 0) {
                    Print.cfo(getCurThreadName() + " 卖出一件，还剩："
                            + (goodsAmount.decrementAndGet()));
                    //sleepMilliSeconds(10); 没注释这一行之前可能出出现下面的 -1 的情况
                    //[SalesDemo$MallGoods.run]：商场销售员-1 卖出一件，还剩：4
                    //[SalesDemo$MallGoods.run]：商场销售员-2 卖出一件，还剩：3
                    //[SalesDemo$MallGoods.run]：商场销售员-2 卖出一件，还剩：2
                    //[SalesDemo$MallGoods.run]：商场销售员-1 卖出一件，还剩：1
                    //[SalesDemo$MallGoods.run]：商场销售员-1 卖出一件，还剩：-1
                    //[SalesDemo$MallGoods.run]：商场销售员-2 卖出一件，还剩：0
                    //[SalesDemo$MallGoods.run]：商场销售员-2 运行结束.
                    //[SalesDemo$MallGoods.run]：商场销售员-1 运行结束.
                }
            }
            Print.cfo(getCurThreadName() + " 运行结束.");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Print.hint("商店版本的销售");
        for (int i = 1; i <= 2; i++) {
            Thread thread = null;
            thread = new StoreGoods("店员-" + i);
            thread.start();
        }

        Thread.sleep(1000);
        Print.hint("商场的商品销售");
        MallGoods mallGoods = new MallGoods();
        for (int i = 1; i <= 2; i++) {
            Thread thread = null;
            thread = new Thread(mallGoods, "商场销售员-" + i);
            thread.start();
        }


        Print.cfo(getCurThreadName() + " 运行结束.");
    }
}