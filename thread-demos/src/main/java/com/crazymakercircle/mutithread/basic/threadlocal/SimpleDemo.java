package com.crazymakercircle.mutithread.basic.threadlocal;

import com.crazymakercircle.util.ThreadUtil;

public class SimpleDemo {
    static class DemoObject {
        private ThreadLocal<Integer> threadLocal = new ThreadLocal<Integer>() {
            Integer integer;

            @Override
            protected Integer initialValue() {
                return 0;
            }
        };

        public Integer get() {
            return threadLocal.get();
        }

        public void set(Integer integer) {
            threadLocal.set(threadLocal.get() + integer);
        }
    }

    public static void main(String[] args) {
        DemoObject demoObject = new DemoObject();
        for (int i = 1; i <= 100; i++) {
            new Thread(() -> {
                for (int j = 1; j <= 100; j++) demoObject.set(j);
                System.out.println("demoClass.get() = " + demoObject.get());
            }).start();
        }
        ThreadUtil.sleepSeconds(Integer.MAX_VALUE);
    }
}
