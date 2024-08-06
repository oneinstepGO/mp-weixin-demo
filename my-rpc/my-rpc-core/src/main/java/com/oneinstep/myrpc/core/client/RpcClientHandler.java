package com.oneinstep.myrpc.core.client;

import com.oneinstep.myrpc.core.dto.RpcResponse;
import com.oneinstep.myrpc.core.exception.RpcException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    /**
     * Store the response of the request
     */
    private static final ConcurrentMap<String, CompletableFuture<RpcResponse>> responseMap = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) {
        try {

            // 检查响应的 requestId
            if (response.getRequestId() == null) {
                log.warn("Received response with null requestId: {}", response);
                return;
            }

            // 获取与 requestId 对应的 CompletableFuture
            CompletableFuture<RpcResponse> future = responseMap.get(response.getRequestId());
            if (future == null) {
                log.warn("No pending requests found for requestId: {}", response.getRequestId());
                return;
            }

            String error = response.getError();
            // 如果错误信息不为空，说明调用过程中出现了错误
            if (error != null && !error.isEmpty()) {
                future.completeExceptionally(new RpcException(error));
            }
            // 否则，说明调用过程正常，将结果返回给调用方
            else {
                future.complete(response);
            }
            log.info("Stored response for requestId: {}", response.getRequestId());
        } catch (Exception e) {
            log.error("Failed to parse and store response: {}", response, e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 发生异常时，关闭 ChannelHandlerContext
        log.error("Client caught exception", cause);
        ctx.close();
    }

    /**
     * 添加请求
     * @param requestId 请求 ID
     * @return CompletableFuture
     */
    public static CompletableFuture<RpcResponse> addResponse(String requestId) {
        return responseMap.computeIfAbsent(requestId, k -> new CompletableFuture<>());
    }

    /**
     * 移除请求
     * @param requestId 请求 ID
     */
    public static void removeResponse(String requestId) {
        responseMap.remove(requestId);
    }

}