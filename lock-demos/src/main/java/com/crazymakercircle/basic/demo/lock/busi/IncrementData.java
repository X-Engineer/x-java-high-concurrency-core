package com.crazymakercircle.basic.demo.lock.busi;

import com.crazymakercircle.util.Print;

import java.util.concurrent.locks.Lock;

import static com.crazymakercircle.util.JvmUtil.curThreadName;

public class IncrementData
{
    public static int sum = 0;

    public static void lockAndFastIncrease(Lock lock)
    {
        // Print.tcfo(curThreadName()+" -- 本线程开始抢占锁");
        lock.lock();
        //Print.tcfo(curThreadName()+" ^-^本线程抢到了锁");
        try
        {
            sum++;
        } finally
        {
            lock.unlock();
            //Print.tcfo("本线程释放了锁");
        }
    }

    public static void lockAndIncrease(Lock lock)
    {
        Print.tcfo(curThreadName() + " -- 开始抢占锁");
        lock.lock();
        Print.tcfo(curThreadName() + " ^-^ 抢到了锁");
        try
        {
            Thread.sleep(100);
            sum++;
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } finally
        {
            lock.unlock();
            Print.tcfo(curThreadName() + " -- 释放了锁");
        }
    }

    //演示方法：抢锁过程可中断
    public static void lockInterruptiblyAndIncrease(Lock lock)
    {
        Print.tcfo(curThreadName() + " -- 本线程开始抢占锁");
        try
        {
            lock.lockInterruptibly();
        } catch (InterruptedException e)
        {
            Print.tcfo(curThreadName() + " @-@本线程被中断，抢锁失败");
            // e.printStackTrace();
            return;
        }
        Print.tcfo(curThreadName() + " ^-^本线程抢到了锁");
        try
        {
            //等待100ms
            Thread.sleep(100);
            sum++;
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } finally
        {
            lock.unlock();
            Print.tcfo("本线程释放了锁");
        }
    }


    public static void tryLockAndIncrease(Lock lock)
    {
        if (lock.tryLock())
        {
            Print.tcfo("本线程抢到了锁");

            try
            {
                Thread.sleep(100);
                sum++;
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            } finally
            {
                lock.unlock();
                Print.tcfo("本线程释放了锁");

            }
        } else
        {
            // perform alternative actions
            Print.tcfo("本线程抢锁失败");
        }
    }

}
