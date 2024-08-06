package com.oneinstep.myrpc.core.registry;

import com.oneinstep.myrpc.core.exception.ServiceNotFoundException;
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
     * @param version        service version
     * @param serviceAddress service address
     * @throws Exception exception
     */
    public void register(String serviceName, String version, String serviceAddress) throws Exception {
        String servicePath = "/my-rpc/" + serviceName;
        // Create a ephemeral sequential node, When the connection is closed, the node will be deleted automatically
        // The node name is like: /my-rpc/com.oneinstep.myrpc.api.ExampleService/address-0000000001
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(servicePath + "/" + version + "/" + "address-", serviceAddress.getBytes());
        log.info("Service registered: service:{},version:{} => {}", serviceName, version, serviceAddress);
    }


    /**
     * Discover the service from ZooKeeper
     *
     * @param serviceName service name
     * @param version     service version
     * @return service address
     * @throws Exception exception
     */
    public String discover(String serviceName, String version) throws Exception {
        String servicePath = "/my-rpc/" + serviceName + "/" + version;
        List<String> addressList;
        try {
            addressList = client.getChildren().forPath(servicePath);
        } catch (org.apache.zookeeper.KeeperException ke) {
            throw new ServiceNotFoundException("Service not found: " + serviceName + " version: " + version);
        }
        if (addressList == null || addressList.isEmpty()) {
            log.error("Service not found: {}", serviceName);
            throw new ServiceNotFoundException("Service not found: " + serviceName + " version: " + version);
        }
        log.info("Service discovered: {}", addressList);
        // 随机获取一个服务地址
        String addressNode = addressList.get(random.nextInt(addressList.size()));
        byte[] addressBytes = client.getData().forPath(servicePath + "/" + addressNode);
        return new String(addressBytes);
    }
}