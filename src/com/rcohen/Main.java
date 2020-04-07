package com.rcohen;

import com.beust.jcommander.JCommander;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String version = "1.1.0";
    private static final ArrayList<String> skippedDependencies = new ArrayList<>();

    public static void main(String[] args) {
        // parse command line flags
        Flags cliFlags = new Flags();
        JCommander commander = new JCommander();
        commander.addObject(cliFlags);
        commander.parse(args);

        if (cliFlags.version) {
            exitWithMessage(version);
            return;
        }
        if (cliFlags.help) {
            commander.usage();
            return;
        }

        // check if gradle wrapper in current folder
        if (!gradleWrapperExists()) {
            exitWithError(new Exception("gradlew not found"));
            return;
        }

        if (!cliFlags.skipDependencies.isEmpty()) {
            String[] dependenciesToSkip = cliFlags.skipDependencies.split(",");
            for (String s : dependenciesToSkip) {
                skippedDependencies.add(s.trim());
            }
        }

        // confirmation from user
        System.out.printf("This will attempt to run the `%s` gradle task for each dependency removal.\n", cliFlags.gradleTask);
        System.out.println("You can override the gradle task by using the `-gradletask` command line option. See `-help` for more options.");
        System.out.println("Proceed? (y/n)");
        String confirm = new Scanner(System.in).next();
        if (!confirm.toLowerCase().equals("y")) {
            return;
        }

        // get all gradle files
        List<String> gradleFiles;
        try {
            gradleFiles = getGradleFiles();
        } catch (IOException e) {
            exitWithError(e);
            return;
        }

        if (gradleFiles.size() == 0) {
            System.out.println("No gradle files found.");
            return;
        }

        // parse gradle files for dependencies
        HashMap<String, List<String>> unusedDependencies = new HashMap<>();
        gradleFiles.forEach(gradleFile -> {
            try {
                DependenciesParser parser = new DependenciesParser(gradleFile);
                System.out.printf("Processing %s ...\n", gradleFile);

                Thread shutdownHookThread = new Thread(parser::close);
                Runtime.getRuntime().addShutdownHook(shutdownHookThread);

                parser.getDependencies().forEach(dependency -> {
                    boolean skipDependency = shouldSkipDependency(dependency);
                    System.out.printf("\t|_ %s%s\n", dependency.getMethodCall(), skipDependency ? " --> SKIPPED" : "");
                    if (skipDependency) {
                        return; // forEach stream
                    }

                    // remove dependency from file from original file content
                    parser.removeDependencyBlock(dependency);

                    // launch gradle task(s)
                    System.out.printf("running %s\n", cliFlags.gradleTask);
                    boolean taskSuccessful = runGradleTask(cliFlags.gradleTask);
                    if (taskSuccessful) {
                        if (!unusedDependencies.containsKey(gradleFile)) {
                            unusedDependencies.put(gradleFile, new ArrayList<>());
                        }
                        unusedDependencies.get(gradleFile).add(dependency.getMethodCall());
                    }
                });

                Runtime.getRuntime().removeShutdownHook(shutdownHookThread);
                parser.close();
            } catch (Exception e) {
                System.out.printf("\nIgnoring %s\n", gradleFile);
            }
        });

        printDependenciesReport(unusedDependencies, cliFlags);
        exitWithMessage("Done.");
    }

    private static boolean gradleWrapperExists() {
        File gradlewFile = new File("gradlew");
        return gradlewFile.exists();
    }

    private static List<String> getGradleFiles() throws IOException {
        List<String> gradleFiles = new ArrayList<>();
        Path currentPath = Paths.get("").toAbsolutePath();
        Files.find(currentPath, Integer.MAX_VALUE, (path, basicFileAttributes) ->
                path.toFile().getName().matches(".*.gradle")).forEach(path -> {
                    if (new File(path.toUri()).isFile()) {
                        gradleFiles.add(path.toAbsolutePath().toString());
                    }
                });
        return gradleFiles;
    }

    private static void exitWithError(Exception error) {
        System.out.println(error.getMessage());
        System.exit(1);
    }

    private static void exitWithMessage(String message) {
        System.out.println(message);
        System.exit(0);
    }

    private static boolean runGradleTask(String gradleTask) {
        try {
            Process process = Runtime.getRuntime().exec(String.format("./gradlew %s", gradleTask));
            int returnCode = process.waitFor();
            return returnCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private static void printDependenciesReport(HashMap<String, List<String>> unusedDependencies, Flags cliFlags) {
        System.out.println("\n");
        if (unusedDependencies.size() == 0) {
            System.out.printf("NO UNUSED DEPENDENCIES FOUND FOR GRADLE TASK %s:\n\n", cliFlags.gradleTask);
            return;
        }

        System.out.printf("POTENTIALLY UNUSED DEPENDENCIES FOR GRADLE TASK %s:\n\n", cliFlags.gradleTask);
        unusedDependencies.keySet().forEach(filepath -> {
            System.out.println(filepath);
            List<String> dependencies = unusedDependencies.get(filepath);
            dependencies.forEach(dependency -> System.out.printf("\t|_ %s", dependency));
        });
    }

    private static boolean shouldSkipDependency(DependencyFileEntry dependency) {
        for (String s: skippedDependencies) {
            if (dependency.getMethodCall().contains(s)) {
                return true;
            }
        }
        return false;
    }
}
