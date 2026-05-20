# syntax=docker/dockerfile:1

# Create a stage for resolving and downloading dependencies.
FROM azul-zulu:21 AS deps
LABEL maintainer="dlmcpaul@gmail.com"

WORKDIR /build

# Copy the mvnw wrapper with executable permissions.
COPY --chmod=0755 mvnw mvnw
COPY .mvn/ .mvn/

# Download dependencies as a separate step to take advantage of Docker's caching.
# Leverage a cache mount to /root/.m2 so that subsequent builds don't have to
# re-download packages.
RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline -DskipTests

RUN apt-get update && \
    apt-get install -y wget && \
    wget -q -P / -O H2MigrationTool.jar https://manticore-projects.com/download/H2MigrationTool-1.4/H2MigrationTool-1.4-all.jar

FROM deps AS package

WORKDIR /build

COPY ./.git .git/
COPY ./src src/
RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 \
    ./mvnw package -DskipTests && \
    mv target/enphasecollector-development-SNAPSHOT.jar target/app.jar

FROM package AS extract

WORKDIR /build

# unpack the uber jar into it's components
RUN java -Djarmode=layertools -jar target/app.jar extract --destination target/extracted

# final image is based on a jre
FROM azul-zulu:21-jre AS final

RUN mkdir "/properties" && \
    mkdir "/internal_db" && \
    touch "/properties/application.properties"

# Copy the executable from the "package" stage.
COPY --from=extract build/target/extracted/dependencies/ ./
COPY --from=extract build/target/extracted/spring-boot-loader/ ./
COPY --from=extract build/target/extracted/snapshot-dependencies/ ./
COPY --from=extract build/target/extracted/application/ ./
COPY --from=extract build/H2MigrationTool.jar ./

# Shell script to run the Database upgrade code using H2MigrationTool
# before running the appication
# Need to escape all $ symbols to prevent Docker Build from trying to subsitute at build time
COPY --chmod=+x <<EOF ./app/runapp.sh
#!/bin/sh
if [ -f "/internal_db/solar_stats_db.mv.db" ]; then
  SOURCE_DB_VERSION=1.4.200
  SOURCE_DB=/internal_db/solar_stats_db.mv.db
fi
if [ -f "/internal_db/solar_stats_db_v2.mv.db" ]; then
  SOURCE_DB_VERSION=2.1.214
  SOURCE_DB=/internal_db/solar_stats_db_v2.mv.db
fi
if [ -f "/internal_db/solar_stats_db_v2.2.mv.db" ] || [ -z "\${SOURCE_DB}" ]; then
  SOURCE_DB_VERSION=2.2.224
  echo "\$SOURCE_DB_VERSION of H2 database found no upgrade required"
fi
if [ -n "\${SOURCE_DB}" ]; then
  # convert database
  echo "Converting H2 database at \$SOURCE_DB from \$SOURCE_DB_VERSION to V2.2.224"
  java -jar H2MigrationTool.jar -f "\$SOURCE_DB_VERSION" -t 2.2.224 -d "\$SOURCE_DB"

  #rename converted file to new database name
  if [ "\$SOURCE_DB_VERSION" = "1.4.200" ]; then
    mv /internal_db/solar_stats_db.mv.db.224null.mv.db /internal_db/solar_stats_db_v2.2.mv.db
  else
    mv /internal_db/solar_stats_db_v2.mv.db.224null.mv.db /internal_db/solar_stats_db_v2.2.mv.db
  fi
  # rename original as backup
  echo "Creating backup of \$SOURCE_DB"
  mv "\$SOURCE_DB" "/internal_db/solar_stats_db_backup.mv.db"
  echo "Upgrade completed"
fi
java -Djava.security.egd=file:/dev/./urandom -Dspring.jmx.enabled=false org.springframework.boot.loader.launch.JarLauncher --spring.config.additional-location=file:/properties/application.properties
EOF

ENV SPRING_DATASOURCE_URL=jdbc:h2:/internal_db/solar_stats_db_v2.2

EXPOSE 8080

VOLUME /internal_db /properties

ENTRYPOINT ["/app/runapp.sh"]