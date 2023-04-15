package com.crazymakercircle.innerlock;

import com.crazymakercircle.util.ByteUtil;
import com.crazymakercircle.util.Print;
import org.junit.Test;
import org.openjdk.jol.info.ClassLayout;

import java.util.Arrays;

/**
 * Java 对象结构与内置锁
 * Java内置锁的很多重要信息都存放在对象结构中。作为铺垫，在介绍Java内置锁之前，先为大家介绍一下Java对象结构。
 * Java对象（Object实例）结构包括三部分：对象头、对象体和对齐字节。
 * 对象头包括三个字段：
 * - 第一个字段叫作Mark Word（标记字），用于存储自身运行时的数据，例如GC标志位、哈希码、锁状态等信息。
 * - 第二个字段叫作Class Pointer（类对象指针），用于存放方法区Class对象的地址，虚拟机通过这个指针来确定这个对象是哪个类的实例。
 * - 第三个字段叫作Array Length（数组长度）。如果对象是一个Java数组，那么此字段必须有，用于记录数组长度的数据；如果对象不是一个Java数组，那么此字段不存在，所以这是一个可选字段。
 * 对象体：
 * 对象体包含对象的实例变量（成员变量），用于成员属性值，包括父类的成员属性值。这部分内存按4字节对齐。
 * 对齐字段：
 * 对齐字节也叫作填充对齐，其作用是用来保证Java对象所占内存字节数为8的倍数HotSpot VM的内存管理要求对象起始地址必须是8字节的整数倍。对象头本身是8的倍数，当对象的实例变量数据不是8的倍数时，便需要填充数据来保证8字节的对齐。
 *
 * 使用 JOL 工具查看对象的布局
 * OpenJDK提供的JOL（Java Object Layout）包是一个非常好的工具，可以帮我们在运行时计算某个对象的大小。
 * JOL是分析JVM中对象的结构布局的工具，该工具大量使用了Unsafe、JVMTI来解码内部布局情况，它的分析结果相对比较精准。
 *
 */
public class ObjectLock {
    private Integer amount = 0;

    public void increase() {
        synchronized (this) {
            amount++;
        }
    }

    public Integer getAmount() {
        return amount;
    }



/*

    public  void tryIncrease(long millis)
    {
        Print.fo("抢锁成功");
        long left = millis * 1000L * 1000L;
        long cost = 0;
        while (true)
        {
            synchronized (amount)
            {
                amount++;
            }
            left = left - cost;
            long mark = System.nanoTime();
            if (left <= 0)
            {
                break;
            }
            LockSupport.parkNanos(100);
            cost = System.nanoTime() - mark;

        }
        Print.fo("释放锁成功");
    }
*/

    /**
     * 输出十六进制、小端模式的hashCode
     *
     * @return hashCode
     */
    public String hexHash() {
        //对象的原始 hash code，JAVA 默认为大端模式
        int hashCode = this.hashCode();

        //转成小端模式的字节数组
        byte[] hashCode_LE = ByteUtil.int2Bytes_LE(hashCode);
        System.out.println("hashCode_LE:"+ Arrays.toString(hashCode_LE));
        //转成十六进制形式的字符串
        return ByteUtil.byteToHex(hashCode_LE);
    }

    /**
     * 输出二进制、小端模式的hashCode
     *
     * @return hashCode
     */
    public String binaryHash() {
        //对象的原始 hash code，JAVA 默认为大端模式
        int hashCode = this.hashCode();

        //转成小端模式的字节数组
        byte[] hashCode_LE = ByteUtil.int2Bytes_LE(hashCode);

        StringBuffer buffer = new StringBuffer();
        for (byte b : hashCode_LE) {
            //转成二进制形式的字符串
            buffer.append(ByteUtil.byte2BinaryString(b));
            buffer.append(" ");
        }
        return buffer.toString();
    }

    /**
     * 输出十六进制、小端模式的ThreadId
     *
     * @return threadID_LE
     */
    public String hexThreadId() {
        //当前线程的 threadID，JAVA 默认为大端模式
        long threadID = Thread.currentThread().getId();
//        threadID=threadID<<2;
        //转成小端模式的字节数组
        byte[] threadID_LE = ByteUtil.long2bytes_LE(threadID);

        //转成十六进制形式的字符串
        return ByteUtil.byteToHex(threadID_LE);
    }

    /**
     * 输出二进制、小端模式的ThreadId
     *
     * @return threadID_LE
     */
    public String binaryThreadId() {
        //当前线程的 threadID，JAVA 默认为大端模式
        long threadID = Thread.currentThread().getId();
//        threadID=threadID<<2;
        //转成小端模式的字节数组
        byte[] threadID_LE = ByteUtil.long2bytes_LE(threadID);

        StringBuffer buffer = new StringBuffer();
        for (byte b : threadID_LE) {
            //转成二进制形式的字符串
            buffer.append(ByteUtil.byte2BinaryString(b));
            buffer.append(" ");
        }
        return buffer.toString();
    }

    public void printSelf() {
        // 输出十六进制、小端模式的hashCode
        Print.fo("lock hexHash= " + hexHash());

        // 输出二进制、小端模式的hashCode
        Print.fo("lock binaryHash= " + binaryHash());
        //通过JOL工具获取this的对象布局
        String printable = ClassLayout.parseInstance(this).toPrintable();
        //输出对象布局
        Print.fo("lock = " + printable);

    }

    public void printObjectStruct() {

        String printable = ClassLayout.parseInstance(this).toPrintable();

        //当前线程的 threadID，JAVA 默认为大端模式
//        long threadID = Thread.currentThread().getId();
//         Print.fo("current threadID_BE= " + threadID);
//        Print.fo("current threadID_LE= " + hexThreadId());
//        Print.fo("current binary threadID_LE= " + binaryThreadId());
        Print.fo("lock = " + printable);
        // LockSupport.parkNanos(100);

    }


}

