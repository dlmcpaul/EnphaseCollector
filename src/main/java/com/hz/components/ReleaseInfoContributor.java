package com.hz.components;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
@Log4j2
public class ReleaseInfoContributor implements InfoContributor, InitializingBean {

	private final Environment env;

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Application Release with version {}", getVersion());
	}

	@Override
	public void contribute(Info.Builder builder) {
		builder.withDetail("release",
				Collections.singletonMap("version", getVersion()));
	}

	public String getVersion() {
		return env != null && env.getProperty("release.version") != null ? env.getProperty("release.version").trim() : "unreleased";
	}
}
