FROM openjdk:17-jdk-slim as builder

ARG JAR_FILE
COPY ${JAR_FILE} ./app.jar

# Explode Uber jar into lib jars and classes
RUN "$JAVA_HOME/bin/jar" -xf ./app.jar

FROM gcr.io/distroless/java:17
LABEL maintainer="dlmcpaul@gmail.com"

# Override these for your timezone and language
ENV TZ="Australia/Sydney" \
    LANG="en_AU.UTF-8"

# Generate a JDK class data share
RUN ["/usr/bin/java", "-Xshare:dump"]

COPY --from=builder "./BOOT-INF/lib" /app/lib
COPY --from=builder "./META-INF" /app/META-INF
COPY --from=builder "./BOOT-INF/classes" /app

EXPOSE 8080

VOLUME /internal_db

ENTRYPOINT ["/usr/bin/java", "-cp", "app:app/lib/*", "-Xshare:on", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.jmx.enabled=false", "com.hz.EnphaseCollectorApplication"]