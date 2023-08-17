package com.hz;


import com.hz.models.database.Panel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BucketTest {

	@Test
	void testBucketRanges() {
		assertEquals(0, new Panel("", 0).bucket());
		assertEquals(25, new Panel("", 4).bucket());
		assertEquals(50, new Panel("", 50).bucket());
		assertEquals(75, new Panel("", 74).bucket());
		assertEquals(100, new Panel("", 100).bucket());
		assertEquals(225, new Panel("", 222).bucket());
		assertEquals(600, new Panel("", 600).bucket());
		assertEquals(25, new Panel("", 15).bucket());
	}
}
