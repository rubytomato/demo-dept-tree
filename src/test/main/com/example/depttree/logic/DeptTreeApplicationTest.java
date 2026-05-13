package com.example.depttree.logic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

class DeptTreeApplicationTest {

	private static final String LINE_SEPARATOR = System.lineSeparator();

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
	void buildsSingleTreeForSingleRootMethod() throws Exception {
		final BytecodeRepository repository = BytecodeRepository.load(
			Arrays.asList(Paths.get("build/classes/java/main/com/example/samples")));
		final CallTreeService callTreeService = new CallTreeService(repository, 20);

		final String tree = callTreeService.buildTree(
			new MethodKey("com.example.samples.Hoge", "method1", "(Ljava/lang/String;Ljava/lang/String;)V"));

		assertTrue(tree.startsWith("# ===========" + LINE_SEPARATOR + "# root method ( method1 )"));
		assertTrue(tree.contains("com.example.samples.Hogo#method3()"));
	}

	@Test
	void countsMarkerClassesWithCrLfTreeOutput() {
		final String report = DeptTreeApplication.buildMarkerReport(
			"com.example.Fuga#method1()\r\ncom.example.Taco#method2()",
			new LinkedHashSet<String>(Arrays.asList("com.example.Fuga", "com.example.Taco")));

		assertTrue(report.contains("(1) com.example.Fuga"));
		assertTrue(report.contains("(1) com.example.Taco"));
	}

	@Test
	void excludesConfiguredExactPackage() throws Exception {
		final BytecodeRepository repository = BytecodeRepository.load(
			Arrays.asList(Paths.get("build/classes/java/main/com/example/samples")),
			PackageExclusions.from(Arrays.asList("com.example.samples")));
		final CallTreeService callTreeService = new CallTreeService(repository, 20);

		final String tree = callTreeService.buildTree(
			new MethodKey("com.example.samples.Hoge", "method1", "(Ljava/lang/String;Ljava/lang/String;)V"));

		assertFalse(tree.contains("com.example.samples.Fuga#method1(Long)"));
		assertFalse(tree.contains("com.example.samples.Poyo#method3(String)"));
	}

	@Test
	void releasesAnalyzedMethodsThatAreNoLongerNeededByRemainingRoots() throws Exception {
		final BytecodeRepository repository = BytecodeRepository.load(
			Arrays.asList(Paths.get("build/classes/java/main/com/example/samples")));
		final List<MethodKey> rootMethods = repository.findRootMethods("com.example.samples.Hoge");
		final MethodKey firstRoot = new MethodKey(
			"com.example.samples.Hoge", "method1", "(Ljava/lang/String;Ljava/lang/String;)V");
		final MethodKey secondRoot = new MethodKey(
			"com.example.samples.Hoge", "method2", "(Ljava/util/Date;)V");
		final MethodKey dependentMethod = new MethodKey(
			"com.example.samples.Fuga", "method1", "(Ljava/lang/Long;)V");

		repository.prepareForRootProcessing(rootMethods);
		repository.releaseProcessedRoot(firstRoot);

		assertFalse(repository.findMethod(firstRoot) != null);
		assertFalse(repository.findMethod(dependentMethod) != null);
		assertTrue(repository.findMethod(secondRoot) != null);
	}

	@Test
	void omitsZeroCountDisplayForMarkerClass() {
		final String report = DeptTreeApplication.buildMarkerReport(
			"com.example.Fuga#method1()",
			new LinkedHashSet<String>(Arrays.asList("com.example.Fuga", "com.example.Taco")));

		assertTrue(report.contains("(1) com.example.Fuga"));
		assertTrue(report.contains("    com.example.Taco"));
	}
}