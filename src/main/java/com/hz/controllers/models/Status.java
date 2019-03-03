package com.hz.controllers.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Status {
	private String image;
	private String text;
	private String value;
}
