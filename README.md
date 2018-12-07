# EnphaseCollector

Uses the undocumented API in the Envoy device to collect individual solar panel data and upload to an influx db

start using 
docker run -e ENVOY_CONTROLLER_PASSWORD=password dlmcpaul/enphasecollector

where password is the last 6 characters of your envoy controller serial number

Other parameters you will likely need to set:

ENVOY_CONTROLLER_HOST=IP Address of your envoy controller
ENVOY_METRICDESTINATION_HOST=IP Address of your Influx DB
ENVOY_METRICDESTINATION_PORT=Port No of your Influx DB
