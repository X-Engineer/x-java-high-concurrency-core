package com.crazymakercircle.designmodel.singleton;
//饿汉式
public enum SingletonEnumStyle {
    INSTANCE;
    // 类初始化时,会立即加载该对象，线程安全,调用效率高

    public  static SingletonEnumStyle getInstance() {
        return INSTANCE;
    }

}