package com.hz.annotations;

import com.hz.components.ToLessThanFromValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ToLessThanFromValidator.class)
public @interface ToLessThanFrom {
	String message() default "`from` should be more recent than `to`";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
