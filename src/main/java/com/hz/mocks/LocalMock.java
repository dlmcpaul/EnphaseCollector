package com.hz.mocks;

import com.hz.interfaces.LocalExportInterface;
import com.hz.metrics.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Profile("!localdb")
@Service
public class LocalMock implements LocalExportInterface {

	private static final Logger LOG = LoggerFactory.getLogger(LocalMock.class);

	@Override
	public void sendMetrics(List<Metric> metricList, Date readTime) {
		LOG.info("Writing stats at {} with {} items", readTime, metricList.size());
	}
}
