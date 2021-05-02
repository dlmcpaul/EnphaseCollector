package com.hz.models.database;

import com.hz.metrics.Metric;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Data
public class Event {
	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
	private Long id;

	@OneToMany(cascade=CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Panel> panels = new ArrayList<>();
	private LocalDateTime time = LocalDateTime.now();
	private BigDecimal consumption = new BigDecimal(0);
	private BigDecimal production = new BigDecimal(0);
	private BigDecimal voltage = new BigDecimal(0);

	public void addSolarPanel(Metric metric) {
		if (metric.isSolarPanel()) {
			panels.add(new Panel(metric.getName(), metric.getValue()));
		}
	}

	public long countMaxPanelsProducing(BigDecimal value) {
		if (panels != null && panels.isEmpty() == false) {
			return panels.stream().filter(p -> BigDecimal.valueOf(p.getValue()).compareTo(value) == 0).count();
		}

		return 0;
	}

	public BigDecimal getMaxPanelProduction() {
		if (panels != null && panels.isEmpty() == false) {
			Comparator<Panel> comparator = Comparator.comparing(Panel::getValue);
			return BigDecimal.valueOf(panels.stream().max(comparator).orElseThrow().getValue());
		}

		return BigDecimal.ZERO;
	}

}
