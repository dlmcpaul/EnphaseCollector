# syntax = docker/dockerfile:1.4
FROM azul/zulu-openjdk-alpine:21 AS builder
LABEL maintainer="dlmcpaul@gmail.com"

RUN wget -q -P / -O H2MigrationTool.jar https://manticore-projects.com/download/H2MigrationTool-1.4/H2MigrationTool-1.4-all.jar

ARG JAR_FILE
COPY ${JAR_FILE} /app.jar

# Generate a JDK class data share and
# Explode Uber jar into lib jars and classes
RUN "$JAVA_HOME/bin/java" -Xshare:dump && \
    "$JAVA_HOME/bin/jar" -xf app.jar

FROM azul/zulu-openjdk-alpine:21-jre-headless
LABEL maintainer="dlmcpaul@gmail.com"

COPY --from=builder "./BOOT-INF/lib" /app/lib
COPY --from=builder "./META-INF" /app/META-INF
COPY --from=builder "./BOOT-INF/classes" /app
COPY --from=builder "${JAVA_HOME}/lib/server/classes.jsa" "${JAVA_HOME}/lib/server"
COPY --from=builder "./H2MigrationTool.jar" "/H2MigrationTool.jar"

# Need to escape all $ symbols to prevent Docker Build from trying to subsitute at build time
COPY <<EOF /app/runapp.sh
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
java -cp app:app/lib/* -Xshare:auto -Djava.security.egd=file:/dev/./urandom -Dspring.jmx.enabled=false com.hz.EnphaseCollectorApplication --spring.config.additional-location=file:/properties/application.properties
EOF

ENV SPRING_DATASOURCE_URL=jdbc:h2:/internal_db/solar_stats_db_v2.2

RUN chmod +x /app/runapp.sh && \
          mkdir "/properties" && \
          touch "/properties/application.properties"

ENTRYPOINT ["/app/runapp.sh"]

EXPOSE 8080

VOLUME /internal_db /properties