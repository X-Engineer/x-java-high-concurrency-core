package com.crazymakercircle.designmodel.singleton;

//懒汉模式
public class FLazySingleton {

    //类初始化时，不会初始化该对象，真正需要使用的时候才会创建该对象。
    private static FLazySingleton instance = null;

    // 私有化构造方法
    private FLazySingleton() {
    }

    //真正需要使用的时候才会创建该对象
    public static synchronized FLazySingleton getInstance() {
        if (null == instance) {
            instance = new FLazySingleton();
        }
        return instance;
    }


}