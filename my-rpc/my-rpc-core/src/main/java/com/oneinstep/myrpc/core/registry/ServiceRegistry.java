package com.oneinstep.myrpc.core.registry;

import com.oneinstep.myrpc.core.exception.ServiceNotFoundException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Service registration
 */
@Component
@Slf4j
public class ServiceRegistry {

    @Value("${zookeeper.address}")
    private String zkAddress;

    private final Random random = new Random();

    private final Map<String, List<String>> CACHED_ADDRESS_LIST = new ConcurrentHashMap<>();
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

    @PreDestroy
    public void close() {
        log.info("Closing ZooKeeper client");
        client.close();
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
        String cacheKey = serviceName + "#" + version;
        List<String> addressList;
        boolean fromCache = false;
        try {
            addressList = client.getChildren().forPath(servicePath);
        } catch (KeeperException ke) {
            // get from cache
            addressList = CACHED_ADDRESS_LIST.get(cacheKey);
            fromCache = true;
            log.info("Service discovered from cache: {}", addressList);

        }
        if (addressList == null || addressList.isEmpty()) {
            log.error("Service not found: {}", serviceName);
            throw new ServiceNotFoundException("Service not found: " + serviceName + " version: " + version);
        }
        log.info("Service discovered: {}", addressList);

        if (!fromCache) {
            // compare and Save the service address to the cache
            List<String> cache = CACHED_ADDRESS_LIST.get(cacheKey);
            if (cache == null || !new HashSet<>(cache).containsAll(addressList) || !new HashSet<>(addressList).containsAll(cache)) {
                // overwrite cache
                CACHED_ADDRESS_LIST.put(cacheKey, addressList);
            }
        }

        // 随机获取一个服务地址
        String addressNode = addressList.get(random.nextInt(addressList.size()));
        byte[] addressBytes = client.getData().forPath(servicePath + "/" + addressNode);
        return new String(addressBytes);
    }
}