package com.crazymakercircle.singleton.busi;

public class BSingleton
{
    static BSingleton instance;

    // 私有化构造方法
    private BSingleton()
    {
    }

    static synchronized BSingleton getInstance()
    {
        if (instance == null) //①②③⑤⑦④
        {
            instance = new BSingleton();  //①②③⑤⑦④
        }
        return instance;
    }
}