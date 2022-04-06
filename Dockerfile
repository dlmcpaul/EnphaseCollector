FROM azul/zulu-openjdk-alpine:17-jre-headless as builder
LABEL maintainer="dlmcpaul@gmail.com"

ARG JAR_FILE
COPY ${JAR_FILE} /app.jar

# Generate a JDK class data share
RUN "$JAVA_HOME/bin/java" -Xshare:dump

# Explode Uber jar into lib jars and classes
RUN "$JAVA_HOME/bin/jar" -xf app.jar

FROM azul/zulu-openjdk-alpine:17-jre-headless
LABEL maintainer="dlmcpaul@gmail.com"

COPY --from=builder "./BOOT-INF/lib" /app/lib
COPY --from=builder "./META-INF" /app/META-INF
COPY --from=builder "./BOOT-INF/classes" /app
COPY --from=builder "${JAVA_HOME}/lib/server/classes.jsa" "${JAVA_HOME}/lib/server"

ENV SPRING_DATASOURCE_URL=jdbc:h2:/internal_db/solar_stats_db
RUN mkdir "/properties"
RUN touch "/properties/application.properties"

ENTRYPOINT ["java", "-cp", "app:app/lib/*", "-Xshare:auto", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.jmx.enabled=false", "com.hz.EnphaseCollectorApplication", "--spring.config.additional-location=file:/properties/application.properties"]

EXPOSE 8080

VOLUME /internal_db /properties