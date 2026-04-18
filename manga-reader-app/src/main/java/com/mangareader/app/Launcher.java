package com.mangareader.app;

/**
 * Entry point that doesn't extend Application.
 * Required for running shaded JavaFX jars on some systems.
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
