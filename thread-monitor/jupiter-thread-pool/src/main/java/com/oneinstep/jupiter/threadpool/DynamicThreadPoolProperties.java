package com.oneinstep.jupiter.threadpool;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Named thread pool properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "dynamic-thread-pool")
public class DynamicThreadPoolProperties {

    private Map<String, ThreadPoolConfig> pools;

    private GlobalMonitorConfig monitor;

    @Data
    public static class GlobalMonitorConfig {
        private MetricsExportConfig export;
        private String baseMonitorUrl;
    }

    @Data
    public static class MetricsExportConfig {
        private Boolean enabled;
        private String step;
        private Integer port;
    }

}
