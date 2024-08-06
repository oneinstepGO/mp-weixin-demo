package com.oneinstep.myrpc.core.client;

import com.oneinstep.myrpc.core.dto.RpcRequest;
import com.oneinstep.myrpc.core.dto.RpcResponse;
import com.oneinstep.myrpc.core.exception.RpcException;
import com.oneinstep.myrpc.core.exception.ServiceNotFoundException;
import com.oneinstep.myrpc.core.registry.ServiceRegistry;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * RPC service proxy
 * Create a proxy for the RPC service
 */
@Slf4j
@Component
public class RpcServiceProxyFactory {

    @Resource
    private ServiceRegistry serviceRegistry;
    @Resource
    private RpcClientManager rpcClientManager;

    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<T> interfaceClass, String version) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                (proxy, method, args) -> {
                    RpcRequest rpcRequest = new RpcRequest();
                    rpcRequest.setRequestId(UUID.randomUUID().toString());
                    rpcRequest.setClassName(method.getDeclaringClass().getName());
                    rpcRequest.setMethodName(method.getName());
                    rpcRequest.setParameterTypes(method.getParameterTypes());
                    rpcRequest.setParameters(args);
                    rpcRequest.setVersion(version);

                    String serviceName = method.getDeclaringClass().getName();
                    log.info("Discover service: {}", serviceName);
                    String serviceAddress = serviceRegistry.discover(serviceName, version);
                    log.info("Service address: {}", serviceAddress);

                    if (serviceAddress == null) {
                        log.error("Service not found: {} , version: {}", serviceName, version);
                        throw new ServiceNotFoundException("Service not found: " + serviceName + ", version: " + version);
                    }

                    String[] addressArr = serviceAddress.split(":");
                    if (addressArr.length != 2) {
                        log.error("Invalid service address format: {}", serviceAddress);
                        throw new RpcException("Invalid service address format: " + serviceAddress);
                    }
                    String host = addressArr[0];
                    int port = Integer.parseInt(addressArr[1]);

                    RpcClient rpcClient = rpcClientManager.getClient(host, port);
                    RpcResponse rpcResponse = rpcClient.send(rpcRequest);

                    if (rpcResponse.getError() != null) {
                        log.error("RPC Error: {}", rpcResponse.getError());
                        throw new RpcException("RPC Error: " + rpcResponse.getError());
                    }

                    Object result = rpcResponse.getResult();
                    log.info("RPC Response: {}", result);

                    return rpcResponse.getResult();
                }
        );
    }
}
