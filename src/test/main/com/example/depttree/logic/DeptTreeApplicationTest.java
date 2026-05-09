package com.example.depttree.logic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.junit.jupiter.api.Test;

class DeptTreeApplicationTest {

	@Test
	void omitsZeroCountDisplayForMarkerClass() {
		final String report = DeptTreeApplication.buildMarkerReport(
			"com.example.Fuga#method1()",
			new LinkedHashSet<String>(Arrays.asList("com.example.Fuga", "com.example.Taco")));

		assertTrue(report.contains("* com.example.Fuga (1)"));
		assertTrue(report.contains("* com.example.Taco"));
		assertFalse(report.contains("* com.example.Taco (0)"));
	}
}