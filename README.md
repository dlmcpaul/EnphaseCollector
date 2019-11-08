# EnphaseCollector

<a href="https://www.mozilla.org/en-US/MPL/2.0/"><img alt="mpl2" src="https://img.shields.io/github/license/dlmcpaul/EnphaseCollector"></a>
<a href="https://bulma.io"><img src="https://img.shields.io/badge/Made_with-Bulma-brightgreen"></a>
<a href="https://www.thymeleaf.org/"><img alt="Thymeleaf" src="https://img.shields.io/badge/Rendered_using-Thymeleaf-brightgreen"></a>

Uses the undocumented API in the Envoy device to collect individual solar panel data and upload to an influx db, pvoutput site or just as an internal view

Can be run as a java application or using the docker image

Main Page             |  Weekly History Tab
----------------------|-------------------------
<img src="https://dlmcpaul.github.io/EnphaseCollector/images/LocalDisplay.png">|<img src="https://dlmcpaul.github.io/EnphaseCollector/images/LocalWeekly.png">

If using the jar file you will need a Java 11 that you can get from https://adoptopenjdk.net/

Example #1 with default internal website
```
java -jar enphasecollector-DEV.jar --envoy.controller.password=envoy-password
```
where envoy-password is likely to be the last 6 characters of your envoy controller serial number

Example #2 when envoy.local is not resolved and you need to specify the ip address
```
java -jar enphasecollector-DEV.jar --envoy.controller.host=envoy-ip --envoy.controller.password=envoy-password
```
where envoy-ip is the ip address of your envoy controller
and envoy-password is likely to be the last 6 characters of your envoy controller serial number


If using the docker image

Example #1 using influxDB for storage
```
docker run \
-e TZ=your-timezone \
-e ENVOY_CONTROLLER_PASSWORD=envoy-password \
-e ENVOY_CONTROLLER_HOST=envoy-ip \
-e ENVOY_INFLUXDBRESOURCE_HOST=influxdb-ip \
-e ENVOY_INFLUXDBRESOURCE_PORT=influxdb-port \
-e SPRING_PROFILES_ACTIVE=influxdb \
dlmcpaul/enphasecollector
```
where envoy-password is likely to be the last 6 characters of your envoy controller serial number

Example #2 in standalone mode with internal database storage

```
docker run \
-e TZ=your-timezone \
-e ENVOY_CONTROLLER_PASSWORD=envoy-password \
-e ENVOY_CONTROLLER_HOST=envoy-ip \
-p 8080:8080 \
dlmcpaul/enphasecollector
```
and a web page available at http://localhost:8080/solar and looks like [this](https://dlmcpaul.github.io/EnphaseCollector "this")

You can also link the internal database to an external file system so the database is not cleared on upgrade of the image using the mount point /internal_db

Example #3 sending data to pvoutput
```
docker run \
-e TZ=your-timezone \
-e ENVOY_CONTROLLER_PASSWORD=envoy-password \
-e ENVOY_CONTROLLER_HOST=envoy-ip \
-e ENVOY_PVOUTPUTRESOURCE_SYSTEMID=your-system-id \
-e ENVOY_PVOUTPUTRESOURCE_KEY=your-key \
-e SPRING_PROFILES_ACTIVE=pvoutput \
dlmcpaul/enphasecollector
```
Your timezone is something like Australia/Sydney or similar

Available environment variables descriptions:

- ENVOY_CONTROLLER_HOST           Set to your Envoy Controller IP Address
- ENVOY_CONTROLLER_PASSWORD       Set to your Envoy Controller password
- ENVOY_INFLUXDBRESOURCE_HOST     Set to your Influx Database IP Address
- ENVOY_INFLUXDBRESOURCE_PORT     Set to your Influx Database Port No
- ENVOY_PVOUTPUTRESOURCE_SYSTEMID Set to your pvoutput systemid
- ENBOY_PVOUTPUTRESOURCE_KEY      Set to your pvoutput key
- SPRING_PROFILES_ACTIVE          Determines destination for stats.  if not set only an internal database gets the stats.  Values can be influxdb and pvoutput
- ENVOY_REFRESH_SECONDS           How often to poll the Envoy Controller.  Default 60000 (60s)
- ENVOY_PAYMENTPERKILOWATT        How much you get paid to export power to grid (FIT) eg 0.125 is 12.5c/Kw
- ENVOY_CHARGEPERKILOWATT         How much it costs to buy from the grid eg 0.32285 is 32.285c/Kw
- ENVOY_DAILYSUPPLYCHARGE         How much it costs to access the grid every day eg 0.93 is 93c/day
- SERVER_SERVLET_CONTEXT-PATH     Context path for local view

## Dependencies
- Docker (or Java 11)

- If profile set to influxdb then an 
Influx DB is needed for storage of the statistics (Will autocreate 2 databases called 'solardb' and 'collectorStats')

- If profile set to pvoutput then every 5m the stats will be uploaded to your account at https://pvoutput.org (you will need to create an account to to get the systemid and key)

- You can set both profiles separated by a comma eg influxdb,pvoutput

- The internal database is always populated so the local view is always available at /solar
