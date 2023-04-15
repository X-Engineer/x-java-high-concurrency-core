package com.crazymakercircle.innerlock;

import com.crazymakercircle.util.Print;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.vm.VM;

/**
 * @project: x-java-high-concurrency-core
 * @ClassName: ArrayLockTest
 * @author: nzcer
 * @creat: 2023/4/15 11:07
 * @description: 测试对象头中的 Array Length
 */
public class ArrayLockTest {
    public static void main(String[] args) {
        Print.tcfo(VM.current().details());
        ArrayObject aobj = new ArrayObject(3);
        //ArrayObject bobj = new ArrayObject(4);
        //ArrayObject cobj = new ArrayObject(5);
        ArrayObject[] arr = new ArrayObject[3];
        arr[0] = aobj;
        //arr[1] = bobj;
        //arr[2] = cobj;
        //aobj.printSelf();
        String printable = ClassLayout.parseInstance(arr).toPrintable();
        Print.fo("lock = " + printable);
        /**
         * 前 8 个字节属于对象头的 MarkWord 字段
         * 3e 13 01 f8 这 4 个字节对应对象头的 class pointer（被压缩成 32 位了）
         * 03 00 00 00 这 4 个字节对应对象头的 Array Length（数组长度为 3）
         * 接下来 12 个字节表示,数组长度为 3，每个元素占 4 字节，故总共 12 字节
         * 最后 4 个字节用于 8 字节对齐（前面： 8+4+4+12=28，故填充 4 字节达到 32 字节，是 8 的倍数）
         */
        // [ArrayLockTest.main]：lock = [Lcom.crazymakercircle.innerlock.ArrayObject; object internals:
        // OFFSET  SIZE                                         TYPE DESCRIPTION                               VALUE
        //      0     4                                              (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
        //      4     4                                              (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
        //      8     4                                              (object header)                           3e 13 01 f8 (00111110 00010011 00000001 11111000) (-134147266)
        //     12     4                                              (object header)                           03 00 00 00 (00000011 00000000 00000000 00000000) (3)
        //     16    12   com.crazymakercircle.innerlock.ArrayObject ArrayObject;.<elements>                   N/A
        //     28     4                                              (loss due to the next object alignment)
        //Instance size: 32 bytes
        //Space losses: 0 bytes internal + 4 bytes external = 4 bytes total
    }
}

class ArrayObject {
    int length;
    public ArrayObject(int length) {
        this.length = length;
    }

    public void printSelf() {
        String printable = ClassLayout.parseInstance(this).toPrintable();
        Print.fo("lock = " + printable);
    }
}