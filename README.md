# EnphaseCollector

Uses the undocumented API in the Envoy device to collect individual solar panel data and upload to an influx db

Can be run as a java application or using the docker image

If using the docker image

docker run -e ENVOY_CONTROLLER_PASSWORD=envoy-password \\\
-e ENVOY_CONTROLLER_HOST=IP=envoy-ip \\\
-e ENVOY_INFLUXDBRESOURCE_HOST=influxdb-ip \\\
-e ENVOY_INFLUXDBRESOURCE_PORT=influxdb-port \\\
-e SPRING_PROFILES_ACTIVE=influxdb \\\
dlmcpaul/enphasecollector

where password is likely to be the last 6 characters of your envoy controller serial number

Environment variables descriptions:

ENVOY_CONTROLLER_HOST           Set to your Envoy Controller IP Address

ENVOY_CONTROLLER_PASSWORD       Set to your Envoy Controller password

ENVOY_INFLUXDBRESOURCE_HOST     Set to your Influx Database IP Address

ENVOY_INFLUXDBRESOURCE_PORT     Sety to your Influx Database Port No

SPRING_PROFILES_ACTIVE          Determines destination for stats.  Only influxdb currently valid

## Dependencies
Influx DB with 2 databases created 'solardb' and 'collectorStats'

Docker (or Java)