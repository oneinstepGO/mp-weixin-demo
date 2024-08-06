package com.oneinstep.myrpc.core.client;

import com.oneinstep.myrpc.core.codec.RpcDecoder;
import com.oneinstep.myrpc.core.codec.RpcEncoder;
import com.oneinstep.myrpc.core.dto.RpcRequest;
import com.oneinstep.myrpc.core.dto.RpcResponse;
import com.oneinstep.myrpc.core.exception.RpcException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * RPC client
 * 发送 RPC 请求，处理 RPC 响应
 */
@Slf4j
public class RpcClient {

    private final String host;
    private final int port;
    private EventLoopGroup group;
    private Channel channel;

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
        initialize();
    }

    /**
     * 初始化 Netty 客户端
     */
    private void initialize() {
        group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // Encode request
                            pipeline.addLast(new RpcEncoder(RpcRequest.class));
                            // Decode response
                            pipeline.addLast(new RpcDecoder(RpcResponse.class));
                            // 处理 RPC 响应
                            pipeline.addLast(new RpcClientHandler());
                        }
                    });

            // 连接服务器
            ChannelFuture future = bootstrap.connect(host, port).sync();
            channel = future.channel();
            log.info("Connected to {}:{}", host, port);
        } catch (InterruptedException e) {
            log.error("Failed to connect to {}:{}", host, port, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 发送 RPC 请求
     *
     * @param request RPC 请求
     * @return RPC 响应
     * @throws InterruptedException exception
     */
    public RpcResponse send(RpcRequest request) throws InterruptedException {
        // 将 requestId 和响应对象的映射关系存入 CompletableFuture
        CompletableFuture<RpcResponse> completableFuture = RpcClientHandler.addResponse(request.getRequestId());
        // 写入 RPC 请求数据
        channel.writeAndFlush(request).sync();
        log.info("Sent RPC request to {}:{}", host, port);
        // 从 RpcClientHandler 获取响应
        try {
            return completableFuture.get();
        } catch (ExecutionException e) {
            log.error("RPC request failed", e);
            if ((e.getCause() instanceof RpcException rpcException)) {
                throw rpcException;
            } else if (e.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            } else {
                throw new RpcException("RPC request failed, cause: " + e.getCause().getMessage());
            }
        } finally {
            // 移除 requestId 和响应对象的映射关系
            RpcClientHandler.removeResponse(request.getRequestId());
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (channel != null) {
            channel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
        log.info("Connection to {}:{} closed", host, port);
    }
}
