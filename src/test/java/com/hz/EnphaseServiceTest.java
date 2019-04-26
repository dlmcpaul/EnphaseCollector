package com.hz;

import com.hz.models.envoy.json.EimType;
import com.hz.models.envoy.json.Production;
import com.hz.models.envoy.json.System;
import com.hz.services.EnphaseService;
import com.hz.utils.Convertors;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
public class EnphaseServiceTest {

	@Mock
	private EnphaseService mockEnphaseService;

	private Optional<System> makeSystem(LocalDateTime now) {
		Optional<System> system = Optional.of(new System());

		system.get().setProduction(new Production());
		system.get().getProduction().setProductionList(new ArrayList<>());
		system.get().getProduction().getProductionList().add(new EimType());
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
		Assert.assertThat(collectionTime, Matchers.equalTo(now));
	}

	@Test
	public void ConvertorsTest() {
		BigDecimal result;

		result = Convertors.convertToWattHours(BigDecimal.ZERO, 0);
		Assert.assertThat(result, Matchers.comparesEqualTo(BigDecimal.ZERO));

		result = Convertors.convertToWattHours(BigDecimal.valueOf(1000), 1);
		Assert.assertThat(result, Matchers.comparesEqualTo(BigDecimal.valueOf(16.7000)));
	}
}