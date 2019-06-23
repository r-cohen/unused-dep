package com.rcohen;

class DependencyFileEntry {
    private String methodCall;
    private int startLineNumber;
    private int endLineNumber;

    DependencyFileEntry(String methodCall, int startLineNumber, int endLineNumber) {
        this.methodCall = methodCall;
        this.startLineNumber = startLineNumber;
        this.endLineNumber = endLineNumber;
    }

    String getMethodCall() {
        return methodCall;
    }

    int getStartLineNumber() {
        return startLineNumber;
    }

    int getEndLineNumber() {
        return endLineNumber;
    }
}
