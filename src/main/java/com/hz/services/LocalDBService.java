package com.hz.services;

import com.hz.interfaces.LocalExportInterface;
import com.hz.metrics.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Profile("localdb")
public class LocalDBService implements LocalExportInterface {

	private static final Logger LOG = LoggerFactory.getLogger(LocalDBService.class);

	@Override
	public void sendMetrics(List<Metric> metricList, Date readTime) {
		LOG.debug("Writing stats at {} with {} items", readTime, metricList.size());
	}
}
