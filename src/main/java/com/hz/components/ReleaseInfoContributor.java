package com.hz.components;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
@Log4j2
public class ReleaseInfoContributor implements InfoContributor, InitializingBean {

	private final Environment env;

	private static final String UNRELEASED = "unreleased";
	private static final String RELEASE_VERSION_PROPERTY = "release.version";

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Application Release version is {}", AnsiOutput.encode(AnsiColor.BLUE) + AnsiOutput.encode(AnsiStyle.BOLD) + getVersion() + AnsiOutput.encode(AnsiStyle.NORMAL));
	}

	@Override
	public void contribute(Info.Builder builder) {
		builder.withDetail("release",
				Collections.singletonMap("version", getVersion()));
	}

	public String getVersion() {
		return env != null && env.getProperty(RELEASE_VERSION_PROPERTY) != null ? env.getProperty(RELEASE_VERSION_PROPERTY).trim() : UNRELEASED;
	}
}
