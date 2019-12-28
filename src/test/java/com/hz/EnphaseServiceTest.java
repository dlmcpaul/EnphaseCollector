package com.hz;

import com.hz.models.envoy.json.EimType;
import com.hz.models.envoy.json.Production;
import com.hz.models.envoy.json.System;
import com.hz.services.EnphaseService;
import com.hz.utils.Calculators;
import com.hz.utils.Convertors;
import com.hz.utils.Validators;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class EnphaseServiceTest {

	@Mock
	private EnphaseService mockEnphaseService;

	private Optional<System> makeSystem(LocalDateTime now) {
		Optional<System> system = Optional.of(new System());

		system.get().setProduction(new Production());
		system.get().getProduction().setProductionList(new ArrayList<>());
		EimType production = new EimType();
		production.setMeasurementType("production");
		system.get().getProduction().getProductionList().add(production);
		Optional<EimType> productionEim = system.get().getProduction().getProductionEim();
		productionEim.ifPresent(typeBase -> typeBase.setReadingTime(now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000L));

		return system;
	}

	@Test
	public void CollectionDateTest() {
		LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

		// Given
		Mockito.when(this.mockEnphaseService.collectEnphaseData()).thenReturn(makeSystem(now));
		Mockito.when(this.mockEnphaseService.getCollectionTime(any(System.class))).thenCallRealMethod();
		// When
		LocalDateTime collectionTime = this.mockEnphaseService.getCollectionTime(this.mockEnphaseService.collectEnphaseData().get());
		// Then
		Assertions.assertEquals(now, collectionTime);
	}

	@Test
	public void ConvertorsTest() {
		assertThat(BigDecimal.ZERO, comparesEqualTo(Convertors.convertToWattHours(BigDecimal.ZERO, 0)));
		assertThat(BigDecimal.valueOf(16.6667), comparesEqualTo(Convertors.convertToWattHours(BigDecimal.valueOf(1000), 1)));

		assertThat(BigDecimal.valueOf(0.1667), comparesEqualTo(Convertors.convertToKiloWattHours(10000L, 1)));
		assertThat(BigDecimal.valueOf(1), comparesEqualTo(Convertors.convertToKiloWattHours(60000L, 1)));
	}

	@Test
	public void ValidatorsTest() {
		Assertions.assertTrue(Validators.isValidDuration("7days"));
		Assertions.assertTrue(Validators.isValidDuration("2WEeks"));
		Assertions.assertTrue(Validators.isValidDuration("3Months"));

		Assertions.assertFalse(Validators.isValidDuration("23Days"));
		Assertions.assertFalse(Validators.isValidDuration("Days"));
		Assertions.assertFalse(Validators.isValidDuration("3alpha"));
	}

	@Test
	public void CalculatorsTest() {
		assertThat(BigDecimal.valueOf(0.007), comparesEqualTo(Calculators.calculateFinancial(6000L, 0.07, "test", 1)));
		assertThat(BigDecimal.valueOf(0), comparesEqualTo(Calculators.calculateFinancial(0L, 0.07, "test", 1)));
	}
}
