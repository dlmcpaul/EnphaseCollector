package com.hz.models.database;

import com.hz.metrics.Metric;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="EVENT")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Event extends EventBase {

	@OneToMany(cascade= CascadeType.ALL, fetch = FetchType.EAGER)
	@ToString.Exclude
	private List<Panel> panels = new ArrayList<>();

	public void addSolarPanel(Metric metric) {
		if (metric.isSolarPanel()) {
			panels.add(new Panel(metric.getName(), metric.getValue()));
		}
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}
}
