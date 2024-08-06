package com.oneinstep.myrpc.core.server;

import com.oneinstep.myrpc.core.dto.RpcRequest;
import com.oneinstep.myrpc.core.dto.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 * Processing RPC requests
 * <p>
 * The server-side handler processes the request and returns the result to the client.
 * The handler is responsible for processing the request and returning the result.
 * </p>
 */
@Slf4j
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    /**
     * Service name and handler object mapping
     */
    private final ConcurrentMap<String, Object> handlerMap;

    public RpcServerHandler(ConcurrentMap<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) {
        log.info("Received request: {}", request);

        RpcResponse rpcResponse = new RpcResponse();
        // Set the request ID
        rpcResponse.setRequestId(request.getRequestId());

        try {
            // Handle the request
            Object result = handleRequest(request);
            rpcResponse.setResult(result);
        } catch (Exception e) {
            Throwable cause = e.getCause();
            rpcResponse.setError(Objects.requireNonNullElse(cause, e).toString());
            log.error("RPC Server handle request error", e);
        }

        log.info("Sending response: {}", rpcResponse);
        // Send the response
        ctx.writeAndFlush(rpcResponse).addListener(future -> {
            if (future.isSuccess()) {
                log.info("Response sent successfully: {}", rpcResponse);
            } else {
                log.error("Failed to send response: {}", rpcResponse);
            }
        });
    }

    /**
     * Handle the request
     *
     * @param rpcRequest request object
     * @return result
     * @throws NoSuchMethodException     no such method
     * @throws IllegalAccessException    illegal access
     * @throws InvocationTargetException invocation target exception
     */
    private Object handleRequest(RpcRequest rpcRequest) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String className = rpcRequest.getClassName();
        String methodName = rpcRequest.getMethodName();
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] parameters = rpcRequest.getParameters();

        // Get the service bean
        Object serviceBean = handlerMap.get(className);
        // Get the method
        Method method = serviceBean.getClass().getMethod(methodName, parameterTypes);
        // Execute the method
        return method.invoke(serviceBean, parameters);
    }

}

