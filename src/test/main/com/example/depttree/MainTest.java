package com.example.depttree;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MainTest {

	@Test
	void usesDefaultLogFileNameWhenRootIsMissing() {
		assertEquals(Main.DEFAULT_LOG_FILE_NAME, Main.resolveLogFileName(new String[] {"--help"}));
	}

	@Test
	void usesRootClassForLongOption() {
		assertEquals("com.example.samples.Hoge.log",
			Main.resolveLogFileName(new String[] {"--root", "com.example.samples.Hoge"}));
	}

	@Test
	void usesRootClassForInlineLongOption() {
		assertEquals("com.example.samples.Hoge.log",
			Main.resolveLogFileName(new String[] {"--root=com.example.samples.Hoge"}));
	}

	@Test
	void usesRootClassForShortOption() {
		assertEquals("com.example.samples.Hoge.log",
			Main.resolveLogFileName(new String[] {"-r", "com.example.samples.Hoge"}));
	}
}