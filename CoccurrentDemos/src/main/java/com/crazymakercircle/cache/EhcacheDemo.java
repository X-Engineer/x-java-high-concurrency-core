package com.crazymakercircle.cache;


import com.crazymakercircle.im.common.bean.User;
import com.crazymakercircle.util.IOUtil;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

import java.io.InputStream;

import static com.crazymakercircle.util.IOUtil.getResourcePath;

public class EhcacheDemo {
    public static void main(String[] args) {


        // 1. 创建缓存管理器
        String inputStream= getResourcePath( "ehcache.xml");
        CacheManager cacheManager = CacheManager.create(inputStream);



        // 2. 获取缓存对象
        Cache cache = cacheManager.getCache("HelloWorldCache");
        CacheConfiguration config = cache.getCacheConfiguration();
        config.setTimeToIdleSeconds(60);
        config.setTimeToLiveSeconds(120);



        // 3. 创建元素
        Element element = new Element("key1", "value1");
         
        // 4. 将元素添加到缓存
        cache.put(element);
         
        // 5. 获取缓存
        Element value = cache.get("key1");
        System.out.println("value: " + value);
        System.out.println(value.getObjectValue());
         
        // 6. 删除元素
        cache.remove("key1");
         
        User user = new User("1000", "Javaer1");
        Element element2 = new Element("user", user);
        cache.put(element2);
        Element value2 = cache.get("user");
        System.out.println("value2: "  + value2);
        User user2 = (User) value2.getObjectValue();
        System.out.println(user2);
         
        System.out.println(cache.getSize());
         
        // 7. 刷新缓存
        cache.flush();
         
        // 8. 关闭缓存管理器
        cacheManager.shutdown();
 
    }
}