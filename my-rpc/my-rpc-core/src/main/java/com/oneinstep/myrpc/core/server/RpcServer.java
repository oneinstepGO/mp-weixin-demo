package com.oneinstep.myrpc.core.server;

import com.oneinstep.myrpc.core.annotation.RpcService;
import com.oneinstep.myrpc.core.dto.RpcRequest;
import com.oneinstep.myrpc.core.dto.RpcResponse;
import com.oneinstep.myrpc.core.codec.RpcDecoder;
import com.oneinstep.myrpc.core.codec.RpcEncoder;
import com.oneinstep.myrpc.core.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * RPC server
 * Scan the class for fields annotated with @RpcService, and register the service
 */
@Component
@Slf4j
public class RpcServer implements ApplicationListener<ContextRefreshedEvent> {

    /**
     * Service registry
     */
    @Resource
    private ServiceRegistry serviceRegistry;
    @Resource
    private Environment environment;
    /**
     * Server port
     */
    @Value("${netty.bind.port}")
    private int bindPort;
    /**
     * Store the service name and corresponding service object
     */
    private final ConcurrentMap<String, Object> handlerMap = new ConcurrentHashMap<>();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Starting RPC Server...");
        ApplicationContext context = event.getApplicationContext();
        // Scan the class for fields annotated with @RpcService, and register the service
        Map<String, Object> serviceBeanMap = context.getBeansWithAnnotation(RpcService.class);

        // If no service is found, log a warning and return
        if (serviceBeanMap.isEmpty()) {
            log.warn("No service need to be registered");
            return;
        }

        log.info("Registering services...");
        for (Object serviceBean : serviceBeanMap.values()) {
            String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();
            try {
                // Register the service to ZooKeeper
                // get the service address
                String ipAddress = "127.0.0.1";
                try {
                    InetAddress inetAddress = InetAddress.getLocalHost();
                    ipAddress = inetAddress.getHostAddress();
                    log.info("本机IP地址: {}", ipAddress);
                } catch (UnknownHostException e) {
                    log.error("Failed to get the IP address", e);
                }
                serviceRegistry.register(interfaceName, ipAddress + ":" + bindPort);
                // Store the service name and corresponding service object
                handlerMap.putIfAbsent(interfaceName, serviceBean);
                log.info("Registered service: {}", interfaceName);
            } catch (Exception e) {
                log.error("Failed to register service", e);
            }
        }

        log.info("Initializing Netty Server...");

        new Thread(() -> {
            // Initialize and start Netty server
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline()
                                        // Decode request
                                        .addLast(new RpcDecoder(RpcRequest.class))
                                        // Encode response
                                        .addLast(new RpcEncoder(RpcResponse.class))
                                        // Processing RPC request
                                        .addLast(new RpcServerHandler(handlerMap));
                            }
                        })
                        .option(ChannelOption.SO_BACKLOG, 1024)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);

                // Bind the server port
                ChannelFuture future = bootstrap.bind(bindPort).sync();
                log.info("Netty Server started on port: {}", bindPort);
                // Wait until the server socket is closed
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                log.error("Failed to start RPC Server", e);
                Thread.currentThread().interrupt();
            } finally {
                // Shut down the event loop group
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        }).start();
    }

}
