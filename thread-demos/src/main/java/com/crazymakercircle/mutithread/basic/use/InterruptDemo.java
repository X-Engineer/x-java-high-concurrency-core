package com.crazymakercircle.mutithread.basic.use;

import com.crazymakercircle.util.Print;
import org.junit.Test;

import static com.crazymakercircle.util.ThreadUtil.sleepMilliSeconds;
import static com.crazymakercircle.util.ThreadUtil.sleepSeconds;

/**
 * 线程的 Interrupt 操作
 * Java语言提供了stop()方法终止正在运行的线程，但是Java将Thread的stop()方法设置为过时，不建议大家使用。为什么呢？
 * 因为使用stop()方法是很危险的，就像突然关闭计算机电源，而不是按正常程序关机。在程序中，我们是不能随便中断一个线程的，我们无法知道这个线程正运行在什么状态，
 * 它可能持有某把锁，强行中断线程可能导致锁不能释放的问题；或者线程可能在操作数据库，强行中断线程可能导致数据不一致的问题。
 * 正是由于调用stop()方法来终止线程可能会产生不可预料的结果，因此不推荐调用stop()方法。
 * <p>
 * <p>
 * 当我们调用线程的interrupt()方法时，它有两个作用：
 * （1）如果此线程处于阻塞状态（如调用了Object.wait()方法），就会立马退出阻塞，并抛出InterruptedException异常，线程就可以通过捕获InterruptedException来做一定的处理，
 * 然后让线程退出。更确切地说，如果线程被Object.wait()、Thread.join()和Thread.sleep()三种方法之一阻塞，此时调用该线程的interrupt()方法，
 * 该线程将抛出一个InterruptedException中断异常（该线程必须事先预备好处理此异常），从而提早终结被阻塞状态。
 * <p>
 * （2）如果此线程正处于运行之中，线程就不受任何影响，继续运行，仅仅是线程的中断标记被设置为true。所以，程序可以在适当的位置通过调用isInterrupted()方法来查看自己是否被中断，并执行退出操作。
 * <p>
 * 如果线程的interrupt()方法先被调用，然后线程开始调用阻塞方法进入阻塞状态，InterruptedException异常依旧会抛出。
 * 如果线程捕获InterruptedException异常后，继续调用阻塞方法，将不再触发InterruptedException异常
 * Created by 尼恩@疯狂创客圈.
 */

public class InterruptDemo {

    public static final int SLEEP_GAP = 5000;//睡眠时长
    public static final int MAX_TURN = 50;//睡眠次数

    static class SleepThread extends Thread {
        static int threadSeqNumber = 1;

        public SleepThread() {
            super("sleepThread-" + threadSeqNumber);
            threadSeqNumber++;
        }

        public void run() {
            try {
                Print.tco(getName() + " 进入睡眠.");
                // 线程睡眠一会
                Thread.sleep(SLEEP_GAP);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Print.tco(getName() + " 发生被异常打断.");
                return;
            }
            Print.tco(getName() + " 运行结束.");
        }

    }

    public static void main(String args[]) throws InterruptedException {

        Thread thread1 = new SleepThread();
        thread1.start();
        Thread thread2 = new SleepThread();
        thread2.start();
        sleepSeconds(2);//等待2秒
        Print.tco(thread1.getName() + " 的state:" + thread1.getState());
        thread1.interrupt(); //打断线程1
        sleepSeconds(5);//等待5秒
        Print.tco(thread1.getName() + " 的state:" + thread1.getState());
        thread2.interrupt();  //打断线程2，此时线程2已经终止
        sleepSeconds(1);//等待1秒
        Print.tco("程序运行结束.");
    }

    //测试用例：获取异步调用的结果

    /**
     * Thread.interrupt()方法并不像Thread.stop()方法那样中止一个正在运行的线程，其作用是设置线程的中断状态位（为true），至于线程是死亡、等待新的任务还是继续运行至下一步，
     * 就取决于这个程序本身。线程可以不时地检测这个中断标示位，以判断线程是否应该被中断（中断标示值是否为true）。总之，Thread.interrupt()方法只是改变中断状态，、
     * 不会中断一个正在运行的线程，线程是否停止执行，需要用户程序去监视线程的isInterrupted()状态，并进行相应的处理。
     */
    @Test
    public void testInterrupted2() {
        //Thread thread = new Thread() {
        //    public void run() {
        //        Print.tco("线程启动了");
        //        //一直循环
        //        while (true) {
        //            Print.tco(isInterrupted());
        //            sleepMilliSeconds(SLEEP_GAP);
        //
        //            //如果调用 interrupt 为true，退出死循环
        //            if (isInterrupted()) {
        //                Print.tco("线程结束了");
        //                break;
        //                //return;
        //            }
        //        }
        //    }
        //};

        // lambda 表达式重写
        Thread thread = new Thread(() -> {
            Print.tco("线程启动了");
            // 一直循环
            while (true) {
                Print.tco(Thread.currentThread().isInterrupted());
                sleepMilliSeconds(SLEEP_GAP);
                if (Thread.currentThread().isInterrupted()) {
                    Print.tco("线程结束了");
                    return;
                }
            }
        }, "测试 Interrupt 线程");
        thread.start();
        sleepSeconds(2);//等待2秒
        thread.interrupt(); //打断线程1
        sleepSeconds(2);//等待2秒
        thread.interrupt();
    }
}