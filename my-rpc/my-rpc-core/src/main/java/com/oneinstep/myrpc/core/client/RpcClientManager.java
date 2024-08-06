package com.oneinstep.myrpc.core.client;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * RPC client manager
 */
@Component
public class RpcClientManager implements DisposableBean {

    /**
     * RPC 客户端缓存
     */
    private final ConcurrentMap<String, RpcClient> clientMap = new ConcurrentHashMap<>();

    /**
     * 获取客户端
     *
     * @param host 主机
     * @param port 端口
     * @return RPC 客户端
     */
    public RpcClient getClient(String host, int port) {
        String key = host + ":" + port;
        return clientMap.computeIfAbsent(key, k -> new RpcClient(host, port));
    }

    /**
     * 关闭所有客户端
     */
    public void closeAllClients() {
        clientMap.values().forEach(RpcClient::close);
    }

    /**
     * 销毁方法
     */
    @Override
    public void destroy() {
        closeAllClients();
    }

}
