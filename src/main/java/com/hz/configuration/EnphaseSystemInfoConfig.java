package com.hz.configuration;

import com.hz.models.envoy.xml.EnvoyDevice;
import com.hz.models.envoy.xml.EnvoyInfo;
import com.hz.models.envoy.xml.EnvoyPackage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.xml.transform.StringSource;

import java.io.IOException;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@Log4j2
@Profile({"!testing"})
public class EnphaseSystemInfoConfig {

	private final RestTemplate enphaseRestTemplate;

	@Bean
	public Unmarshaller enphaseMarshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setClassesToBeBound(EnvoyInfo.class, EnvoyPackage.class, EnvoyDevice.class);

		return marshaller;
	}

	@Bean
	public EnvoyInfo envoyInfo(Unmarshaller enphaseMarshaller) {
		try {
			String infoXml = enphaseRestTemplate.getForObject(EnphaseRestClientConfig.CONTROLLER, String.class);
			if (infoXml != null) {
				return (EnvoyInfo) enphaseMarshaller.unmarshal(new StringSource(infoXml));
			}
		} catch (IOException | ResourceAccessException e) {
			log.warn("Failed to read envoy info page.  Exception was {}", e.getMessage());
		}

		return new EnvoyInfo("Unknown", "Unknown");
	}
}
