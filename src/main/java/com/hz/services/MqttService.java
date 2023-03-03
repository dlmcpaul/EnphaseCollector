package com.hz.services;

import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.metrics.Metric;
import com.hz.models.events.MetricCollectionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneId;

@Service
@Log4j2
@RequiredArgsConstructor
@Profile("mqtt")
public class MqttService {
	private final IMqttClient mqttClient;
	private final EnphaseCollectorProperties properties;

	private double getMetric(MetricCollectionEvent metricCollectionEvent, String name) {
		return metricCollectionEvent.getMetrics()
				.stream()
				.filter(metric -> metric.getName().equalsIgnoreCase(name))
				.findFirst()
				.map(metric -> BigDecimal.valueOf(metric.getValue()))
				.orElse(BigDecimal.ZERO).doubleValue();
	}

	public String createPayload(MetricCollectionEvent metricCollectionEvent) {

		StringBuilder payload = new StringBuilder();

		payload.append("solar.collection.time").append(":").append(metricCollectionEvent.getCollectionTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()).append("\n");
		payload.append("solar.collection.time.TZ").append(":").append(ZoneId.systemDefault().getId()).append("\n");
		payload.append("solar.collection.period").append(":").append(properties.getRefreshSeconds()).append("\n");
		payload.append("solar.meter.production").append(":").append(getMetric(metricCollectionEvent, Metric.METRIC_PRODUCTION_CURRENT)).append("\n");
		payload.append("solar.meter.consumption").append(":").append(getMetric(metricCollectionEvent, Metric.METRIC_CONSUMPTION_CURRENT)).append("\n");
		payload.append("solar.meter.voltage").append(":").append(getMetric(metricCollectionEvent, Metric.METRIC_PRODUCTION_VOLTAGE)).append("\n");
		payload.append("solar.meter.import").append(":").append(getMetric(metricCollectionEvent, Metric.METRIC_GRID_IMPORT)).append("\n");
		payload.append("solar.meter.export").append(":").append(getMetric(metricCollectionEvent, Metric.METRIC_SOLAR_EXCESS)).append("\n");

		metricCollectionEvent.getMetrics()
				.stream()
				.filter(Metric::isSolarPanel)
				.forEach(panel -> payload.append("solar.panel.production").append(".id.").append(panel.getName()).append(":").append(getMetric(metricCollectionEvent, panel.getName())).append("\n"));

		return payload.toString();
	}

	@EventListener
	public void metricListener(MetricCollectionEvent metricCollectionEvent) {
		if (mqttClient.isConnected() == false) {
			throw new RuntimeException("Mqtt client not connected");
		}

		log.debug("Sending metric stats at {} with {} items to MQTT topic {}", metricCollectionEvent.getCollectionTime(), metricCollectionEvent.getMetrics().size(), properties.getMqqtResource().getTopic());
		String payload = createPayload(metricCollectionEvent);

		MqttMessage msg = new MqttMessage(payload.getBytes());
		msg.setQos(0);
		msg.setRetained(true);
		try {
			mqttClient.publish(properties.getMqqtResource().getTopic(), msg);
		} catch (MqttException e) {
			throw new RuntimeException(e);
		}
	}
}
