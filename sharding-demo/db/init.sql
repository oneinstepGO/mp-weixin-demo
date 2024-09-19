-- db0
create database if not exists sharding_db_0;
use sharding_db_0;

drop table if exists t_order_0;
CREATE TABLE if not exists t_order_0
(
    order_id   bigint PRIMARY KEY,
    order_name VARCHAR(255) not null,
    user_id    bigint       not null
);
drop table if exists t_order_item_0;
CREATE TABLE if not exists t_order_item_0
(
    order_item_id bigint PRIMARY KEY,
    item_name     VARCHAR(255) not null,
    order_id      bigint       not null,
    user_id       bigint       not null
);
drop table if exists t_user_0;
CREATE TABLE if not exists t_user_0
(
    user_id   bigint PRIMARY KEY,
    username  VARCHAR(255) not null unique,
    password  VARCHAR(255) not null,
    email     VARCHAR(255) not null,
    telephone VARCHAR(255) not null
);


drop table if exists t_order_1;
CREATE TABLE if not exists t_order_1
(
    order_id   bigint PRIMARY KEY,
    order_name VARCHAR(255) not null,
    user_id    bigint       not null
);
drop table if exists t_order_item_1;
CREATE TABLE if not exists t_order_item_1
(
    order_item_id bigint PRIMARY KEY,
    item_name     VARCHAR(255) not null,
    order_id      bigint       not null,
    user_id       bigint       not null
);
drop table if exists t_user_1;
CREATE TABLE if not exists t_user_1
(
    user_id   bigint PRIMARY KEY,
    username  VARCHAR(255) not null unique,
    password  VARCHAR(255) not null,
    email     VARCHAR(255) not null,
    telephone VARCHAR(255) not null
);


drop table if exists t_order_2;
CREATE TABLE if not exists t_order_2
(
    order_id   bigint PRIMARY KEY,
    order_name VARCHAR(255) not null,
    user_id    bigint       not null
);
drop table if exists t_order_item_2;
CREATE TABLE if not exists t_order_item_2
(
    order_item_id bigint PRIMARY KEY,
    item_name     VARCHAR(255) not null,
    order_id      bigint       not null,
    user_id       bigint       not null
);
drop table if exists t_user_2;
CREATE TABLE if not exists t_user_2
(
    user_id   bigint PRIMARY KEY,
    username  VARCHAR(255) not null unique,
    password  VARCHAR(255) not null,
    email     VARCHAR(255) not null,
    telephone VARCHAR(255) not null
);


drop table if exists t_address;
create table if not exists t_address
(
    id       bigint primary key auto_increment,
    province varchar(128) not null,
    city     varchar(128) not null,
    area     varchar(128) not null,
    detail   varchar(512) not null
);


-- db1
create database if not exists sharding_db_1;
use sharding_db_1;

drop table if exists t_order_0;
CREATE TABLE if not exists t_order_0
(
    order_id   bigint PRIMARY KEY,
    order_name VARCHAR(255) not null,
    user_id    bigint       not null
);
drop table if exists t_order_item_0;
CREATE TABLE if not exists t_order_item_0
(
    order_item_id bigint PRIMARY KEY,
    item_name     VARCHAR(255) not null,
    order_id      bigint       not null,
    user_id       bigint       not null
);
drop table if exists t_user_0;
CREATE TABLE if not exists t_user_0
(
    user_id   bigint PRIMARY KEY,
    username  VARCHAR(255) not null unique,
    password  VARCHAR(255) not null,
    email     VARCHAR(255) not null,
    telephone VARCHAR(255) not null
);


drop table if exists t_order_1;
CREATE TABLE if not exists t_order_1
(
    order_id   bigint PRIMARY KEY,
    order_name VARCHAR(255) not null,
    user_id    bigint       not null
);
drop table if exists t_order_item_1;
CREATE TABLE if not exists t_order_item_1
(
    order_item_id bigint PRIMARY KEY,
    item_name     VARCHAR(255) not null,
    order_id      bigint       not null,
    user_id       bigint       not null
);
drop table if exists t_user_1;
CREATE TABLE if not exists t_user_1
(
    user_id   bigint PRIMARY KEY,
    username  VARCHAR(255) not null unique,
    password  VARCHAR(255) not null,
    email     VARCHAR(255) not null,
    telephone VARCHAR(255) not null
);


