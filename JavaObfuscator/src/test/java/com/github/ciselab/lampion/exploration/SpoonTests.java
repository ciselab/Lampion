package com.github.ciselab.lampion.exploration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.refactoring.CtRefactoring;
import spoon.refactoring.CtRenameGenericVariableRefactoring;
import spoon.refactoring.CtRenameLocalVariableRefactoring;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.CompressionType;
import spoon.support.reflect.code.CtBlockImpl;
import spoon.support.reflect.code.CtLiteralImpl;
import spoon.support.reflect.declaration.CtParameterImpl;
import spoon.support.reflect.declaration.CtTypeImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

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
 * Optionally, Spoon Launcher can be run on a folder (of the project) and builds a "CtModel" which holds all entities found.
 *
 * All Spoon Items start with Ct, which is short for "CompileTime".
 * Some CtElements are Generics of <T> where T is a placeholder for their type if known/applicable.
 * Literals can be build using the factory.createLiteral("Hello") or factory.createLiteral(6) and are correct typed.
 * Some items further have SubTypes.
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
 * In "spoonExploration_AddInlineComment()" there is an example how to add an inline comment.
 * Important: The items need a valid position, which is not trivially done by inserting it into a list of all available
 * Statements (e.g. filterChildren(instanceof Statement) and inserting the inline comment is still invisible!)
 *
 * For some items there are "refactorings" availible (mostly for renaming) which only provide interfaces except for
 * Variable Renaming (that one is fully implemented).
 * Altering method names requires to provide a new refactoring (atleast to do it properly).
 *
 * One of the "run" or "refactor" methods builds a spooned folder with the altered class.
 * There is a "buildModel" method of the Launcher, which does not need to
 *
 * TODO:
 * - Inspect what builds the "spooned" folder
 *
 * Further Examples / Reading:
 * - https://github.com/INRIA/spoon
 * - http://spoon.gforge.inria.fr/first_transformation.html introduction to transformations
 * - http://spoon.gforge.inria.fr/first_transformation.html overview of all elements
 * - http://spoon.gforge.inria.fr/ general landing page for docs
 * - https://github.com/SpoonLabs/spoon-examples secondary project that uses Spoon
 */
public class SpoonTests {

    private static String pathToTestFile = "./src/test/resources/javafiles/example.java";
    private static String pathToTestFileFolder = "./src/test/resources/javafiles";



    /*
    ================================================================================================================
                    Building, Search and Iterations
    ================================================================================================================
     */
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
    void spoonExploration_FromFileWithLauncher() throws IOException {
        // The "Run" is necessary!
        Launcher launcher = new Launcher();

        launcher.addInputResource(pathToTestFile);

        launcher.run();

        var results = launcher.getModel().filterChildren(c -> c instanceof CtMethod).list();

        assertEquals(1,results.size());
    }

