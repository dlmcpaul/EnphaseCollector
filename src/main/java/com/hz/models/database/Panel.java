package com.hz.models.database;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class Panel {
	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
	private Long id;

	private String identifier;
	private float value;

	public Panel(String identifier, float value) {
		this.identifier = identifier;
		this.value = value;
	}

}
