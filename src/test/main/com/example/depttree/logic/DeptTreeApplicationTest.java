package com.example.depttree.logic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

class DeptTreeApplicationTest {

	@Test
	void resolvesConcreteMethodInheritedFromAbstractSuperclass() throws Exception {
		final BytecodeRepository repository = BytecodeRepository.load(
			Arrays.asList(Paths.get("build/classes/java/main/com/example/samples")));
		final CallTreeService callTreeService = new CallTreeService(repository, 20);

		final String tree = callTreeService.buildTrees(Arrays.asList(new MethodKey(
			"com.example.samples.Hoge", "method1", "(Ljava/lang/String;Ljava/lang/String;)V")));

		assertTrue(tree.contains("com.example.samples.Hogo#method3()"));
		assertFalse(tree.contains("[UNRESOLVED] com.example.samples.Hogo#method3()"));
	}

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