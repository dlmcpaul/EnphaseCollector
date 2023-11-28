package com.hz;

import com.hz.configuration.EnphaseCollectorProperties;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;

class MqqtResourceTest {

	private EnphaseCollectorProperties.MqttResource makeMqqtResource() {
		return new EnphaseCollectorProperties.MqttResource();
	}

	@Test
	void MqqtTest() {
		EnphaseCollectorProperties.MqttResource resource = makeMqqtResource();

		assertThat(resource.isPublisherIdEmpty(), comparesEqualTo(true));
		assertThat(resource.isTopicEmpty(), comparesEqualTo(true));

		resource.setTopic("");
		assertThat(resource.isTopicEmpty(), comparesEqualTo(true));

		resource.setPublisherId("");
		assertThat(resource.isPublisherIdEmpty(), comparesEqualTo(true));

		resource.setHost("hzmega.local");
		resource.setPort(1883);
		resource.setTopic("topic1");
		resource.setPublisherId("pid1");

		assertThat(resource.isPublisherIdEmpty(), comparesEqualTo(false));
		assertThat(resource.isTopicEmpty(), comparesEqualTo(false));
		assertThat(resource.getUrl(), comparesEqualTo("tcp://hzmega.local:1883"));
	}

}
