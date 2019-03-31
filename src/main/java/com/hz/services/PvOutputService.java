package com.hz.services;

import com.hz.configuration.EnphaseCollectorProperties;
import com.hz.interfaces.PvOutputExportInterface;
import com.hz.metrics.Metric;
import com.hz.utils.Convertors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Profile("pvoutput")
public class PvOutputService implements PvOutputExportInterface {
	private static final Logger LOG = LoggerFactory.getLogger(PvOutputService.class);

	private BigDecimal energyGeneratedAccumulator = BigDecimal.ZERO;
	private BigDecimal energyConsumedAccumulator = BigDecimal.ZERO;
	private int powerGenerated = 0;
	private int powerConsumed = 0;
	private LocalDateTime nextUpdate = LocalDateTime.now().toLocalDate().atStartOfDay();

	private final EnphaseCollectorProperties properties;
	private final RestTemplate pvRestTemplate;

	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
	private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

	@Autowired
	public PvOutputService(EnphaseCollectorProperties properties, RestTemplate pvRestTemplate) {
		this.properties = properties;
		this.pvRestTemplate = pvRestTemplate;

		// Roll forward nextUpdate to nearest 5 min in future
		LocalDateTime now = LocalDateTime.now();
		while (now.isAfter(nextUpdate)) {
			nextUpdate = nextUpdate.plusMinutes(5);
		}

		// TODO should read the last update for today from pvoutput and set the accumulators
	}

	@Override
	public void sendMetrics(List<Metric> metrics, Date readTime) {
		LocalDateTime localReadTime = (LocalDateTime.ofInstant(readTime.toInstant(), ZoneId.systemDefault()));
		BigDecimal production = getMetric(metrics, "solar.production.current").map(metric -> BigDecimal.valueOf(metric.getValue())).orElse(BigDecimal.ZERO);
		BigDecimal consumption = getMetric(metrics, "solar.consumption.current").map(metric -> BigDecimal.valueOf(metric.getValue())).orElse(BigDecimal.ZERO);

		this.updateAccumulators(Convertors.convertToWattHours(production, properties.getRefreshSeconds()), Convertors.convertToWattHours(consumption, properties.getRefreshSeconds()));
		this.updatePower(production.intValue(), consumption.intValue());

		if (localReadTime.isAfter(nextUpdate)) {
			LOG.debug("dt={} v1={} v2={} v3={} v4={}", nextUpdate, energyGeneratedAccumulator, powerGenerated, energyConsumedAccumulator, powerConsumed);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
			map.add("d", dateFormatter.format(nextUpdate));
			map.add("t", timeFormatter.format(nextUpdate));
			map.add("v1", energyGeneratedAccumulator.toString());
			map.add("v2", String.valueOf(powerGenerated));
			map.add("v3", energyConsumedAccumulator.toString());
			map.add("v4", String.valueOf(powerConsumed));

			HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(map, headers);

			try {
				final ResponseEntity<String> stringResponseEntity = this.pvRestTemplate.postForEntity(properties.getPvOutputResource().getUrl(), requestEntity, String.class);
				if (stringResponseEntity.getStatusCodeValue() != 200) {
					LOG.info("ERROR {}", stringResponseEntity.hasBody() ? stringResponseEntity.getBody() : "NO BODY");
				}
			} catch (HttpClientErrorException e) {
				LOG.error("ERROR: {}", e.getMessage());
			}

			int day = nextUpdate.getDayOfMonth();
			nextUpdate = nextUpdate.plusMinutes(5);
			if (day != nextUpdate.getDayOfMonth()) {
				clearAccumulators();
			}
			clearPower();
		}
	}

	private Optional<Metric> getMetric(List<Metric> metrics, String name) {
		return metrics.stream().filter(metric -> metric.getName().equalsIgnoreCase(name)).findFirst();
	}

	private void clearAccumulators() {
		this.energyConsumedAccumulator = BigDecimal.ZERO;
		this.energyGeneratedAccumulator = BigDecimal.ZERO;
		this.powerConsumed = 0;
		this.powerGenerated = 0;
	}

	private void clearPower() {
		this.powerGenerated = 0;
		this.powerConsumed = 0;
	}

	private void updatePower(int powerGenerated, int powerConsumed) {
		if (powerGenerated > this.powerGenerated) {
			this.powerGenerated = powerGenerated;
		}
		if (powerConsumed > this.powerConsumed) {
			this.powerConsumed = powerConsumed;
		}
	}

	private void updateAccumulators(BigDecimal energyGenerated, BigDecimal energyConsumed) {
		this.energyGeneratedAccumulator = this.energyGeneratedAccumulator.add(energyGenerated);
		this.energyConsumedAccumulator = this.energyConsumedAccumulator.add(energyConsumed);
	}
}
