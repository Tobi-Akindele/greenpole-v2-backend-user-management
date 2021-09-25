FROM openjdk:11-jre-slim

FROM maven:3.6.3-jdk-11 AS MAVEN_BUILD

COPY setup.sh /root/greenpole/setup.sh
RUN chmod +x /root/greenpole/setup.sh
RUN /root/greenpole/setup.sh

COPY pom.xml /build/
COPY src /build/src/
WORKDIR /build/
RUN mvn package -U -Dmaven.test.skip=true
RUN ls /build/target
RUN cp /build/target/greenpole-user-management-0.0.1.jar /opt/greenpole


WORKDIR /

COPY install.sh /root/greenpole/install.sh
RUN chmod +x /root/greenpole/install.sh
CMD  /root/greenpole/install.sh
