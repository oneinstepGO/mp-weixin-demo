package com.oneinstep.myrpc.core.client;

import com.oneinstep.myrpc.core.codec.RpcDecoder;
import com.oneinstep.myrpc.core.codec.RpcEncoder;
import com.oneinstep.myrpc.core.dto.RpcRequest;
import com.oneinstep.myrpc.core.dto.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * RPC client
 * send request to server and get response
 */
@Slf4j
public class RpcClient {

    /**
     * Send RPC request
     *
     * @param request request object
     * @param host    server host
     * @param port    server port
     * @return response object
     * @throws InterruptedException exception
     */
    public RpcResponse send(RpcRequest request, String host, int port) throws InterruptedException {
        // 创建并初始化 Netty 客户端 Bootstrap 对象
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            // 设置 EventLoopGroup、channel 类型、连接地址、处理器
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

            // 将 requestId 和响应对象的映射关系存入 CompletableFuture
            CompletableFuture<RpcResponse> completableFuture = RpcClientHandler.addResponse(request.getRequestId());
            // 连接服务器
            ChannelFuture future = bootstrap.connect(host, port).sync();
            // 写入 RPC 请求数据
            future.channel().writeAndFlush(request).sync();
            log.info("Sent RPC request to {}:{}", host, port);
            // 从 RpcClientHandler 获取响应
            try {
                return completableFuture.get();
            } catch (ExecutionException e) {
                log.error("RPC request failed", e);
                if (e.getCause() instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                } else {
                    throw new RuntimeException(e.getCause());
                }
            }
        } finally {
            // 关闭 EventLoopGroup
            group.shutdownGracefully();
            // 移除 requestId 和响应对象的映射关系
            RpcClientHandler.removeResponse(request.getRequestId());
        }
    }

}