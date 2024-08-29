### 说明

- 本项目是一个基于 `spring boot` 的线程池监控组件，支持动态配置线程池参数，支持监控线程池的运行状态
- 本项目基于 `spring boot` 的 `actuator` 模块，通过 `prometheus` 暴露监控指标，通过 `grafana` 展示监控图表
- 支持 `LinkedBlockingQueue`、`ArrayBlockingQueue`、`SynchronousQueue`、`PriorityBlockingQueue` 四种队列类型
- 支持 `AbortPolicy`、`CallerRunsPolicy`、`DiscardOldestPolicy`、`DiscardPolicy` 四种拒绝策略
- 可选全局配置，对所有线程池生效
    - dynamic-thread-pool.monitor.export.enabled: 用于指定是否开启监控指标的暴露，默认 true
    - dynamic-thread-pool.monitor.export.port: 用于指定暴露监控指标的端口，默认 18081
    - dynamic-thread-pool.monitor.export.step: 用于指定监控指标的采集间隔，默认 1s
- 线程池管理界面只能查看线程池的基本信息，监控指标需要通过 `prometheus` 暴露，通过 `grafana` 展示
- 线程池管理界面只能查看和修改本机的线程池配置，无法查看和修改其他机器的线程池配置，使用者可以根据自己的需求进行扩展
- grafana 监控面板 JSON 配置文件在 `thread-monitor/grafana/provisioning/dashboards/thread-pool-monitor.json` 目录下，可以直接导入到 grafana 中使用, datasource 默认名为 `Prometheus-Thread-Monitor`
- **生产使用时，请先经过充分测试**

> 注意：如果覆盖了 management.endpoints.web.exposure.include 配置，需要确保包含了 `prometheus` 或 `all`，否则监控指标无法暴露

### 使用方式

- 1、将本项目中的 `jupiter-thread-pool` 模块打包成 `jar` 包，通过 maven 或其它方式引入到 `你的项目` 中
- 2、在你的 `spring boot`项目的 `application.yml` 或 `application.properties` 中添加线程池配置，例如：

```yaml
dynamic-thread-pool:
  pools:
    bizThreadPool:
      corePoolSize: 10
      maxPoolSize: 10
      keepAliveTimeMs: 6000
      workQueue:
        type: "LinkedBlockingQueue"
        capacity: 32
      policy: "AbortPolicy"
      monitor:
        enabled: true
        timeWindowSeconds: 3
        monitorUrl: "xxx"
    # 可以添加多个线程池配置
    otherThreadPool:
      ...
  monitor:
    baseMonitorUrl: "xxx"
```

- 3、在启动类上添加 `@EnableDynamicThreadPool` 注解，启动项目
- 4、访问 `http://{{your_server_host}}:{{your_server_port}}/thread-pool` ，例如：`http://localhost:8081/thread-pool`
  ，即可查看线程池监控管理页面，如果使用了 spring security 或者其他安全框架，需要配置对应的权限
- 5、访问 `http://{{your_server_host}}:{{your_metrics_export_port}}/actuator/prometheus`
  ，例如：`http://localhost:18081/actuator/prometheus` ，即可查看线程池监控指标

### Docker 一键启动演示项目
- 安装 docker
- 进入项目根目录 `mp-weixin-demo/thread-monitor`，执行以下命令
```
 docker compose up -d
```shell

- 访问 `http://localhost:8081/thread-pool` 线程池管理页面，点击线程池列表的监控按钮，查看对应的线程池监控图表