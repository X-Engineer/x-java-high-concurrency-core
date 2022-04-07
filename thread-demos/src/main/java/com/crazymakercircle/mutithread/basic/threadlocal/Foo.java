package com.crazymakercircle.mutithread.basic.threadlocal;

import com.crazymakercircle.util.RandomUtil;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Foo {
    public ThreadLocal<Foo> fooThreadLocal = ThreadLocal.withInitial(() -> new Foo());
    public String bar;

    public Foo() {
        this.bar = "bar_" + RandomUtil.randInMod(100);
    }


    /**
     * 利用反射获取ThreadLocal中的 Foo 值
     */
    public static Object threadLocalGet(Thread t) {
        try {
            // Thread
            Field field = ReflectionUtils.findField(Thread.class, "threadLocals");
            field.setAccessible(true);
            Object localMap = ReflectionUtils.getField(field, t);

            // ThreadLocalMap.Entry[]
            Field entryField = ReflectionUtils.findField(localMap.getClass(), "table");
            entryField.setAccessible(true);
            Object[] entries = (Object[]) ReflectionUtils.getField(entryField, localMap);

            List<Object> list = new ArrayList<>(entries.length);
            for (Object entry : entries) {
                if (entry != null) {
                    list.add(entry);
                }
            }

            List<Object> result = new ArrayList<>(entries.length);
            for (Object o : list) {

                // Entry.value
                Field entryValue = ReflectionUtils.findField(o.getClass(), "value");
                entryValue.setAccessible(true);
                Object value = ReflectionUtils.getField(entryValue, o);
                if (value instanceof Foo) {
                    result.add(value);
                }
            }
            return result.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 利用反射获取ThreadLocal中的 Foo 对应的Key
     */
    public static Object threadLocalKey(Thread t) {
        try {
            // Thread
            Field field = ReflectionUtils.findField(Thread.class, "threadLocals");
            field.setAccessible(true);
            Object localMap = ReflectionUtils.getField(field, t);

            // ThreadLocalMap.Entry[]
            Field entryField = ReflectionUtils.findField(localMap.getClass(), "table");
            entryField.setAccessible(true);
            Object[] entries = (Object[]) ReflectionUtils.getField(entryField, localMap);

            List<Object> list = new ArrayList<>(entries.length);
            for (Object entry : entries) {
                if (entry != null) {
                    list.add(entry);
                }
            }

            List<Object> result = new ArrayList<>(entries.length);
            for (Object o : list) {

                // Entry.value
                Field entryValue = ReflectionUtils.findField(o.getClass(), "value");
                entryValue.setAccessible(true);
                // Entry.key
                Field entryKey = ReflectionUtils.findField(o.getClass(), "referent");
                entryKey.setAccessible(true);
                Object value = ReflectionUtils.getField(entryValue, o);
                if (value instanceof Foo) {
                    Object key = ReflectionUtils.getField(entryKey, o);
                    result.add(key);
                }
            }
            return result.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
