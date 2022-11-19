package com.hz;


import com.hz.models.database.Panel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BucketTest {

	@Test
	void testBucketRanges() {
		Assertions.assertEquals(0, new Panel("", 0).bucket());
		Assertions.assertEquals(25, new Panel("", 4).bucket());
		Assertions.assertEquals(50, new Panel("", 50).bucket());
		Assertions.assertEquals(75, new Panel("", 74).bucket());
		Assertions.assertEquals(100, new Panel("", 100).bucket());
		Assertions.assertEquals(225, new Panel("", 222).bucket());
		Assertions.assertEquals(600, new Panel("", 600).bucket());
		Assertions.assertEquals(25, new Panel("", 15).bucket());
	}
}
