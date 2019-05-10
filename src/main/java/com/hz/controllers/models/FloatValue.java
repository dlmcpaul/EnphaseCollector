package com.hz.controllers.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Data
@JsonFormat(shape= JsonFormat.Shape.ARRAY)
public class FloatValue {
	private long date;
	private float watts;

	public FloatValue(LocalDateTime localDateTime, BigDecimal watts) {
		this.date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()).getTime();
		this.watts = watts.floatValue();
	}

}
