package com.example.depttree.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class CliOptionsTest {

    @Test
    void parseAnalyzeAllowsMultipleValuesInSingleOccurrence() {
        CliOptions options = new CliOptions();
        CommandLine commandLine = new CommandLine(options);

        commandLine.parseArgs(
            "--root", "com.example.struts.action.CustomerAction",
            "--analyze", "D:/demo/target/classes/**/*.class",
            "--analyze", "D:/demo/target/dependency/antlr-2.7.2.jar", "D:/demo/target/dependency/commons-beanutils-1.8.0.jar",
            "--depth", "100"
        );

        assertEquals("com.example.struts.action.CustomerAction", options.rootClass);
        assertEquals(100, options.depth);
        assertIterableEquals(
            Arrays.asList(
                "D:/demo/target/classes/**/*.class",
                "D:/demo/target/dependency/antlr-2.7.2.jar",
                "D:/demo/target/dependency/commons-beanutils-1.8.0.jar"
            ),
            options.analyze
        );
    }

    @Test
    void parseAnalyzeStopsAtNextOption() {
        CliOptions options = new CliOptions();
        CommandLine commandLine = new CommandLine(options);

        commandLine.parseArgs(
            "--root", "com.example.Hoge",
            "--analyze", "./target/classes/**/*.class", "./libs/a.jar", "./libs/b.jar",
            "--depth", "20"
        );

        assertEquals(20, options.depth);
        assertIterableEquals(
            Arrays.asList("./target/classes/**/*.class", "./libs/a.jar", "./libs/b.jar"),
            options.analyze
        );
    }
}
