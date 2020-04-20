FROM azul/zulu-openjdk-alpine:11 as builder
LABEL maintainer="dlmcpaul@gmail.com"

ARG JAR_FILE
COPY ${JAR_FILE} /app.jar

# Explode Uber jar into jars and core
RUN "$JAVA_HOME/bin/jar" -xf app.jar

FROM azul/zulu-openjdk-alpine:11
LABEL maintainer="dlmcpaul@gmail.com"

COPY --from=builder "./BOOT-INF/lib" /app/lib
COPY --from=builder "./META-INF" /app/META-INF
COPY --from=builder "./BOOT-INF/classes" /app

ENTRYPOINT ["java","-cp","app:app/lib/*","-Djava.security.egd=file:/dev/./urandom", "-Dspring.jmx.enabled=false", "com.hz.EnphaseCollectorApplication"]

EXPOSE 8080

VOLUME /internal_db