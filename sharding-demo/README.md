# shardingsphere-jdbc 演示

## 1. 版本
- Spring Boot: 3.3.3
- shardingsphere-jdbc: 5.5.0

## 2.演示内容
- 分库分表
- 加密
- 脱敏

## 3. 数据库
数据库为 `mysql` 8.0，结构如下：
```
sharding_db_0
    ├── t_order_0 (order_id, order_name, user_id)
    ├── t_order_item_0 (order_item_id, item_name, order_id, user_id)
    ├── t_user_0 (user_id, username, password, email, telephone)
    ├── t_order_1 (order_id, order_name, user_id)
    ├── t_order_item_1 (order_item_id, item_name, order_id, user_id)
    ├── t_user_1 (user_id, username, password, email, telephone)
    ├── t_order_2 (order_id, order_name, user_id)
    ├── t_order_item_2 (order_item_id, item_name, order_id, user_id)
    ├── t_user_2 (user_id, username, password, email, telephone)
    └── t_address (id, province, city, area, address)

sharding_db_1
    ├── t_order_0 (order_id, order_name, user_id)
    ├── t_order_item_0 (order_item_id, item_name, order_id, user_id)
    ├── t_user_0 (user_id, username, password, email, telephone)
    ├── t_order_1 (order_id, order_name, user_id)
    ├── t_order_item_1 (order_item_id, item_name, order_id, user_id)
    ├── t_user_1 (user_id, username, password, email, telephone)
    ├── t_order_2 (order_id, order_name, user_id)
    ├── t_order_item_2 (order_item_id, item_name, order_id, user_id)
    ├── t_user_2 (user_id, username, password, email, telephone)
    └── t_address (id, province, city, area, address)

sharding_db_2
    ├── t_order_0 (order_id, order_name, user_id)
    ├── t_order_item_0 (order_item_id, item_name, order_id, user_id)
    ├── t_user_0 (user_id, username, password, email, telephone)
    ├── t_order_1 (order_id, order_name, user_id)
    ├── t_order_item_1 (order_item_id, item_name, order_id, user_id)
    ├── t_user_1 (user_id, username, password, email, telephone)
    ├── t_order_2 (order_id, order_name, user_id)
    ├── t_order_item_2 (order_item_id, item_name, order_id, user_id)
    ├── t_user_2 (user_id, username, password, email, telephone)
    └── t_address (id, province, city, area, address)
```
> 原则：尽量让同一用户的数据在同一库中，避免跨库查询和跨库事物
## 4.启动演示
- `docker-compose up -d`
- 添加数据
  - `http://localhost:8088/test/addUserAndOrder`
- 查询数据
  - `http://localhost:8088/test/users`

[测试API](api%2Ftest_api.http)

## 5. 参考
[shardingsphere 官方文档](https://shardingsphere.apache.org/document/current/cn/overview/)