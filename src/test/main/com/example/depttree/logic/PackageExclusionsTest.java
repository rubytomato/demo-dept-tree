package com.example.depttree.logic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class PackageExclusionsTest {

	@Test
	void excludesOnlyExactPackageWhenRuleHasNoTrailingDot() {
		final PackageExclusions exclusions = PackageExclusions.from(Arrays.asList("com.example.target"));

		assertTrue(exclusions.isExcludedClass("com.example.target.Hoge"));
		assertFalse(exclusions.isExcludedClass("com.example.target.child.Hoge"));
	}

	@Test
	void excludesPackagePrefixWhenRuleHasTrailingDot() {
		final PackageExclusions exclusions = PackageExclusions.from(Arrays.asList("com.example.target."));

		assertTrue(exclusions.isExcludedClass("com.example.target.child.Hoge"));
		assertFalse(exclusions.isExcludedClass("com.example.target.Hoge"));
	}
}