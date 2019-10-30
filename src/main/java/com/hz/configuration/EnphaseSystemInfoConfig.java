package com.hz.configuration;

import com.hz.models.envoy.xml.EnvoyDevice;
import com.hz.models.envoy.xml.EnvoyInfo;
import com.hz.models.envoy.xml.EnvoyPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.client.RestTemplate;
import org.springframework.xml.transform.StringSource;

import java.io.IOException;

@Configuration
public class EnphaseSystemInfoConfig {

	private static final Logger LOG = LoggerFactory.getLogger(EnphaseSystemInfoConfig.class);

	private final EnphaseCollectorProperties config;
	private final RestTemplate enphaseRestTemplate;

	@Autowired
	public EnphaseSystemInfoConfig(EnphaseCollectorProperties config, RestTemplate enphaseRestTemplate) {
		this.config = config;
		this.enphaseRestTemplate = enphaseRestTemplate;
	}

	@Bean
	public Unmarshaller enphaseMarshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setClassesToBeBound(EnvoyInfo.class, EnvoyPackage.class, EnvoyDevice.class);

		return marshaller;
	}

	@Bean
	public EnvoyInfo envoyInfo(Unmarshaller enphaseMarshaller) {
		String infoXml = enphaseRestTemplate.getForObject(EnphaseRestClientConfig.CONTROLLER, String.class);

		try {
			if (infoXml != null) {
				return (EnvoyInfo) enphaseMarshaller.unmarshal(new StringSource(infoXml));
			}
		} catch (IOException e) {
			LOG.warn("Failed to read envoy info page.  Exception was {}", e.getMessage());
		}

		return new EnvoyInfo();
	}
}
