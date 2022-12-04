package com.crazymakercircle.cache;

import com.crazymakercircle.util.Logger;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LruDemo {


    @Test
    public  void testSimpleLRUCache() {

        SimpleLRUCache cache = new SimpleLRUCache( 2 /* 缓存容量 */ );
        cache.put(1, 1);
        cache.put(2, 2);
        Logger.cfo(cache.get(1));       // 返回  1
        cache.put(3, 3);    // 该操作会使得 2 淘汰
        Logger.cfo(cache.get(2));        // 返回 -1 (未找到)
        cache.put(4, 4);    // 该操作会使得 1 淘汰
        Logger.cfo(cache.get(1));        // 返回 -1 (未找到)
        Logger.cfo(cache.get(3));        // 返回  3
        Logger.cfo(cache.get(4));        // 返回  4
    }

    @Test
    public  void testLRUCache() {

        LRUCache cache = new LRUCache( 2 /* 缓存容量 */ );
        cache.put(1, 1);
        cache.put(2, 2);
        Logger.cfo(cache.get(1));       // 返回  1
        cache.put(3, 3);    // 该操作会使得 2 淘汰
        Logger.cfo(cache.get(2));        // 返回 -1 (未找到)
        cache.put(4, 4);    // 该操作会使得 1 淘汰
        Logger.cfo(cache.get(1));        // 返回 -1 (未找到)
        Logger.cfo(cache.get(3));        // 返回  3
        Logger.cfo(cache.get(4));        // 返回  4
    }

    

    static class SimpleLRUCache extends LinkedHashMap<Integer, Integer> {
        private int capacity;

        public SimpleLRUCache(int capacity) {
            super(capacity, 0.75F, true);
            this.capacity = capacity;
        }

        public int get(int key) {
            return super.getOrDefault(key, -1);
        }

        public void put(int key, int value) {
            super.put(key, value);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
            return size() > capacity;
        }
    }

    static  private class Entry {
        private int key;
        private int value;
        private Entry before;
        private Entry after;

        public Entry() {
        }

        public Entry(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    static  class LRUCache {
        //map容器 ，空间换时间，保存key对应的CacheNode，保证用O(1) 的时间获取到value
        private Map<Integer, Entry> cacheMap = new HashMap<Integer, Entry>();
        // 最大容量
        private int capacity;
        /**
         * 通过双向指针来保证数据的插入更新顺序，以及队尾淘汰机制
         */
        //头指针
        private Entry head;
        //尾指针
        private Entry tail;

        //容器大小
        private int size;


        /**
         * 初始化双向链表，容器大小
         */
        public LRUCache(int capacity) {
            this.capacity = capacity;
            head = new Entry();
            tail = new Entry();
            head.after = tail;
            tail.before = head;
        }

        public int get(int key) {
            Entry node = cacheMap.get(key);
            if (node == null) {
                return -1;
            }
            // node != null,返回node后需要把访问的node移动到双向链表头部
            moveToBack(node);
            return node.value;
        }

        public void put(int key, int value) {
            Entry node = cacheMap.get(key);
            if (node == null) {
                //缓存不存在就新建一个节点，放入Map以及双向链表的头部
                Entry newNode = new Entry(key, value);
                cacheMap.put(key, newNode);
                addToBack(newNode);
                size++;
                //如果超出缓存容器大小，就移除队首元素
                if (size > capacity) {
                    Entry removeNode = removeFirst();
                    cacheMap.remove(removeNode.key);
                    size--;
                }
            } else {
                //如果已经存在，就把node移动到头部。
                node.value = value;
                moveToBack(node);
            }
        }

        /**
         * 移动节点到尾部：
         * 1、删除节点
         * 2、把节点添加到尾部
         */
        private void moveToBack(Entry node) {
            removeNode(node);
            addToBack(node);
        }

        /**
         * 移除队首元素
         */
        private Entry removeFirst() {
            Entry node = head.after;
            removeNode(node);
            return node;
        }

        private void removeNode(Entry node) {

            node.before.after = node.after;
            node.after.before = node.before;
        }

        /**
         * 把节点添加到尾部
         */
        private void addToBack(Entry node) {
            head.after.before = node;
            node.after = head.after;
            head.after = node;
            node.before = head;
        }
    }
}
