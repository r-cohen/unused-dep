package com.rcohen;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.builder.AstBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class DependenciesParser implements Closeable {
    private List<ASTNode> nodes;
    private String gradleFilePath;
    private String originalFileContent;

    DependenciesParser(String gradleFilePath) throws Exception {
        this.gradleFilePath = gradleFilePath;
        this.originalFileContent = new String(Files.readAllBytes(Paths.get(gradleFilePath)));
        this.nodes = new AstBuilder().buildFromString(originalFileContent);
    }

    List<DependencyFileEntry> getDependencies() {
        DependenciesExtractor extractor = new DependenciesExtractor();
        nodes.forEach(node -> node.visit(extractor));
        return extractor.getDependencies();
    }

    void removeDependencyBlock(DependencyFileEntry dependency) {
        List<String> lines = new ArrayList<>(Arrays.asList(originalFileContent.split("\n")));
        int linesCountToDelete = dependency.getEndLineNumber() - dependency.getStartLineNumber() + 1;
        for (int i = 0; i < linesCountToDelete; i++) {
            lines.remove(dependency.getStartLineNumber() - 1);
        }
        writeFileWithContent(String.join("\n", lines));
    }

    private void writeFileWithContent(String s) {
        try (PrintStream out = new PrintStream(new FileOutputStream(gradleFilePath))) {
            out.print(s);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private void restoreFileContent() {
        writeFileWithContent(originalFileContent);
    }

    @Override
    public void close() {
        restoreFileContent();
    }
}
