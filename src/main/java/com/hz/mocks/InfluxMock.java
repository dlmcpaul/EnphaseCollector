package com.hz.mocks;

import com.hz.interfaces.ExportServiceInterface;
import com.hz.metrics.Metric;
import com.hz.services.EnphaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Profile("!influxdb")
@Service
public class InfluxMock implements ExportServiceInterface {

	private static final Logger LOG = LoggerFactory.getLogger(InfluxMock.class);

	@Override
	public void sendMetrics(List<Metric> metricList, Date readTime) {
		LOG.debug("Writing stats at {} with {} items", readTime, metricList.size());
	}
}
