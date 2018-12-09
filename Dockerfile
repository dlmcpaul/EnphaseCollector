FROM anapsix/alpine-java:latest
MAINTAINER David McPaul <dlmcpaul@gmail.com>

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-jar", "/home/enphasecollector.jar"]
EXPOSE 8080
ARG JAR_FILE
ADD ${JAR_FILE} /home/enphasecollector.jar