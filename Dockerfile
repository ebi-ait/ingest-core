FROM java:8-alpine

# security-related updates (as reported by Quay)
RUN apk update && \
    apk upgrade freetype 2.6.3-r1 && \
    apk upgrade zlib 1.2.11-r0 && \
    apk upgrade musl 1.1.14-r16 && \
    apk upgrade libtasn1 4.8-r2


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
