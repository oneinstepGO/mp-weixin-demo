FROM maven:3.9.9-amazoncorretto-21 AS build
WORKDIR /home/app

COPY .. /home/app

RUN mvn -f /home/app/pom.xml clean package -DskipTests

FROM amazoncorretto:21-alpine3.20
WORKDIR /usr/local/lib

COPY --from=build /home/app/target/sharding-boot-demo-0.0.1-SNAPSHOT.jar ./sharding-boot-demo.jar

EXPOSE 8088
ENTRYPOINT ["java","-jar","/usr/local/lib/sharding-boot-demo.jar","--spring.profiles.active=docker"]