FROM azul/zulu-openjdk-alpine:11
MAINTAINER David McPaul <dlmcpaul@gmail.com>

# Precompile java runtime
RUN java -Xshare:dump

ENV LC_ALL en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US.UTF-8
ENTRYPOINT ["java", "-Xshare:on", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.jmx.enabled=false", "-jar", "/home/enphasecollector.jar"]
EXPOSE 8080
ARG JAR_FILE
COPY ${JAR_FILE} /home/enphasecollector.jar