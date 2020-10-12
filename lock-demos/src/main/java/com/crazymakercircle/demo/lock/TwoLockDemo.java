package com.crazymakercircle.demo.lock;

import com.crazymakercircle.util.Print;
import com.crazymakercircle.util.RandomUtil;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static com.crazymakercircle.util.JvmUtil.curThreadName;

public class TwoLockDemo
{
    //演示代码：使用两把锁, 通过可以中断的方式抢锁
    public static void useTowlockInterruptiblyLock(Lock lock1, Lock lock2)
    {
        String lock1Name = lock1.toString()
                .replace("java.util.concurrent.locks.", "");

        String lock2Name = lock2.toString()
                .replace("java.util.concurrent.locks.", "");
        Print.tcfo(curThreadName() + " -- 开始抢占锁, 锁为：" + lock1Name);
        try
        {
            lock1.lockInterruptibly();
        } catch (InterruptedException e)
        {
            Print.tcfo(curThreadName() + " @-@被中断，抢占失败, 锁为：" + lock1Name);
            //e.printStackTrace();
            return;
        }
        Print.tcfo(curThreadName() + " ^-^抢到了, 锁为：" + lock1Name);

        try
        {
            Print.tcfo(curThreadName() + " -- 开始抢占, 锁为：" + lock2Name);
            try
            {
                lock2.lockInterruptibly();
            } catch (InterruptedException e)
            {
                Print.tcfo(curThreadName() + " @-@被中断，抢锁失败, 锁为：" + lock2Name);
                //e.printStackTrace();
                return;
            }
            Print.tcfo(curThreadName() + " ^-^抢到了, 锁为：" + lock2Name);
            try
            {
                Print.tcfo(curThreadName() + "do something ");
                //等待1000ms
                Thread.sleep(10000);
            } catch (Exception e)
            {
                e.printStackTrace();
            } finally
            {
                lock2.unlock();
                Print.tcfo(curThreadName() + " 释放了, 锁为：" + lock2Name);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            lock1.unlock();
            Print.tcfo(curThreadName() + " 释放了, 锁为：" + lock1Name);
        }
    }

    //演示代码：使用两把锁, 通过不可中断的方式抢锁
    public static void useTowLock(Lock lock1, Lock lock2)
    {
        String lock1Name = lock1.toString()
                .replace("java.util.concurrent.locks.", "");

        String lock2Name = lock2.toString()
                .replace("java.util.concurrent.locks.", "");
        Print.tcfo(curThreadName() + " -- 开始抢占锁, 锁为：" + lock1Name);

        lock1.lock();

        Print.tcfo(curThreadName() + " ^-^抢到了, 锁为：" + lock1Name);

        try
        {
            Print.tcfo(curThreadName() + " -- 开始抢占, 锁为：" + lock2Name);

            lock2.lock();

            Print.tcfo(curThreadName() + " ^-^抢到了, 锁为：" + lock2Name);
            try
            {
                Print.tcfo(curThreadName() + "do something ");
                //等待1000ms
                Thread.sleep(10000);
            } catch (Exception e)
            {
                e.printStackTrace();
            } finally
            {
                lock2.unlock();
                Print.tcfo(curThreadName() + " 释放了, 锁为：" + lock2Name);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            lock1.unlock();
            Print.tcfo(curThreadName() + " 释放了, 锁为：" + lock1Name);
        }
    }

    //演示代码：使用两把锁, 通过限时等待的方式抢锁
    public static void tryTowLock(Lock lock1, Lock lock2)
    {
        String lock1Name = lock1.toString()
                .replace("java.util.concurrent.locks.", "");

        String lock2Name = lock2.toString()
                .replace("java.util.concurrent.locks.", "");
        Print.tcfo(curThreadName() + " -- 开始抢占外部锁, 锁为：" + lock1Name);

        boolean lock1Succeed = false;
        boolean lock2Succeed = false;
        try
        {
            //等待一个10s秒的随机数
            int lock1Wait = RandomUtil.randInMod(10);
            lock1Succeed = lock1.tryLock(lock1Wait, TimeUnit.SECONDS);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        if (lock1Succeed)
        {
            Print.tcfo(curThreadName() + " ^-^抢到了外部锁, 锁为：" + lock1Name);
        } else
        {
            Print.tcfo(curThreadName() + " @-@超时中断，抢占外部锁失败, 锁为：" + lock1Name);
            return;
        }
        try
        {
            Print.tcfo(curThreadName() + " -- 开始抢占内部锁, 锁为：" + lock2Name);
            try
            {
                //等待一个10s秒的随机数
                int lock2Wait = RandomUtil.randInMod(10);
                lock2Succeed = lock2.tryLock(lock2Wait, TimeUnit.SECONDS);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            if (lock2Succeed)
            {
                Print.tcfo(curThreadName() + " ^-^抢到了内部锁, 锁为：" + lock2Name);
            } else
            {
                Print.tcfo(curThreadName() + " @-@超时中断，抢占内部锁失败, 锁为：" + lock2Name);
                return;
            }
            try
            {
                Print.tcfo(curThreadName() + " do something ");
                //等待1000ms
                Thread.sleep(1000);
            } catch (Exception e)
            {
                e.printStackTrace();
            } finally
            {
                lock2.unlock();
                Print.tcfo(curThreadName() + " 释放了内部锁, 锁为：" + lock2Name);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            lock1.unlock();
            Print.tcfo(curThreadName() + " 释放了外部锁, 锁为：" + lock1Name);
        }
    }

}
