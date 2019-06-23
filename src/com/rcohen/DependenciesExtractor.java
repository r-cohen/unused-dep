package com.rcohen;

import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.*;

import java.util.ArrayList;
import java.util.List;

class DependenciesExtractor extends CodeVisitorSupport {
    private List<DependencyFileEntry> dependencies;
    private boolean walkingDependencies = false;
    private int dependencyClosures = 0;

    DependenciesExtractor() {
        dependencies = new ArrayList<>();
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        if (call.getMethodAsString().equals("dependencies")) {
            walkingDependencies = true;
        } else if (walkingDependencies && !isDependencyLineFlagged(call.getLineNumber())) {
            DependencyFileEntry dependency = new DependencyFileEntry(
                    call.getText(),
                    call.getLineNumber(),
                    call.getLastLineNumber());
            dependencies.add(dependency);
            //System.out.println(call.getText() + " | " + call.getLineNumber() + " | " + call.getLastLineNumber());
        }
        super.visitMethodCallExpression(call);
    }

    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        if (walkingDependencies) {
            dependencyClosures++;
            super.visitClosureExpression(expression);
            dependencyClosures--;
            if (dependencyClosures == 0) {
                walkingDependencies = false;
            }
        }
    }

    private boolean isDependencyLineFlagged(int lineNumber) {
        for (DependencyFileEntry dependency : dependencies) {
            if (lineNumber >= dependency.getStartLineNumber() && lineNumber <= dependency.getEndLineNumber()) {
                return true;
            }
        }
        return false;
    }

    List<DependencyFileEntry> getDependencies() {
        return dependencies;
    }
}
