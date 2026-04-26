package com.example.depttree.logic;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;

class CallTreeServiceInterfaceDispatchTest {

    @Test
    void interfaceInvocationResolvesToImplementations() throws Exception {
        Path workspace = Files.createTempDirectory("dept-tree-interface-case-");
        try {
            Path sourceRoot = workspace.resolve("src");
            Path classesRoot = workspace.resolve("classes");
            writeSource(sourceRoot, "com/example/ifacecase/Worker.java",
                "package com.example.ifacecase;\n"
                    + "public interface Worker {\n"
                    + "  void doWork();\n"
                    + "}\n");
            writeSource(sourceRoot, "com/example/ifacecase/WorkerImpl.java",
                "package com.example.ifacecase;\n"
                    + "public class WorkerImpl implements Worker {\n"
                    + "  public void doWork() { helper(); }\n"
                    + "  void helper() { }\n"
                    + "}\n");
            writeSource(sourceRoot, "com/example/ifacecase/Caller.java",
                "package com.example.ifacecase;\n"
                    + "public class Caller {\n"
                    + "  public void run() {\n"
                    + "    Worker worker = new WorkerImpl();\n"
                    + "    worker.doWork();\n"
                    + "  }\n"
                    + "}\n");

            compileSources(sourceRoot, classesRoot);

            List<Path> targets = collectClassFiles(classesRoot);
            BytecodeRepository repository = BytecodeRepository.load(targets);
            List<MethodKey> roots = repository.findRootMethods("com.example.ifacecase.Caller");

            CallTreeService service = new CallTreeService(repository, 10);
            String tree = service.buildTrees(roots);

            assertTrue(tree.contains("com.example.ifacecase.Caller#run()"));
            assertTrue(tree.contains("com.example.ifacecase.Worker#doWork()"));
            assertTrue(tree.contains("com.example.ifacecase.WorkerImpl#doWork()"));
        } finally {
            deleteRecursively(workspace);
        }
    }

    private static void compileSources(Path sourceRoot, Path classesRoot) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("System Java compiler is not available.");
        }

        List<Path> javaFiles = collectFilesByExtension(sourceRoot, ".java");
        Files.createDirectories(classesRoot);

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
        try {
            List<java.io.File> fileList = javaFiles.stream()
                .map(Path::toFile)
                .collect(Collectors.toList());
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(fileList);
            List<String> options = Arrays.asList("-d", classesRoot.toString());
            Boolean success = compiler.getTask(null, fileManager, null, options, null, compilationUnits).call();
            if (!Boolean.TRUE.equals(success)) {
                throw new IllegalStateException("Compilation failed for interface dispatch test sources.");
            }
        } finally {
            fileManager.close();
        }
    }

    private static List<Path> collectClassFiles(Path classesRoot) throws IOException {
        return collectFilesByExtension(classesRoot, ".class");
    }

    private static List<Path> collectFilesByExtension(Path root, String extension) throws IOException {
        List<Path> files = new ArrayList<Path>();
        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(extension))
                .forEach(files::add);
        }
        return files;
    }

    private static void writeSource(Path sourceRoot, String relativePath, String content) throws IOException {
        Path file = sourceRoot.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
    }

    private static void deleteRecursively(Path root) {
        if (root == null || !Files.exists(root)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(root)) {
            stream.sorted((a, b) -> b.getNameCount() - a.getNameCount())
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
