# EnphaseCollector

Uses the undocumented API in the Envoy device to collect individual solar panel data and upload to an influx db

Can be run as a java application or using the docker image

If using the docker image

Example #1 using influxDB for storage

docker run \\\
-e ENVOY_CONTROLLER_PASSWORD=envoy-password \\\
-e ENVOY_CONTROLLER_HOST=IP=envoy-ip \\\
-e ENVOY_INFLUXDBRESOURCE_HOST=influxdb-ip \\\
-e ENVOY_INFLUXDBRESOURCE_PORT=influxdb-port \\\
-e SPRING_PROFILES_ACTIVE=influxdb \\\
dlmcpaul/enphasecollector

where password is likely to be the last 6 characters of your envoy controller serial number

Example #2 in standalone mode with no storage

docker run \\\
-e ENVOY_CONTROLLER_PASSWORD=envoy-password \\\
-e ENVOY_CONTROLLER_HOST=IP=envoy-ip \\\
-p 8080:8080 \\\
dlmcpaul/enphasecollector

and a web page available at http://localhost:8080

![Example Local Display](docs/images/LocalDisplay.jpg "Example Local Display")

Available environment variables descriptions:

- ENVOY_CONTROLLER_HOST           Set to your Envoy Controller IP Address
- ENVOY_CONTROLLER_PASSWORD       Set to your Envoy Controller password
- ENVOY_INFLUXDBRESOURCE_HOST     Set to your Influx Database IP Address
- ENVOY_INFLUXDBRESOURCE_PORT     Sety to your Influx Database Port No
- SPRING_PROFILES_ACTIVE          Determines destination for stats.  Only influxdb currently valid
- ENVOY_REFRESH_SECONDS           How often to poll the Envoy Controller.  Default 60000 (60s)
## Dependencies
- Docker (or Java)

- If profile set to influxdb then an 
Influx DB is needed for storage of stats (Will autocreate 2 databases called 'solardb' and 'collectorStats')

