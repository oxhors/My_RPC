package com.wx.wxrpc.core.filter.filterchain;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.wx.wxrpc.core.entity.RpcRequest;
import com.wx.wxrpc.core.entity.RpcResponse;
import com.wx.wxrpc.core.filter.Filter;
import com.wx.wxrpc.core.filter.impl.BaseFilter;
import org.springframework.validation.ObjectError;

import java.util.List;
import java.util.Objects;

//责任链设计模式
public class FilterChainService {
    private List<BaseFilter> filterChain;

    public FilterChainService(List<BaseFilter> filterChain) {
        this.filterChain = filterChain;
        filterChain.sort((f1,f2) ->{
            return f1.getOrder() - f2.getOrder();
        });
        // 在责任链中将过滤器按照指定的顺序从小到大排序
    }

    public Object preFilter(RpcRequest request){
        Object res = null;
        for (BaseFilter filter : filterChain) {
            res = filter.preFilter(request);
        }
        if(Objects.nonNull(res)){
            return res;
        }
        return null;
    }

    public void postFilter(RpcRequest request, RpcResponse response,Object res){
        for(BaseFilter filter : filterChain){
            //将结果传递给下一个过滤器
            res = filter.postFilter(request, response, res);
        }
    }
}
