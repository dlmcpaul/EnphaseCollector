FROM anapsix/alpine-java:latest
MAINTAINER David McPaul <dlmcpaul@gmail.com>

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-jar", "/home/enphasecollector.jar"]

ARG JAR_FILE
ADD ${JAR_FILE} /home/enphasecollector.jar