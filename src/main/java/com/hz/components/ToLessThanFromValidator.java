package com.hz.components;

import com.hz.annotations.ToLessThanFrom;
import com.hz.controllers.models.DateRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ToLessThanFromValidator implements ConstraintValidator<ToLessThanFrom, DateRange> {
	@Override
	public boolean isValid(DateRange value,
	                       ConstraintValidatorContext context) {
		if (value.getFrom() == null || value.getTo() == null) {
			// Will be handled by field level validation
			return true;
		}

		if (value.getTo().isAfter(value.getFrom()) == false) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(String.format("'From' date (%s) must be prior to 'To' date (%s)", value.getFrom(), value.getTo()))
					.addConstraintViolation();
			return false;
		}
		return true;
	}
}
