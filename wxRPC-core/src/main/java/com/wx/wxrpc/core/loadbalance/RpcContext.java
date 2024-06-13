package com.wx.wxrpc.core.loadbalance;

import com.wx.wxrpc.core.filter.Filter;

import java.util.List;

public class RpcContext {
    private List<Filter> filters;
    private LoadBalance loadBalance;
    private Router router;

    public RpcContext(LoadBalance loadBalance, Router router,List<Filter> filters) {
        this.loadBalance = loadBalance;
        this.router = router;
        this.filters = filters;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public LoadBalance getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    public Router getRouter() {
        return router;
    }

    public void setRouter(Router router) {
        this.router = router;
    }
}
