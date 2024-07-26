package com.oneinstep.myrpc.core.client;

import com.oneinstep.myrpc.core.dto.RpcRequest;
import com.oneinstep.myrpc.core.dto.RpcResponse;
import com.oneinstep.myrpc.core.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * RPC service proxy
 * Create a proxy for the RPC service
 */
@Slf4j
public class RpcServiceProxy {

    /**
     * Service registry
     */
    private final ServiceRegistry serviceRegistry;

    public RpcServiceProxy(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                (proxy, method, args) -> {
                    // 创建一个 RPC 请求对象并填充必要的信息
                    RpcRequest rpcRequest = new RpcRequest();
                    // 设置请求 ID
                    rpcRequest.setRequestId(UUID.randomUUID().toString());
                    // 设置请求的类名、方法名、参数类型、参数值
                    rpcRequest.setClassName(method.getDeclaringClass().getName());
                    rpcRequest.setMethodName(method.getName());
                    rpcRequest.setParameterTypes(method.getParameterTypes());
                    rpcRequest.setParameters(args);

                    // 从注册中心获取服务地址
                    String serviceName = method.getDeclaringClass().getName();
                    log.info("Discover service: {}", serviceName);
                    String serviceAddress = serviceRegistry.discover(serviceName);
                    log.info("Service address: {}", serviceAddress);

                    if (serviceAddress == null) {
                        log.error("Service not found: {}", serviceName);
                        throw new RuntimeException("Service not found: " + serviceName);
                    }

                    // 解析主机名与端口号
                    String[] addressArr = serviceAddress.split(":");
                    if (addressArr.length != 2) {
                        log.error("Invalid service address format: {}", serviceAddress);
                        throw new RuntimeException("Invalid service address format: " + serviceAddress);
                    }
                    String host = addressArr[0];
                    int port = Integer.parseInt(addressArr[1]);

                    // 使用 RPC 客户端发送请求
                    RpcClient rpcClient = new RpcClient();
                    RpcResponse rpcResponse = rpcClient.send(rpcRequest, host, port);

                    if (rpcResponse.getError() != null) {
                        log.error("RPC Error: {}", rpcResponse.getError());
                        throw new RuntimeException("RPC Error: " + rpcResponse.getError());
                    }

                    // 返回 RPC 响应结果、
                    Object result = rpcResponse.getResult();
                    log.info("RPC Response: {}", result);

                    // 返回 RPC 响应结果
                    return rpcResponse.getResult();
                }
        );
    }
}
