package com.hz.models.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		Panel panel = (Panel) o;
		return Objects.equals(id, panel.id);
	}

	@Override
	public int hashCode() {
		return 0;
	}
}
