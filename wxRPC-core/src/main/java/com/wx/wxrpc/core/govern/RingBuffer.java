package com.wx.wxrpc.core.govern;

import java.util.Arrays;

/**
 * 环形数组用来作为统计故障次数的容器
 */
public class RingBuffer {

    private final int[] buffer;
    private final int size;

    public RingBuffer(int size) {
        this.size = size;
        buffer = new int[size];
    }

    /**
     * 根据某一个下标进行次数记录
     */
    public void incrByIndex(int idex,int val){
        buffer[idex % size] += val;
    }

    /**
     * 范围置位
     * @param start
     * @param end
     * @param val
     */
    // 0 1 2 3 4 5
    // 1- 5 ---》 1 2 3 4
    // 5 - 1 --- > 5 0
    public void reset(int start,int end,int val){
        start %= size;
        end %= size;
        if(start < end){
            Arrays.fill(buffer,start,end,val);
        } else if (start > end) {
            Arrays.fill(buffer,0,end,val);
            Arrays.fill(buffer,start,size,val);
        }
    }
    public void reset(){
        Arrays.fill(buffer,0,size,0);
    }

    //返回这个时间窗口内的计数
    public int sum(){
        return Arrays.stream(buffer).sum();
    }
}