    @Tag("Exploration")
    @Test
    void spoonExploration_FromFolderWithLauncher() throws IOException {
        // The "Run" is necessary!
        Launcher launcher = new Launcher();

        launcher.addInputResource(pathToTestFileFolder);

        launcher.run();

        var results = launcher.getModel().filterChildren(c -> c instanceof CtMethod).list();

        assertEquals(1,results.size());
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

    /*
    ===================================================================================================================
                        Refactorings & Transformation Tests
    ===================================================================================================================
     */
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
    void spoonExploration_AddInlineComment() throws IOException {
        CtClass containerClass = readExampleFile();

        Launcher launcher = new Launcher();
        Factory factory = launcher.getFactory();

        // Statements in the method "sum"
        var statements = containerClass.filterChildren(c -> c instanceof CtMethod)
                .filterChildren(c -> c instanceof CtStatement).list();

        var comment =  factory.createInlineComment("I am a comment!");

        CtBlockImpl block = (CtBlockImpl) statements.get(0);
        // With using the CtBlockImpl::addStatement the comment also gets it's position set, which is very important!
        // Without the sourceCodePosition, the comment stays invisible
        block.addStatement(comment);

        String result = containerClass.toString();

        assertTrue(result.contains("I am a comment!"));
    }

    @Tag("Exploration")
    @Test
    void spoonExploration_AddUnusedMethod() throws IOException {
        Launcher launcher = new Launcher();
        launcher.addInputResource(pathToTestFile);
        launcher.run();

        CtClass testObject = launcher.getModel().filterChildren(u -> u instanceof CtClass).first();
        Factory factory = launcher.getFactory();
        TypeFactory types = new TypeFactory();

        CtMethod additionalMethod = factory.createMethod();
        additionalMethod.setSimpleName("truther");
        additionalMethod.setType(types.BOOLEAN_PRIMITIVE);
        additionalMethod.addModifier(ModifierKind.STATIC);
        additionalMethod.addModifier(ModifierKind.PUBLIC);
        additionalMethod.addModifier(ModifierKind.FINAL);
        CtReturn content = factory.createReturn();
        content.setReturnedExpression(factory.createLiteral(true));
        additionalMethod.setBody(content);

        testObject.addMethod(additionalMethod);

        String result = testObject.toString();

        assertTrue(testObject.toString().contains("truther"));
        assertTrue(testObject.toString().contains("return true;"));
    }


    @Tag("Exploration")
    @Test
    void spoonExploration_AddLambdaIdentityWrapper_generalInspection(){
        /*
        This is for looking into and building the first steps of this stuff
        See below in "spoonExploration_AddLambdaIdentityWrapper_WithMapper()"
        and "wrapInLambda" how to apply it
         */
        Launcher launcher = new Launcher();
        CtClass testObject = Launcher.parseClass("class A { void addOne(int a) {return a + 1;} }");

        CtMethod testMethod = (CtMethod) testObject.getMethods().stream().findFirst().get();
        Factory factory = launcher.getFactory();
        TypeFactory types = new TypeFactory();

        List<CtLiteral> literals = testObject.filterChildren(c -> c instanceof CtLiteral).list();

        CtLiteral one = literals.get(0);
        CtLambda lambda = factory.createLambda();
        lambda.setExpression(one);
        //lambda.setType(one.getType());

        CtLocalVariable wrapped = factory.createLocalVariable(types.OBJECT,"u",lambda);

        testMethod.getBody().addStatement(wrapped);

        var test2 = factory.createCodeSnippetExpression("("+lambda.toString()+").get()");
        test2.setType(one.getType());

        String result = testObject.toString();

        return;
    }


    @Tag("Exploration")
    @Test
    void spoonExploration_AddLambdaIdentityWrapper_WithMapper(){
        CtClass testObject = Launcher.parseClass("class A { void addOne(int a) {return a + 1;} }");
        CtMethod testMethod = (CtMethod) testObject.getMethods().stream().findFirst().get();


        List<CtLiteral> literals = testMethod.filterChildren(c -> c instanceof CtLiteral).list();
        CtLiteral one = literals.get(0);

        var wrapped = wrapInLambda(one);

        one.replace(wrapped);

        String result = testObject.toString();

        assertTrue(result.contains("a + (() -> 1).get()"));
        assertFalse(result.contains("a + 1"));
    }

    private CtExpression<?> wrapInLambda(CtLiteral<?> toWrap){
        // Important: Make a clone ! Otherwise it's overwriting the initial items attributes
        Factory factory = (new Launcher()).getFactory();
        CtLambda lambda = factory.createLambda();
        lambda.setExpression(toWrap.clone());

        CtExpression wrapped = factory.createCodeSnippetExpression("("+lambda.toString()+").get()");
        wrapped.setType(toWrap.getType());
        wrapped.setPosition(toWrap.getPosition());
        wrapped.setParent(toWrap.getParent());
        return wrapped;
    }

    @Tag("Exploration")
    @Test
    void spoonExploration_WrapInIfStatement() throws IOException {
        Launcher launcher = new Launcher();
        launcher.addInputResource(pathToTestFile);
        launcher.run();

        CtMethod testObject = launcher.getModel().filterChildren(u -> u instanceof CtMethod).first();
        Factory factory = launcher.getFactory();
        TypeFactory types = new TypeFactory();

        CtBlock methodBody = testObject.getBody();

        var ifWrapper = factory.createIf();
        ifWrapper.setCondition(factory.createLiteral(true));
        ifWrapper.setThenStatement(methodBody);

        testObject.setBody(ifWrapper);

        String result = testObject.toString();

        assertTrue(result.contains("if"));
    }

    @Tag("Exploration")
    @Test
    void spoonExploration_renameParameter() throws IOException {
        Launcher launcher = new Launcher();
        launcher.addInputResource(pathToTestFile);
        launcher.run();

        CtMethod testObject = launcher.getModel().filterChildren(u -> u instanceof CtMethod).first();
        Factory factory = launcher.getFactory();
        TypeFactory types = new TypeFactory();


        List<CtVariable> localVars = testObject.filterChildren(c -> c instanceof CtVariable).list();

        CtRenameGenericVariableRefactoring refac = new CtRenameGenericVariableRefactoring();
        refac.setTarget(localVars.get(0));
        refac.setNewName("changedA");
        refac.refactor();

        String result = testObject.toString();

        assertTrue(result.contains("changedA"));
        assertFalse(result.contains("return a + b;"));
    }

    /*
    ==================================================================================================================
                                    Other Minor Tests and Stuff
    ==================================================================================================================
     */
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

        assertTrue(result.contains("class myNewName"));
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
