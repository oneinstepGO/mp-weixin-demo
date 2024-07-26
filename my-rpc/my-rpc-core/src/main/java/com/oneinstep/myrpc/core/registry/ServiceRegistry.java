package com.oneinstep.myrpc.core.registry;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;


/**
 * Service registration
 */
@Component
@Slf4j
public class ServiceRegistry {

    @Value("${zookeeper.address}")
    private String zkAddress;

    private final Random random = new Random();

    /**
     * ZooKeeper client
     */
    private CuratorFramework client;

    @PostConstruct
    public void init() {
        // Create a ZooKeeper client
        client = CuratorFrameworkFactory.builder()
                .connectString(zkAddress)
                .sessionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        client.start();
    }

    /**
     * Register the service to ZooKeeper
     *
     * @param serviceName    service name
     * @param serviceAddress service address
     * @throws Exception exception
     */
    public void register(String serviceName, String serviceAddress) throws Exception {
        String servicePath = "/my-rpc/" + serviceName;
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(servicePath + "/address-", serviceAddress.getBytes());
        log.info("Service registered: {} at {}", serviceName, serviceAddress);
    }


    /**
     * Discover the service from ZooKeeper
     *
     * @param serviceName service name
     * @return service address
     * @throws Exception exception
     */
    public String discover(String serviceName) throws Exception {
        String servicePath = "/my-rpc/" + serviceName;
        List<String> addressList = client.getChildren().forPath(servicePath);
        if (addressList == null || addressList.isEmpty()) {
            log.error("Service not found: {}", serviceName);
            throw new RuntimeException("Service not found: " + serviceName);
        }
        log.info("Service discovered: {}", addressList);
        // 随机获取一个服务地址
        String addressNode = addressList.get(random.nextInt(addressList.size()));
        byte[] addressBytes = client.getData().forPath(servicePath + "/" + addressNode);
        return new String(addressBytes);
    }
}