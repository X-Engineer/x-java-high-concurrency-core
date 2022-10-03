package com.crazymakercircle.falseShare;

public class CacheLineWithPadding implements SomeOneEntity {

    //模拟  disruptor 的 Sequence

    protected volatile long p1, p2, p3, p4, p5, p6, p7;

    public volatile long x = 1L;

    protected volatile long p9, p10, p11, p12, p13, p14, p15;


    @Override
    public void setValue(long value) {
        x = value;
    }

    /**
     * 疯狂创客圈 社群 有小伙伴担心，被 jvm 优化掉了
     * <p>
     * 下面的代码，就是防止被 GC 优化
     * <p>
     * To prevent GC optimizations for cleaning unused padded references
     */
    public long sumPaddingToPreventOptimization() {
        return p1 + p2 + p3 + p4 + p5 + p6 + p9 + p10 + p11 + p12 + p13 + p14 + p15;
    }
}