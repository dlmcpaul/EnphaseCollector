package com.hz.configuration;

import com.hz.models.envoy.xml.EnvoyDevice;
import com.hz.models.envoy.xml.EnvoyInfo;
import com.hz.models.envoy.xml.EnvoyPackage;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@TestConfiguration
@Profile("testing")
public class TestEnphaseSystemInfoConfig {
	@Bean
	public Unmarshaller enphaseMarshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setClassesToBeBound(EnvoyInfo.class, EnvoyPackage.class, EnvoyDevice.class);

		return marshaller;
	}

	@Bean
	public EnvoyInfo envoyInfo() {
		return new EnvoyInfo("unknown","unknown");
	}
}
