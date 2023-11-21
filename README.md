# EnphaseCollector

<a href="https://www.mozilla.org/en-US/MPL/2.0/"><img alt="mpl2" src="https://img.shields.io/github/license/dlmcpaul/EnphaseCollector"></a>
<a href="https://bulma.io"><img src="https://img.shields.io/badge/Made_with-Bulma-brightgreen"></a>
<a href="https://www.thymeleaf.org/"><img alt="Thymeleaf" src="https://img.shields.io/badge/Rendered_using-Thymeleaf-brightgreen"></a>

> ## Support for envoy firmware >= D7.0.88
> From around V7 of the envoy firmware the security model for API access was changed.  This is obviously problematic for software such as mine that relies on local access to the API's
> 
> While it is entirely up to Enphase as to how they develop their software I see a number of issues with their new security model
>
>- It links your enphase community account to the token needed to access the API (**If you don't want an account or enphase suspends your account you will lose access**)
>- It does not look to be based on a standard authentication mechanism such as OAuth (**You should never write your own authentication protocol**)
>- It is currently broken in a number of ways and will reduce the security of your envoy device (**I will not list the issues here**)
>  
> The current release does support V7 firmware but you will either need to manage the token generation yourself or supply your enphase web user & password details
> SSL over HTTP is also a requirement so the port will need to be set to 443

EnphaseCollector uses the **undocumented API** in the Envoy device to collect individual solar panel data and upload to an influx db, pvoutput site or just as an internal view

Can be run as a java application or using the docker image

| Main Page                                                                       | Weekly History Tab                                                             | Questions and Answers Tab                                                      |
|---------------------------------------------------------------------------------|--------------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| <img src="https://dlmcpaul.github.io/EnphaseCollector/images/LocalDisplay.png"> | <img src="https://dlmcpaul.github.io/EnphaseCollector/images/LocalWeekly.png"> | <img src="https://dlmcpaul.github.io/EnphaseCollector/images/LocalQnA.png">    |

If using the jar file you will need a Java 17 that you can get from https://adoptium.net/

Example #1 with default internal website (assuming jar is named enphasecollector-development-SNAPSHOT.jar which is the default build artifact)
```
java -jar enphasecollector-development-SNAPSHOT.jar
```
where the application will attempt to guess the envoy location and password.

Example #2 when envoy.local is not resolved, and you need to specify the ip address and the password cannot be guessed.
```
java -jar enphasecollector-DEV.jar --envoy.controller.host=envoy-ip --envoy.controller.password=envoy-password
```
where envoy-ip is the ip address of your envoy controller
and envoy-password is likely to be the last 6 characters of your envoy controller serial number

Example #3 run spring boot locally with debugger support connecting to enphase to pull down a token
```
mvn spring-boot:run -Dspring-boot.run.arguments="--envoy.controller.host=<PRIVATE IP OF ENVOY> --envoy.controller.port=443 --envoy.enphaseWebUser=<USER> --envoy.enphaseWebPassword=<PASSWORD>" -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

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

You can also link the internal database to an external file system, so the database kept on upgrade of the image using the mount point /internal_db

```
docker run \
-e TZ=your-timezone \
-e ENVOY_CONTROLLER_PASSWORD=envoy-password \
-e ENVOY_CONTROLLER_HOST=envoy-ip \
-p 8080:8080 \
--mount target=/internal_db,source=host_path
dlmcpaul/enphasecollector
```
and replace host_path with the path on your host machine where you want to store the data.

Example #3 sending data to pvoutput.
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

Example #4 sending data to mqtt.
```
docker run \
-e TZ=your-timezone \
-e ENVOY_CONTROLLER_PASSWORD=envoy-password \
-e ENVOY_CONTROLLER_HOST=envoy-ip \
-e ENVOY_MQQTRESOURCE_HOST=mqqt-ip \
-e ENVOY_MQQTRESOURCE_PORT=mqqt-port \
-e ENVOY_MQQTRESOURCE_TOPIC=topic-name \
-e ENVOY_MQQTRESOURCE_PUBLISHERID=publisher-id \
-e SPRING_PROFILES_ACTIVE=mqtt \
dlmcpaul/enphasecollector
```

if ENVOY_MQQTRESOURCE_PUBLISHERID is not provided a random value will be chosen

Note the spelling mistake in the environment variables (MQQT instead of MQTT) This will likely be fixed in a later release

Available environment variables descriptions:

- ENVOY_CONTROLLER_HOST           Set to your Envoy Controller IP Address if envoy.local cannot be found (usually if run in Docker)
- ENVOY_CONTROLLER_USER           Set if the default user is not "envoy"
- ENVOY_CONTROLLER_PASSWORD       Set to your Envoy Controller password if you have changed it from the default
- ENVOY_INFLUXDBRESOURCE_HOST     Set to your Influx Database IP Address
- ENVOY_INFLUXDBRESOURCE_PORT     Set to your Influx Database Port No
- ENVOY_INFLUXDBRESOURCE_USER     Set if your Influx Database needs a user/password
- ENVOY_INFLUXDBRESOURCE_PASSWORD Set if your Influx Database needs a user/password
- ENVOY_PVOUTPUTRESOURCE_SYSTEMID Set to your pvoutput systemid
- ENVOY_PVOUTPUTRESOURCE_KEY      Set to your pvoutput key
- ENVOY_MQQTRESOURCE_HOST         Set to your MQTT Server IP Address
- ENVOY_MQQTRESOURCE_PORT         Set to your MQTT Server Port No
- ENVOY_MQQTRESOURCE_TOPIC        Set to the MQTT topic you want to write to 
- ENVOY_MQQTRESOURCE_PUBLISHERID  Set to the MQTT publisher id you want to use
- SPRING_PROFILES_ACTIVE          Determines destination for stats.  If not set only an internal database gets the stats.  Values can be influxdb, pvoutput, mqtt
- ENVOY_REFRESHSECONDS            How often to poll the Envoy Controller.  Default 60s
- ENVOY_PAYMENTPERKILOWATT        How much you get paid to export power to grid (FIT) eg 0.125 is 12.5c/Kw
- ENVOY_CHARGEPERKILOWATT         How much it costs to buy from the grid eg 0.32285 is 32.285c/Kw
- ENVOY_DAILYSUPPLYCHARGE         How much it costs to access the grid every day eg 0.93 is 93c/day
- SERVER_SERVLET_CONTEXT-PATH     Context path for local view if you want it on something other than /solar

### V7 support
Either supply
- ENVOY_BEARERTOKEN               Set this if you want to control the token refresh process and not supply your website user/password

Or if you want auto refresh

- ENVOY_ENPHASEWEBUSER            Set this to your enphase website user id
- ENVOY_ENPHASEWEBPASSWORD        Set this to your enphase website password

### New configuration

- ENVOY_EXPORT-LIMIT              If you have a limit on your export this will display a upper boundary on the main graph and display a new excess production line
- ENVOY_BANDS[].FROM              The bands array configuration will add a shaded band to the main graph that you can use to highlight changes to import costs and the like (See example below)
- ENVOY_BANDS[].TO                From and To are start and end times in 24hr format (must include a leading 0 eg 0700)
- ENVOY_BANDS[].COLOUR            The Colour field can be formatted like #55BF3B or rgba(200, 60, 60, .2)

### External Configuration file
The easiest way to configure the bands is with an external configuration file

- Create a file called application.properties containing values like the following (defining 2 bands 8am-12pm & 4pm-6pm)
```
envoy.bands[0].from = 0800
envoy.bands[0].to = 1200
envoy.bands[0].colour = #55BF3B
envoy.bands[1].from = 1600
envoy.bands[1].to = 1800
envoy.bands[1].colour = rgba(200, 60, 60, .2)
```
- Pass the file to the jar using the spring.config.additional-location parameter

```
java -jar enphasecollector.jar --spring.config.additional-location=file:application.properties
```

All properties can be configured this way and will override any defaults set in the jar.  Check the application.properties file for more properties that can be set

For Docker you will need a local directory to hold the file

```
docker run \
-e TZ=your-timezone \
-e ENVOY_CONTROLLER_PASSWORD=envoy-password \
-e ENVOY_CONTROLLER_HOST=envoy-ip \
-p 8080:8080 \
--mount target=/internal_db,source=host_path
--mount target=/properties,source=host_path
dlmcpaul/enphasecollector
```

## Exposing this application to the web
While I make every effort to make this application secure I cannot make any guarantees.  The application should be hosted behind a firewall and only exposed through a reverse proxy which includes an authentication mechanism and utilises https.

## Dependencies
- Docker (or Java 17)

- If profile set to influxdb then an 
Influx DB is needed for storage of the statistics (Will autocreate 2 databases called 'solardb' and 'collectorStats')

- If profile set to pvoutput then every 5m the stats will be uploaded to your account at https://pvoutput.org (you will need to create an account to to get the systemid and key)

- You can set multiple profiles separated by a comma eg influxdb,pvoutput

- The internal database is always populated so the local view is always available at /solar
- Stats can be pulled to Prometheus by using the Actuator endpoint configured at /solar/actuator/prometheus
- Stats can be pushed to a mqtt server with the mqtt profile (requires mqtt server)

## Building for yourself
This is a fairly standard maven project using spring boot so ```mvn package -Dmaven.test.skip``` should get your started and can build a working jar located in the target directory

You will need the following tools installed to develop and build this code.
- Git to clone the code and commit changes
- Java 17 to compile the code
- Maven to manage the build process
- Docker to support the testing

There are also modules built in if you want to store the data somewhere other than the internal database.  To use them you will need an installation or authentication for the specific system:
- InfluxDB 1.8
- PvOutput system id and key
- Prometheus
- Mqtt Server

**There are some caveats**
- The build will generate a jar with a default version of unreleased

### Docker Images
- You can use the spring boot plugin build-image to generate a docker image that works but does not export a properties file location you can use
- I also have a number of dockerfiles I use for my releases and experimentation.  I have documented them under [DOCKER.md](https://github.com/dlmcpaul/EnphaseCollector/blob/master/DOCKER.md)
