package com.crazymakercircle.singleton.busi;

public class FSingleton
{
    static final FSingleton instance = new FSingleton();
    // 私有化构造方法
    private FSingleton()
    {
    }
    static FSingleton getInstance()
    {
        return instance;
    }


}