package com.crazymakercircle.mutithread.basic.use;

import com.crazymakercircle.util.Print;

import static com.crazymakercircle.util.ThreadUtil.sleepSeconds;

/**
 * “等待-通知”通信模式演示案例：
 *
 * Java的“等待-通知”机制是指：
 * 一个线程A调用了同步对象的wait()方法进入等待状态，而另一线程B调用了同步对象的notify()或者notifyAll()方法通知等待线程，当线程A收到通知后，重新进入就绪状态，准备开始执行。
 *
 * 在示例程序执行过程中，WaitThread首先调用locko.wait()等待被通知并且进入阻塞状态，释放locko的Owner权利，然后NotifyThread可以获取locko的Owner权利，进入临界区执行。NotifyThread的临界区代码首先从屏幕读取用于输入，目的是阻塞NotifyThread线程，方便使用jstack查看线程状态。
 * 运行以上程序，在屏幕中输入任意内容之前，结合使用jps与jstack两个指令查看线程的状态
 * 此时通过jps指令查看到WaitNotifyDemo进程的Id为25192，然后使用jstack 25192指令查看WaitThread、NotifyThread两个线程的状态：WaitThread的状态为WAITING，NotifyThread的状态为RUNNABLE。
 * 为什么WaitThread的状态为WAITING呢？此时WaitThread处于locko的监视器的WaitSet（等待集）中，等待被唤醒。
 *
 * 通过实例的演示目前已经知道：WaitThread线程调用locko.wait后会一直处于WAITING状态，不会再占用CPU的时间片，
 * 也不会占用同步对象locko的监视器，一直到其他线程使用locko.notify方法发出通知。
 *
 */
public class WaitNotifyDemo {
    static Object locko = new Object();

    //等待线程
    static class WaitTarget implements Runnable {
        public void run() {
            //加锁
            synchronized (locko) {
                try {
                    //启动等待，同时释放locko监视器的Owner权限
                    Print.tco("启动等待");
                    //等待被通知，同时释放locko监视器的Owner权限
                    locko.wait();
                    //收到通知后，线程会进入locko监视器的EntryList
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //条件满足
                Print.tco("收到通知，当前线程继续执行");
            }
        }
    }

    //通知线程
    static class NotifyTarget implements Runnable {
        public void run() {
            //加锁
            synchronized (locko) {
                //从屏幕读取输入，目的阻塞通知线程，方便使用jstack查看线程状态
                Print.consoleInput();
                //获取lock锁，然后进行发送
                // 此时不会立即释放locko的Monitor的Owner，需要该线程执行完毕
                locko.notifyAll();
                Print.tco("发出通知了，但是线程还没有立马释放锁");
            }
        }
    }


    public static void main(String[] args) throws InterruptedException {
        //创建等待线程
        Thread waitThread = new Thread(new WaitTarget(), "WaitThread");
        //启动等待线程
        waitThread.start();
        // 主线程延迟，确保等待线程先执行，防止后面通知线程先一步于等待线程执行
        sleepSeconds(1);
        //创建通知线程
        Thread notifyThread = new Thread(new NotifyTarget(), "NotifyThread");
        //启动通知线程
        notifyThread.start();

    }

}