package com.hz.configuration;

import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

/*
  Additional Configuration for Thymeleaf
 */

@Configuration
public class ThymeleafConfiguration {

	@Bean
	public SpringResourceTemplateResolver javascriptTemplateResolver(ThymeleafProperties properties) {
		SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
		templateResolver.setPrefix(properties.getPrefix());
		templateResolver.setSuffix(".js");
		templateResolver.setTemplateMode(TemplateMode.JAVASCRIPT);
		templateResolver.setCharacterEncoding("UTF-8");
		templateResolver.setOrder(1);
		templateResolver.setCheckExistence(true);

		return templateResolver;
	}
}
