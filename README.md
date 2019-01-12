# EnphaseCollector

Uses the undocumented API in the Envoy device to collect individual solar panel data and upload to an influx db

Can be run as a java application or using the docker image

If using the docker image

docker run -e ENVOY_CONTROLLER_PASSWORD=<password> -e ENVOY_CONTROLLER_HOST=IP=<ip> -e ENVOY_METRICDESTINATION_HOST=<influxdb-ip> -e ENVOY_METRICDESTINATION_PORT=<influxdb_port> dlmcpaul/enphasecollector

where password is the last 6 characters of your envoy controller serial number

Environment variables descriptions:

ENVOY_CONTROLLER_HOST           Envoy Controller IP Address
ENVOY_CONTROLLER_PASSWORD       Envoy Controller password (last 6 characters of your envoy controller serial number)
ENVOY_METRICDESTINATION_HOST    Influx Database IP Address
ENVOY_METRICDESTINATION_PORT    Influx Database Port No

## Dependencies
Influx DB with 2 databases created 'solardb' and 'collectorStats'

Docker (or Java)