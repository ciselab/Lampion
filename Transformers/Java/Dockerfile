##### Stage 1
##### Build and Run tests
FROM maven:3.8.4-openjdk-17 as builder

WORKDIR /app
COPY . .
RUN mvn -P nofiles verify

#### STAGE 2
#### Deploying the application
FROM azul/zulu-openjdk-alpine:17

ARG TRANSFORMER_VERSION="1.3-SNAPSHOT"

LABEL maintainer="L.H.Applis@tudelft.nl"
LABEL name="ciselab/lampion/java-transformer"
LABEL description="A metamorphic transformer for Java code"
LABEL org.opencontainers.image.source="https://github.com/ciselab/Lampion"
LABEL url="https://github.com/ciselab/Lampion"
LABEL vcs="https://github.com/ciselab/Lampion"

ENV dir /usr/app

# bash so i have a normal commandline
RUN apk update && apk add bash

RUN mkdir -p $dir
Run mkdir -p /config

COPY --from=builder /app/CLI/target/Lampion-CLI-${TRANSFORMER_VERSION}.jar $dir
COPY --from=builder /app/resources/Docker/entrypoint.sh $dir
# Copy the sample config
COPY --from=builder /app/resources/Docker/config.properties /config/

ENV target="/app/transformer_input"
ENV configfile="/config/config.properties"
ENV output="/app/transformer_output"

WORKDIR $dir

# The issue with build args is that they disappear after build time, 
# They are not availible as env-variables after the container was build 
# hence, rename the jar to a non-versioned name at build time, to not have runtime issues while keeping same entrypoint
RUN mv Lampion-CLI-${TRANSFORMER_VERSION}.jar Lampion-Transformer.jar

RUN chmod +x ./entrypoint.sh

ENTRYPOINT ["bash","./entrypoint.sh"]