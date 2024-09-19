FROM mysql:8.0

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

COPY ./db/init.sql /docker-entrypoint-initdb.d