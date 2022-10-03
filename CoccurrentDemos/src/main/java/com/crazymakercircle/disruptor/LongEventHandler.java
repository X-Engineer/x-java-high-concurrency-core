package com.crazymakercircle.disruptor;

import com.lmax.disruptor.EventHandler;

/**
     * 类似于消费者
     * disruptor会回调此处理器的方法
     */
     class LongEventHandler implements EventHandler<LongEvent> {
        @Override
        public void onEvent(LongEvent longEvent, long l, boolean b) throws Exception {
            System.out.println(longEvent.getValue());
        }
    }