package com.crazymakercircle.visiable;

/**
 * 为了解决CPU访问主存时主存读写性能的短板，在CPU中增加了高速缓存，但这带来了可见性问题。
 * 而Java的volatile关键字可以保证共享变量的主存可见性，也就是将共享变量的改动值立即刷新回主存。在正常情况下，系统操作并不会校验共享变量的缓存一致性，只有当共享变量用volatile关键字修饰了，该变量所在的缓存行才被要求进行缓存一致性的校验
 *
 *
 */
public class VolatileVar {
    //使用volatile保障内存可见性
    volatile int var = 0;

    public void setVar(int var) {
        System.out.println("setVar = " + var);
        this.var = var;
    }

    public static void main(String[] args) {
        VolatileVar var = new VolatileVar();
        var.setVar(100);
    }
}