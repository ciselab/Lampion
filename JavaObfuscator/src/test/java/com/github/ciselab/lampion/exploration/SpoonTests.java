package com.github.ciselab.lampion.exploration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import spoon.Launcher;
import spoon.reflect.declaration.CtClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This Class contains exploration-tests to evaluate the behaviour of the Spoon Library.
 * It is also intended to be a playground for initial implementations.
 *
 * These tests are not highly maintained as they are not run with every run of the normal build, so be a bit careful.
 *
 * Any important findings are reported in the following paragraphs.
 *
 * Findings:
 *
 *
 *
 * Further Examples:
 * - https://github.com/INRIA/spoon
 * - http://spoon.gforge.inria.fr/
 * - https://github.com/SpoonLabs/spoon-examples
 */
public class SpoonTests {

    private static String pathToTestFile = "./src/test/resources/example.java";

    @Tag("Exploration")
    @Test
    void minimalSpoonExample(){
        CtClass testObject = Launcher.parseClass("class A { void m() { System.out.println(\"yeah\");} }");

        assertNotNull(testObject);
    }

    @Tag("Exploration")
    @Test
    void minimalSpoonExample_FromFile() throws IOException {
        Path fileName = Path.of(pathToTestFile);
        String readClass = Files.readString(fileName);

        CtClass testObject = Launcher.parseClass(readClass);

        assertNotNull(testObject);
    }

}
