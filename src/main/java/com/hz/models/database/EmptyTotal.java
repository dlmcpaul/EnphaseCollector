package com.hz.models.database;

import java.time.LocalDate;

public class EmptyTotal implements Total {
	@Override
	public LocalDate getDate() {
		return null;
	}

	@Override
	public Long getValue() {
		return 0L;
	}
}
