package com.hz.components;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Log4j2
@Lazy(false)
public class ReleaseInfoContributor implements InfoContributor, InitializingBean {

	@NotNull
	private final Environment env;

	private static final String UNRELEASED = "unreleased";
	private static final String RELEASE_VERSION_PROPERTY = "release.version";

	@Override
	public void afterPropertiesSet() {
		log.info("Application Release version is {}", AnsiOutput.encode(AnsiColor.BLUE) + AnsiOutput.encode(AnsiStyle.BOLD) + getVersion() + AnsiOutput.encode(AnsiStyle.NORMAL));
	}

	@Override
	public void contribute(Info.Builder builder) {
		builder.withDetail("release",
				Collections.singletonMap("version", getVersion()));
	}

	public String getVersion() {
		return Optional.ofNullable(env.getProperty(RELEASE_VERSION_PROPERTY)).orElse(UNRELEASED).trim();
	}
}
