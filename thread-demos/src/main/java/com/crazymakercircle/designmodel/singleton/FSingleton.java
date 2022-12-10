package com.crazymakercircle.designmodel.singleton;
//饿汉式
public class FSingleton {

    // 类初始化时,会立即加载该对象，线程安全,调用效率高
    private static final FSingleton instance = new FSingleton();

    // 私有化构造方法
    private FSingleton() {
    }

    public static FSingleton getInstance() {
        return instance;
    }


}