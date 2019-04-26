package com.hz.models.database;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Event {
	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
	private Long id;

	@OneToMany(cascade=CascadeType.ALL, fetch = FetchType.LAZY)
	@ElementCollection(targetClass=Panel.class)
	private List<Panel> panels = new ArrayList<>();
	private LocalDateTime time = LocalDateTime.now();
	private BigDecimal consumption = new BigDecimal(0);
	private BigDecimal production = new BigDecimal(0);
	private BigDecimal voltage = new BigDecimal(0);

	public void addSolarPanel(Panel panel) {
		if (panel.isSolarPanel()) {
			panels.add(panel);
		}
	}

}
