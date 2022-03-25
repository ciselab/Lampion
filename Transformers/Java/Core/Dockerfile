##### Build and Run tests
FROM maven:3.8.4-openjdk-17 as builder

WORKDIR /app
COPY . .
RUN mvn clean install