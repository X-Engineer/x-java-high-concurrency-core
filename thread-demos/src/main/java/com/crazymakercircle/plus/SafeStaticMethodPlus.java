package com.crazymakercircle.plus;

/**
 * Created by 尼恩@疯狂创客圈.
 * 所有的类都是在第一次使用时被动态加载到JVM中的（懒加载），其各个类都是在必需时才加载的。这一点与许多传统语言（如C++）都不同，
 * JVM为动态加载机制配套了一个判定一个类是否已经被加载的检查动作，使得类加载器首先检查这个类的Class对象是否已经被加载。
 * 如果尚未加载，类加载器就会根据类的全限定名查找.class文件，验证后加载到JVM的方法区内存，并构造其对应的Class对象。
 *
 * 静态方法属于Class实例而不是单个Object实例，在静态方法内部是不可以访问Object实例的this引用（也叫指针、句柄）的。所以，
 * 修饰static方法的synchronized关键字就没有办法获得Object实例的this对象的监视锁。
 * 实际上，使用synchronized关键字修饰static方法时，synchronized的同步锁并不是普通Object对象的监视锁，而是类所对应的Class对象的监视锁。
 *
 * 为了以示区分，这里将Object对象的监视锁叫作对象锁，将Class对象的监视锁叫作类锁。当synchronized关键字修饰static方法时，同步锁为类锁；
 * 当synchronized关键字修饰普通的成员方法（非静态方法）时，同步锁为对象锁。由于类的对象实例可以有很多，但是每个类只有一个Class实例，
 * 因此使用类锁作为synchronized的同步锁时会造成同一个JVM内的所有线程只能互斥地进入临界区段。所以，使用synchronized关键字修饰static方法是非常粗粒度的同步机制。
 *
 * 通过synchronized关键字所抢占的同步锁什么时候释放呢？
 * 一种场景是synchronized块（代码块或者方法）正确执行完毕，监视锁自动释放；另一种场景是程序出现异常，非正常退出synchronized块，监视锁也会自动释放。
 * 所以，使用synchronized块时不必担心监视锁的释放问题。
 */
public class SafeStaticMethodPlus {
    private static Integer amount = 0;

    public static synchronized void selfPlus() {

        amount++;

    }

    public Integer getAmount() {
        return amount;
    }


}

