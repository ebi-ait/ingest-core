FROM anapsix/alpine-java:8_jdk

WORKDIR /opt

ENV LC_ALL=C
ENV MONGO_URI=mongodb://localhost:27017/admin

ADD gradle ./gradle
ADD src ./src

COPY gradlew build.gradle ./

RUN ./gradlew assemble

CMD java -jar build/libs/*.jar --spring.data.mongodb.uri=$MONGO_URI
