package com.smallhk.toolkit;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringUtilsTest {

	@Test
	public void testHasTextString() {
		assertFalse(StringUtils.hasText(""));
		assertTrue(StringUtils.hasText("ewew"));
		assertTrue(StringUtils.hasText("ewew	e2ew"));
		assertTrue(StringUtils.hasText("e w e w e 2 e w"));
		assertTrue(StringUtils.hasText(" ewee2ew"));
		assertTrue(StringUtils.hasText("ewee2ew "));
	}

}
