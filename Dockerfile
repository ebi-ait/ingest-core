FROM java:8-alpine

# security-related updates:
# https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2017-8287
RUN apk update && \
    apk upgrade freetype 2.6.3-r1

WORKDIR /opt

ENV LC_ALL=C
ENV MONGO_URI=mongodb://localhost:27017/admin
ENV RABBIT_HOST=localhost
ENV RABBIT_PORT=5672
ENV SCHEMA_BASE_URI=https://schema.humancellatlas.org

ADD gradle ./gradle
ADD src ./src

COPY gradlew build.gradle ./

RUN ./gradlew assemble

CMD     java -jar build/libs/*.jar \
        --spring.data.mongodb.uri=$MONGO_URI \
        --spring.rabbitmq.host=$RABBIT_HOST \
        --spring.rabbitmq.port=$RABBIT_PORT \
        --schema.base-uri=$SCHEMA_BASE_URI \
        -XX:+UseG1GC
