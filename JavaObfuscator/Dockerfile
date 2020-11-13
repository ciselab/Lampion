##### Stage 1
##### Build and Run tests
FROM maven:3.6.3-openjdk-15 as builder

WORKDIR /app

# Copy project files to the docker image
COPY . .

WORKDIR /app

RUN mvn package verify

#### STAGE 2
#### Deploying the application

FROM azul/zulu-openjdk-alpine:15

ENV dir /usr/app

# bash so i have a normal commandline
RUN apk update && apk add bash

RUN mkdir -p $dir
Run mkdir -p /config

COPY --from=builder /app/target/Lampion-JavaObfuscator-1.0-SNAPSHOT.jar $dir
COPY --from=builder /app/src/main/resources/Docker/entrypoint.sh $dir
COPY --from=builder /app/src/main/resources/createManifestTables.sql $dir/schema.sql
COPY --from=builder /app/src/main/resources/Docker/config.properties /config/

WORKDIR $dir

RUN chmod +x ./entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]