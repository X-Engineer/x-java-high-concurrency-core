package com.crazymakercircle.designmodel.singleton;

public class Singleton {
    //静态内部类 
    private static class LazyHolder { 
          //通过final保障初始化时的线程安全  
           private static final Singleton INSTANCE = new Singleton(); 
    } 
       //私有的构造器 
    private Singleton (){} 
      //获取单例的方法 
    public static final Singleton getInstance() { 
      //返回内部类的静态、最终成员 
       return LazyHolder.INSTANCE; 
    } 
} 