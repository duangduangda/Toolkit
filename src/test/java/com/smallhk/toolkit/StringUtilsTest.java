package com.smallhk.toolkit;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringUtilsTest {

	@Test
	public void testHasText() {
		assertFalse(StringUtils.hasText(""));
		assertTrue(StringUtils.hasText("ewew"));
		assertTrue(StringUtils.hasText("ewew	e2ew"));
		assertTrue(StringUtils.hasText("e w e w e 2 e w"));
		assertTrue(StringUtils.hasText(" ewee2ew"));
		assertTrue(StringUtils.hasText("ewee2ew "));
	}
	
	@Test
	public void testHasLength(){
		assertFalse(StringUtils.hasLength(""));
		assertTrue(StringUtils.hasLength("ewew"));
		assertTrue(StringUtils.hasLength("ewew	e2ew"));
		assertTrue(StringUtils.hasLength("e w e w e 2 e w"));
		assertTrue(StringUtils.hasLength(" ewee2ew"));
		assertTrue(StringUtils.hasLength("ewee2ew "));
	}

}
