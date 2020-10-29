package com.github.ciselab.lampion.exploration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.CompressionType;
import spoon.support.reflect.declaration.CtParameterImpl;
import spoon.support.reflect.declaration.CtTypeImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

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
 * The Launcher can be run with a main method, which is inherited from Spoon being a normal application as well.
 * For all of Lampions cases, the "parseClass" is the correct Entrypoint.
 * Hence, The program will need a lookup from Files to Classes and a helper logic around it.
 *
 * All Spoon Items start with Ct, which is short for "CompileTime".
 * The TopLevel item, "CTModel" implements a "CtQueryable", which enables one to apply "CtQuery"
 * The CTQuery can be used to iterate over all implemented children.
 * Lambdas over CtElements fulfill CtQueries, so using "filterChildren(predicate)" is a perfectly normal thing.
 *
 * For Creation and Alternation, there are many factories provided. Factories either build structures or types,
 * See "spoonExploration_AddParameter()" and "spoonExploration_prettyPrintLauncherTest()" for how to use factories.
 * There are no "sanity checks" put in place when building the parameters,
 * so if one prints them, there might be no resulting valid java code. Solve this with rigid unit-testing.
 *
 * The CtElements often have "SetComment" and "AddComment" methods for many things such as fields, variables, parameters
 *
 * ToDo:
 * - What does CtClass<T> <T> mean ?
 * - Apply a class-level transformation and add a nonsense method
 * - Apply the following method level transformations:
 *      - Add Comment
 *      - Alter variable name
 *      - Put whole statement into If(true) block
 * - Is there anything else usable for toplevel parsing than Launcher?
 * - Can I check if the code compiles / is correct
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
        assertEquals("Example",testObject.getSimpleName());
    }

    @Tag("Exploration")
    @Test
    void spoonExploration_getAMethodFromTheTestClass() throws IOException {
        CtClass containerClass = readExampleFile();

        Set<CtMethod> methods = containerClass.getAllMethods();

        var maybeSum = methods.stream().filter(m -> m.getSimpleName().equalsIgnoreCase("sum")).findFirst();
        assertTrue(maybeSum.isPresent());
    }


    @Tag("Exploration")
    @Test
    void spoonExploration_lookAtStatements() throws IOException {
        CtClass containerClass = readExampleFile();

        Set<CtMethod> methods = containerClass.getAllMethods();

        var maybeSum = methods.stream().filter(m -> m.getSimpleName().equalsIgnoreCase("sum")).findFirst();
        var sumMethod = maybeSum.get();

        var statements = sumMethod.getBody().getStatements();
    }


    @Tag("Exploration")
    @Test
    void spoonExploration_getElementsUsingQuery() throws IOException {
        CtClass containerClass = readExampleFile();

        var methods = containerClass.filterChildren(c -> c instanceof CtMethod).list();
        var statements = containerClass.filterChildren(c -> c instanceof CtStatement).list();

        assertFalse(methods.isEmpty());
        assertFalse(statements.isEmpty());
    }

    @Tag("Exploration")
    @Test
    void spoonExploration_AddParameter() throws IOException {
        CtClass containerClass = readExampleFile();

        Set<CtMethod> methods = containerClass.getAllMethods();

        var maybeSum = methods.stream().filter(m -> m.getSimpleName().equalsIgnoreCase("sum")).findFirst();
        var sumMethod = maybeSum.get();

        Launcher launcher = new Launcher();
        Factory factory = launcher.getFactory();
        TypeFactory types = new TypeFactory();

        CtParameter parameter = new CtParameterImpl();
        parameter.setSimpleName("yolo");
        parameter.setType(types.BYTE_PRIMITIVE);

        sumMethod.addParameter(parameter);

        // Add breakpoint and debug here to inspect the result
        var result = containerClass.toString();

        assertTrue(result.contains("byte yolo"));
    }

    @Tag("Exploration")
    @Test
    void spoonExploration_whatElseCanLauncherDo() throws IOException {
        Launcher launcher = new Launcher();

        Factory factory = launcher.getFactory();
        CtClass aClass = factory.createClass("my.org.MyClass");

        aClass.setSimpleName("myNewName");
        CtMethod myMethod = factory.createMethod();
        aClass.addMethod(myMethod);

        String result  = aClass.toString();
    }

    @Tag("Exploration")
    @Test
    void spoonExploration_prettyPrintLauncherTest() throws IOException{
        Launcher launcher = new Launcher();

        launcher.setSourceOutputDirectory("./src/test/resources/prettyprinted");

        Factory factory = launcher.getFactory();
        CtClass aClass = factory.createClass("my.org.MyClass");

        aClass.setSimpleName("myNewName");
        CtMethod myMethod = factory.createMethod();
        aClass.addMethod(myMethod);


        Environment environment = launcher.getEnvironment();
        environment.setCommentEnabled(true);
        environment.setAutoImports(true);
        // the transformation must produce compilable code
        environment.setShouldCompile(true);
        launcher.prettyprint();

        assertTrue(Files.exists(Paths.get("./src/test/resources/prettyprinted/my/org/myNewName.java")));

        // CleanUp
        Files.delete(Paths.get("./src/test/resources/prettyprinted/my/org/myNewName.java"));
    }

    /*
    ==============================================================================
                        Helper Methods
    ==============================================================================
     */

    private static CtClass readExampleFile() throws IOException {
        Path fileName = Path.of(pathToTestFile);
        String readClass = Files.readString(fileName);

        CtClass testObject = Launcher.parseClass(readClass);
        return testObject;
    }
}
