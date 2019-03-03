package com.hz.mocks;

import com.hz.interfaces.PvOutputExportInterface;
import com.hz.metrics.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Profile("!pvoutput")
@Service
public class PvOutputMock implements PvOutputExportInterface {

	private static final Logger LOG = LoggerFactory.getLogger(PvOutputMock.class);

	@Override
	public void sendMetrics(List<Metric> metrics, Date readTime) {
		LOG.debug("Writing stats at {} with {} items", readTime, metrics.size());
	}
}
