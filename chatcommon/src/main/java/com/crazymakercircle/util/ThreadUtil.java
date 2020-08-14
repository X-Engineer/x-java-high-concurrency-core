/**
 * Created by 尼恩@疯狂创客圈
 */
package com.crazymakercircle.util;


import java.util.concurrent.locks.LockSupport;

public class ThreadUtil
{

    /**
     * 线程睡眠
     *
     * @param second 秒
     */
    public static void sleepSeconds(int second)
    {
        LockSupport.parkNanos(second * 1000L * 1000L * 1000L);
    }

    /**
     * 线程睡眠
     *
     * @param millisecond 毫秒
     */
    public static void sleepMilliSeconds(int millisecond)
    {
        LockSupport.parkNanos(millisecond * 1000L * 1000L);
    }


    /**
     * 调用栈中的类名
     *
     * @return
     */
    public static String stackClassName(int level)
    {
//        Thread.currentThread().getStackTrace()[1]是当前方法 curClassName 执行堆栈
//        Thread.currentThread().getStackTrace()[2]就是 curClassName 的 上一级的方法堆栈 以此类推

        String className = Thread.currentThread().getStackTrace()[level].getClassName();//调用的类名
        return className;

    }

    /**
     * 调用栈中的方法名称
     *
     * @return
     */
    public static String stackMethodName(int level)
    {
//        Thread.currentThread().getStackTrace()[1]是当前方法 curMethodName 执行堆栈
//        Thread.currentThread().getStackTrace()[2]就是 curMethodName 的 上一级的方法堆栈 以此类推

        String className = Thread.currentThread().getStackTrace()[level].getMethodName();//调用的类名
        return className;
    }
}
