package com.hz;

import com.hz.configuration.EnphaseCollectorProperties;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;

class MqqtResourceTest {

	private EnphaseCollectorProperties.MqqtResource makeMqqtResource() {
		EnphaseCollectorProperties.MqqtResource resource = new EnphaseCollectorProperties.MqqtResource();

		return resource;
	}

	@Test
	void MqqtTest() {
		EnphaseCollectorProperties.MqqtResource resource = makeMqqtResource();

		assertThat(resource.isPublisherIdEmpty(), comparesEqualTo(true));
		assertThat(resource.isTopicEmpty(), comparesEqualTo(true));

		resource.setHost("hzmega.local");
		resource.setPort(1883);
		resource.setTopic("topic1");
		resource.setPublisherId("pid1");

		assertThat(resource.isPublisherIdEmpty(), comparesEqualTo(false));
		assertThat(resource.isTopicEmpty(), comparesEqualTo(false));
		assertThat(resource.getUrl(), comparesEqualTo("tcp://hzmega.local:1883"));

	}

}
