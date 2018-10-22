package com.vrondakis.zap;

import hudson.console.ConsoleNote;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class TaskListenerStub implements hudson.model.TaskListener {

    @Override
    public PrintStream getLogger() {
        try {
            return new PrintStream(System.out, false, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Unsupported encoding...
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void annotate(ConsoleNote consoleNote) throws IOException {

    }

    @Override
    public void hyperlink(String s, String s1) throws IOException {

    }

    @Override
    public PrintWriter error(String s) {
        return null;
    }

    @Override
    public PrintWriter error(String s, Object... objects) {
        return null;
    }

    @Override
    public PrintWriter fatalError(String s) {
        return null;
    }

    @Override
    public PrintWriter fatalError(String s, Object... objects) {
        return null;
    }
}
