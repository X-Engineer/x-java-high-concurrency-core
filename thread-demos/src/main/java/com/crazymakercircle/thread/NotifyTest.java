package com.crazymakercircle.thread;

import com.crazymakercircle.util.ThreadUtil;
import org.junit.Test;

public class NotifyTest {

    Object lock=new Object();


    @Test
    public void testNotify()
    {
        synchronized (lock)
        {
            lock.notify();
        }

        ThreadUtil.sleepMilliSeconds(1000000);
    }
}
