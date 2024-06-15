package com.wx.wxrpc.core.govern;

import java.util.concurrent.ForkJoinPool;

/**
 * 滑动窗口，记录某个时间窗口内发生的故障次数，如果大于设定值，如30S十次，则判定服务出现故障，将服务隔离
 */
public class SlidingTimeWindow {

    //时间窗口大小
    private final int size;

    private RingBuffer ringBuffer;

    // 出现故障的次数
    private int sum ;

    private long preTime = -1L;

    private long curTime = -1L;

    private int curIdex = 0;

    private int preIdex = 0;
    public SlidingTimeWindow() {
        this(30);
    }

    public SlidingTimeWindow(int size) {
        this.size = size;
        ringBuffer = new RingBuffer(size);
    }

    //故障计数
    public void record(long ms){
        long s = ms / 1000L;
        if (preTime == -1L){
            //第一次出现故障,下标从0开始
            preTime = s;
            preIdex = 0;
            ringBuffer.incrByIndex(preIdex,1);
        }else if(s > preTime && s < preTime + size){
            int offset = (int)(s - preTime);
            // start = preidex + 1 ,endidex = preidex + offset
            ringBuffer.reset(preIdex+1,preIdex+offset,0);
            ringBuffer.incrByIndex(preIdex+offset,1);
            preTime = s;
            preIdex = (preIdex + offset) % size;
        }else {
            //全部清空
            ringBuffer.reset();
        }
        //此时的故障次数记录
        sum = ringBuffer.sum();
    }

    //获取故障次数
    public int getSum() {
        return sum;
    }
}
