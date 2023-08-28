#
# Build stage
#
FROM maven:3.9.3-eclipse-temurin-17-focal AS builder
WORKDIR /lingo_workspace
# Copy src code
COPY . .
RUN mvn clean package -DskipTests

#
# Package stage
#
FROM openjdk:17-jdk-slim-buster
WORKDIR /lingo-server

COPY --from=builder /lingo_workspace/lingo-server/target/lingo-server.jar .

ENTRYPOINT ["java", "-jar","lingo-server.jar"]
