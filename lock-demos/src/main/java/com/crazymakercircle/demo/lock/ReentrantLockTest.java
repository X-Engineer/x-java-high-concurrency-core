package com.crazymakercircle.demo.lock;

import com.crazymakercircle.util.Print;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockTest
{
    Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public void testMethod()
    {

        try
        {
            lock.lock();
            Print.tcfo("开始wait");
            condition.await();

        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } finally
        {
            lock.unlock();
        }
    }

    public void signal()
    {
        try
        {
            lock.lock();
            condition.signal();
        } finally
        {
            lock.unlock();
        }
    }


}
