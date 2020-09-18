package com.crazymakercircle.mutithread.basic.threadlocal;

import com.crazymakercircle.util.Print;
import com.crazymakercircle.util.ThreadUtil;
import lombok.Data;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import static com.crazymakercircle.util.ThreadUtil.sleepMilliSeconds;

public class ThreadLocalTest
{
    @Data
    static class Foo
    {
        //实例总数
        static final AtomicInteger AMOUNT = new AtomicInteger(0);
        //对象的编号
        int index = 0;
        //对象的内容
        int bar = 10;

        //构造器
        public Foo()
        {
            index = AMOUNT.incrementAndGet(); //总数增加，并且给对象的编号
        }

        @Override
        public String toString()
        {
            return index + "@Foo{bar=" + bar + '}';
        }
    }

    //定义线程本地变量
    private static final ThreadLocal<Foo> localFoo = new ThreadLocal<Foo>();

    public static void main(String[] args) throws InterruptedException
    {
//        ThreadLocal<Foo> localFoo = ThreadLocal.withInitial(() -> new Foo());

        //获取自定义的混合线程池
        ThreadPoolExecutor threadPool = ThreadUtil.getMixedTargetThreadPool();

        //共5个线程
        for (int i = 0; i < 5; i++)
        {
            threadPool.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    //获取“线程本地变量”中当前线程所绑定的值
                    if (localFoo.get() == null)
                    {
                        //设置“线程本地变量”中当前线程所绑定的值
                        localFoo.set(new Foo());
                    }

                    Print.tco("初始的本地值：" + localFoo.get());
                    //每个线程执行10次
                    for (int i = 0; i < 10; i++)
                    {
                        Foo foo = localFoo.get();
                        foo.setBar(foo.getBar() + 1);
                        sleepMilliSeconds(10);

                    }
                    Print.tco("累加10次之后的本地值：" + localFoo.get());

                    //删除“线程本地变量”中当前线程所绑定的值，对于线程池中的线程尤其重要
                    localFoo.remove();
                }
            });
        }
    }
}
