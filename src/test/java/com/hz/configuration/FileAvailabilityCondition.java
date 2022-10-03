package com.hz.configuration;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

@Log4j2
public class FileAvailabilityCondition implements ExecutionCondition {

	private Resource loadWithClassPathResource(String resourceName) {
		return new ClassPathResource(resourceName);
	}

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {

		final var optional = findAnnotation(context.getElement(), SkipWhenMissingFile.class);
		if (optional.isPresent()) {
			final SkipWhenMissingFile annotation = optional.get();
			final String filename = annotation.filename();

			Resource resource = loadWithClassPathResource(filename);

			if (resource.exists()) {
				log.info("File {} Found enabling TestCase", filename);
				return ConditionEvaluationResult.enabled("Test Enabled when file exists");
			}
			log.info("File {} not Found disabling TestCase", filename);
			return ConditionEvaluationResult.disabled("Test Disabled as no file exists");
		}
		return ConditionEvaluationResult.enabled("Test Enabled as Annotation not set");
	}
}