drop table if exists t_order_2;
CREATE TABLE if not exists t_order_2
(
    order_id   bigint PRIMARY KEY,
    order_name VARCHAR(255) not null,
    user_id    bigint       not null
);
drop table if exists t_order_item_2;
CREATE TABLE if not exists t_order_item_2
(
    order_item_id bigint PRIMARY KEY,
    item_name     VARCHAR(255) not null,
    order_id      bigint       not null,
    user_id       bigint       not null
);
drop table if exists t_user_2;
CREATE TABLE if not exists t_user_2
(
    user_id   bigint PRIMARY KEY,
    username  VARCHAR(255) not null unique,
    password  VARCHAR(255) not null,
    email     VARCHAR(255) not null,
    telephone VARCHAR(255) not null
);


drop table if exists t_address;
create table if not exists t_address
(
    id       bigint primary key auto_increment,
    province varchar(128) not null,
    city     varchar(128) not null,
    area     varchar(128) not null,
    detail   varchar(512) not null
);


-- db2
create database if not exists sharding_db_2;
use sharding_db_2;

drop table if exists t_order_0;
CREATE TABLE if not exists t_order_0
(
    order_id   bigint PRIMARY KEY,
    order_name VARCHAR(255) not null,
    user_id    bigint       not null
);
drop table if exists t_order_item_0;
CREATE TABLE if not exists t_order_item_0
(
    order_item_id bigint PRIMARY KEY,
    item_name     VARCHAR(255) not null,
    order_id      bigint       not null,
    user_id       bigint       not null
);
drop table if exists t_user_0;
CREATE TABLE if not exists t_user_0
(
    user_id   bigint PRIMARY KEY,
    username  VARCHAR(255) not null unique,
    password  VARCHAR(255) not null,
    email     VARCHAR(255) not null,
    telephone VARCHAR(255) not null
);


drop table if exists t_order_1;
CREATE TABLE if not exists t_order_1
(
    order_id   bigint PRIMARY KEY,
    order_name VARCHAR(255) not null,
    user_id    bigint       not null
);
drop table if exists t_order_item_1;
CREATE TABLE if not exists t_order_item_1
(
    order_item_id bigint PRIMARY KEY,
    item_name     VARCHAR(255) not null,
    order_id      bigint       not null,
    user_id       bigint       not null
);
drop table if exists t_user_1;
CREATE TABLE if not exists t_user_1
(
    user_id   bigint PRIMARY KEY,
    username  VARCHAR(255) not null unique,
    password  VARCHAR(255) not null,
    email     VARCHAR(255) not null,
    telephone VARCHAR(255) not null
);


drop table if exists t_order_2;
CREATE TABLE if not exists t_order_2
(
    order_id   bigint PRIMARY KEY,
    order_name VARCHAR(255) not null,
    user_id    bigint       not null
);
drop table if exists t_order_item_2;
CREATE TABLE if not exists t_order_item_2
(
    order_item_id bigint PRIMARY KEY,
    item_name     VARCHAR(255) not null,
    order_id      bigint       not null,
    user_id       bigint       not null
);
drop table if exists t_user_2;
CREATE TABLE if not exists t_user_2
(
    user_id   bigint PRIMARY KEY,
    username  VARCHAR(255) not null unique,
    password  VARCHAR(255) not null,
    email     VARCHAR(255) not null,
    telephone VARCHAR(255) not null
);


drop table if exists t_address;
create table if not exists t_address
(
    id       bigint primary key auto_increment,
    province varchar(128) not null,
    city     varchar(128) not null,
    area     varchar(128) not null,
    detail   varchar(512) not null
);