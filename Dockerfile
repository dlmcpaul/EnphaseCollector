FROM azul/zulu-openjdk-alpine:11
LABEL maintainer="dlmcpaul@gmail.com"

# Precompile java runtime
RUN java -Xshare:dump

ENTRYPOINT ["java", "-Xshare:on", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.jmx.enabled=false", "-jar", "/home/enphasecollector.jar"]
EXPOSE 8080

VOLUME /internal_db
ARG JAR_FILE
COPY ${JAR_FILE} /home/enphasecollector.jar