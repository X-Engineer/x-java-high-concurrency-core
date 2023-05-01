package com.crazymakercircle.visiable;

/**
 * 从前面的规则可以知道：如果第二个操作为volatile写，无论第一个操作是什么都不能重排序。拿上面的代码来说，由于代码②为写入flag（volatile变量）操作，因此代码①不会被重排序到代码②的后面。
 * 从前面的规则可以知道：如果第一个操作为volatile读，无论第二个操作是什么都不能重排序。拿上面的代码来说，由于代码③为读取flag（volatile变量），因此代码④不会被重排序到代码③之前。
 */
class VolatileReorderDemo2
{
    int x = 10;
    int doubleValue = 0;
    volatile  boolean flag = false;
    public void update()
    {
        x = 100;      //①
        flag = true;      //②
    }
    public void doubleX()
    {
        if (flag)            //③
        {
            doubleValue = x + x;  //④
        }
    }
}