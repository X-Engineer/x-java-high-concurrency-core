package com.crazymakercircle.visiable;

/**
 * volatile规则的具体内容：对一个volatile变量的写先行发生（Happens-Before）于任意后续对这个volatile变量的读。
 * 1.如果第二个操作为volatile写，无论第一个操作是什么都不能重排序，这就确保了volatile写之前的操作不会被重排序到自己之后
 * 2.如果第一个操作为volatile读，无论第二个操作是什么都不能重排序，这确保了volatile读之后的操作不会被重排序到自己的前面
 *
 * 假设线程A执行update()方法，线程B执行doubleX()方法，因为代码①和②没有数据依赖关系，所以①和②可能被重排序，它们在重排序后的次序为：
 *      flag = true;    //②
 *      x = 100;         //①
 * 线程A执行重排之后的代码，在完成语句②（flag=true）但没有开始语句①（x=100）时，假设线程B开始执行doubleX()方法，将两个x（此时值仍然为10）累加，得到的doubleValue为20。
 * 为了获取正确的结果，必须阻止代码重排，为以上代码的flag成员属性增加volatile修饰，得到 VolatileReorderDemo2
 */
class VolatileReorderDemo
{
    int x = 10;
    int doubleValue = 0;
    boolean flag = false;
    public void update()
    {
        x = 100;      //①
        flag = true;      //②
    }
    public void doubleX()
    {
        if (flag)           //③
        {
            doubleValue = x + x;
        }
    }
}