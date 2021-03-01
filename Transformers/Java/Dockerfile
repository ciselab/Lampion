##### Stage 1
##### Build and Run tests
FROM maven:3.6.3-openjdk-15 as builder

WORKDIR /app
COPY . .
RUN mvn package verify

#### STAGE 2
#### Deploying the application
FROM azul/zulu-openjdk-alpine:15

ARG TRANSFORMER_VERSION

LABEL org.opencontainers.image.source https://github.com/ciselab/Lampion

ENV dir /usr/app

# bash so i have a normal commandline
RUN apk update && apk add bash

RUN mkdir -p $dir
Run mkdir -p /config

COPY --from=builder /app/target/Lampion-JavaTransformer-$TRANSFORMER_VERSION.jar $dir
COPY --from=builder /app/src/main/resources/Docker/entrypoint.sh $dir
COPY --from=builder /app/src/main/resources/createManifestTables.sql $dir/schema.sql
COPY --from=builder /app/src/main/resources/Docker/config.properties /config/

WORKDIR $dir

# The issue with build args is that they disappear after build time, 
# They are not availible as env-variables after the container was build 
# hence, rename the jar to a generic name at build time, to not have runtime issues
RUN mv Lampion-JavaTransformer-$TRANSFORMER_VERSION.jar Lampion-Transformer.jar

RUN chmod +x ./entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]