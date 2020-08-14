package com.crazymakercircle.basic.demo.cas.busi;
import sun.misc.Contended;
public class ContendedDemo
{
    //有填充的演示成员
    @Contended
    public volatile long padVar;

    //没有填充的演示成员
    public volatile long notPadVar;
}