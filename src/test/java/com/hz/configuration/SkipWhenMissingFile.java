package com.hz.configuration;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(FileAvailabilityCondition.class)
public @interface SkipWhenMissingFile {
	String filename();
}
