package com.github.ciselab.lampion.exploration;

import com.github.ciselab.lampion.transformations.EmptyTransformationResult;
import com.github.ciselab.lampion.transformations.TransformationResult;
import com.github.ciselab.lampion.transformations.transformers.RandomParameterNameTransformer;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.refactoring.CtRenameGenericVariableRefactoring;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.support.reflect.code.CtBlockImpl;
import spoon.support.reflect.code.CtLocalVariableImpl;
import spoon.support.reflect.declaration.CtParameterImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
 * For some items there are "refactorings" available (mostly for renaming) which only provide interfaces except for
 * Variable Renaming (that one is fully implemented).
 * Altering method names requires to provide a new refactoring (atleast to do it properly).
 *
 * One of the "run" or "refactor" methods builds a spooned folder with the altered class.
 * There is a "buildModel" method of the Launcher, which does not need to write the spooned folder.
 *
 * There is a method "CtClass::compileAndReplaceSnippets" which does resolve and replace the snippets.
 * Statement snippets are kept "as is" until this method is run and have no semantic information until then.
 * However, the compileAndReplaceSnippets can fail due to weird reasons and should be used carefully?
 *
 * Further Examples / Reading:
 * - https://github.com/INRIA/spoon
 * - http://spoon.gforge.inria.fr/first_transformation.html introduction to transformations
 * - http://spoon.gforge.inria.fr/first_transformation.html overview of all elements
 * - http://spoon.gforge.inria.fr/ general landing page for docs
 * - https://github.com/SpoonLabs/spoon-examples secondary project that uses Spoon
 */
public class SpoonTests {

    private static String pathToTestFile = "./src/test/resources/javafiles/javafiles_simple/example.java";
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
    void spoonExploration_InvalidJavaMissingReferences_doesNotBreak(){
        CtClass testObject = Launcher.parseClass("class A { void addOne(int a) {return a + getOne();} }");

        assertEquals("A", testObject.getSimpleName());
        var ms = testObject.getMethods();

        return;
    }

    @Tag("Exploration")
    @Tag("File")
    @Test
    void spoonExploration_InvalidJava_MissingReferences_fromFile_doesNotBreak() throws IOException{
        String pathToTestFile = "./src/test/resources/bad_javafiles/missing_reference.java";

        Launcher launcher = new Launcher();
        launcher.addInputResource(pathToTestFile);
        launcher.buildModel();

        var testObject = launcher.getModel();

        launcher.setSourceOutputDirectory("./src/test/resources/bad_javafiles_output");
        launcher.prettyprint();

        assertTrue(Files.exists(Paths.get("./src/test/resources/bad_javafiles_output/lampion/tests/examples/Misser.java")));

        // Cleanup
        if(Files.exists(Paths.get("./src/test/resources/bad_javafiles_output"))) {
            Files.walk(Paths.get("./src/test/resources/bad_javafiles_output"))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        return;
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

        assertTrue(result.contains("->"));
        assertTrue(result.contains("get()"));
        assertFalse(result.contains("a + 1"));
    }

    private CtExpression<?> wrapInLambda(CtLiteral<?> toWrap){
        // Important: Make a clone ! Otherwise it's overwriting the initial items attributes
        Factory factory = (new Launcher()).getFactory();
        CtLambda lambda = factory.createLambda();
        lambda.setExpression(toWrap.clone());

        //Old!
        //CtExpression wrapped = factory.createCodeSnippetExpression("((Supplier<"+toWrap.getType().getSimpleName()+">)("+lambda.toString()+")).get()");
        // Working, but noisy
        CtExpression wrapped = factory.createCodeSnippetExpression("("+toWrap.getType().getSimpleName()+")((java.util.function.Supplier<?>)("+lambda.toString()+")).get()");
        wrapped.setType(toWrap.getType());
        wrapped.setPosition(toWrap.getPosition());
        wrapped.setParent(toWrap.getParent());
        return wrapped;
    }


    @Tag("Exploration")
    @Tag("Regression")
    @Test
    void spoonExploration_AddLambdaIdentityWrapper_WithMapper_RestoreLiterals(){
        // There was an issue that once the literal is replaced, it is not detected as a literal anymore.
        // There must be a way to restore its structure
        CtClass testObject = Launcher.parseClass("package test; class A { int addOne(int a) {return a + 1;} }");
        CtMethod testMethod = (CtMethod) testObject.getMethods().stream().findFirst().get();


        List<CtLiteral> literals = testMethod.filterChildren(c -> c instanceof CtLiteral).list();
        CtLiteral one = literals.get(0);

        var wrapped = wrapInLambda(one);

        one.replace(wrapped);
        // at this point, there is no literal anymore as the CtLambda "consumed it"
        assertTrue(testMethod.filterChildren(u -> u instanceof CtLiteral).list().isEmpty());

        testObject.compileAndReplaceSnippets();

        // at this point, the literal should be "restored"
        assertFalse(testMethod.filterChildren(u -> u instanceof CtLiteral).list().isEmpty());
    }


    @Tag("Exploration")
    @Tag("Regression")
    @Test
    void spoonExploration_AddLambdaIdentityWrapper_WithMapper_RestoreLiteralsFromMethodUpwards(){
        // There was an issue that once the literal is replaced, it is not detected as a literal anymore.
        // There must be a way to restore its structure
        CtClass testObject = Launcher.parseClass("package test; class A { int addOne(int a) {return a + 1;} }");
        CtMethod testMethod = (CtMethod) testObject.getMethods().stream().findFirst().get();


        List<CtLiteral> literals = testMethod.filterChildren(c -> c instanceof CtLiteral).list();
        CtLiteral one = literals.get(0);

        var wrapped = wrapInLambda(one);

        one.replace(wrapped);
        // at this point, there is no literal anymore as the CtLambda "consumed it"
        assertTrue(testMethod.filterChildren(u -> u instanceof CtLiteral).list().isEmpty());

        CtClass lookingForParent = one.getParent(p -> p instanceof CtClass);
        lookingForParent.compileAndReplaceSnippets();

        // at this point, the literal should be "restored"
        assertFalse(testMethod.filterChildren(u -> u instanceof CtLiteral).list().isEmpty());
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
    void spoonExploration_tryAddImports_fromFile_importShouldBeAdded(){
        Launcher launcher = new Launcher();
        launcher.addInputResource(pathToTestFile);
        launcher.buildModel();

        CtClass model = launcher.getModel().filterChildren(t -> t instanceof CtClass).first();
        var factory = model.getFactory();
        DefaultJavaPrettyPrinter defaultJavaPrettyPrinter = new DefaultJavaPrettyPrinter(factory.getEnvironment());

        var unit = model.getFactory().CompilationUnit().getOrCreate(model);
        unit.setImports(Arrays.asList(factory.createImport(factory.createReference("java.utils.Arraylist"))));

        var o = defaultJavaPrettyPrinter.prettyprint(unit);
        assertTrue(o.contains("import java.utils.Arraylist;"));
    }

    @Tag("Exploration")
    @Test
    void spoonExploration_tryAddImports_fromFile_WithExistingImports_importShouldBeAdded(){
        String pathToTestFile = "./src/test/resources/javafiles/javafiles_with_import/";
        Launcher launcher = new Launcher();
        launcher.addInputResource(pathToTestFile);
        launcher.buildModel();

        CtClass model = launcher.getModel().filterChildren(t -> t instanceof CtClass).first();
        var factory = model.getFactory();
        DefaultJavaPrettyPrinter defaultJavaPrettyPrinter = new DefaultJavaPrettyPrinter(factory.getEnvironment());

        var unit = model.getFactory().CompilationUnit().getOrCreate(model);
        var existingImports = unit.getImports();
        existingImports.add(factory.createImport(factory.createReference("java.utils.Arraylist")));

        var o = defaultJavaPrettyPrinter.prettyprint(unit);
        assertTrue(o.contains("import java.utils.Arraylist;"));
    }

    @Tag("Exploration")
    @Test
    void spoonExploration_WrapInIfStatementElseReturnFalse() throws IOException {
        /**
         * There is an issue with compilation of the method has a return statement.
         * If there is a return statement, and the compiler does not understand that its if(true)
         * then there will be a compilation error.
         * hence, there must be a trivial else branch returning false
         */
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
        ifWrapper.setElseStatement(factory.createBlock().addStatement(factory.createCodeSnippetStatement("return null")));

        testObject.setBody(ifWrapper);

        String result = testObject.toString();

        assertTrue(result.contains("if"));
        assertTrue(result.contains("else"));
        assertTrue(result.contains("return null;"));
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

    @Tag("Exploration")
    @Test
    void spoonExploration_add0ToLiteral() {
        CtClass testObject = Launcher.parseClass("package test; class A { int addOne(int a) {return a + 1;} }");

        CtLiteral one = (CtLiteral) testObject.filterChildren(c -> c instanceof CtLiteral).list().get(0);

        CtLiteral oneone = one.clone();

        Factory factory = one.getFactory();

        var bin = factory.createBinaryOperator();
        bin.setKind(BinaryOperatorKind.PLUS);
        bin.setLeftHandOperand(oneone);
        bin.setRightHandOperand(factory.createLiteral(0));
        bin.setType(oneone.getType());
        //bin.setParent(one.getParent());


        one.replace(bin);

        //one.replace(factory.createLiteral(2));

        testObject.updateAllParentsBelow();
        testObject.compileAndReplaceSnippets();

        var hu = testObject.toString();

        assertTrue(hu.contains("(1 + 0)"));
    }

    @Tag("Exploration")
    @Test
    void spoonExploration_add0ToVariable() {
        CtClass testObject = Launcher.parseClass("package test; class A { int addOne(int a) {return a + 1;} }");

        var uu = (CtVariableRead<Integer>) testObject.filterChildren(c -> c instanceof CtVariableRead).list().get(0);

        var threthre = uu.clone();

        Factory factory = uu.getFactory();

        CtBinaryOperator bin = factory.createBinaryOperator();
        bin.setKind(BinaryOperatorKind.PLUS);
        bin.setLeftHandOperand(threthre);
        bin.setRightHandOperand(factory.createLiteral(0));

        testObject.updateAllParentsBelow();
        testObject.compileAndReplaceSnippets();

        uu.replace(bin);

        var hu = testObject.toString();

        assertTrue(hu.contains("(a + 0)"));
    }

    @Tag("Exploration")
    @Test
    void spoonExploration_addUnusedVariableDeclaration(){
        CtClass testObject = Launcher.parseClass("package test; class A { int addOne(int a) {return a + 1;} }");

        var uu = (CtVariableRead<Integer>) testObject.filterChildren(c -> c instanceof CtVariableRead).list().get(0);

        Factory factory = uu.getFactory();

        var locvar = factory.createLocalVariable(factory.Type().INTEGER,"test2",factory.createLiteral(5));

        CtMethod m = (CtMethod) testObject.getMethods().stream().findFirst().get();
        m.getBody().getStatements().add(0,locvar);


        testObject.updateAllParentsBelow();
        testObject.compileAndReplaceSnippets();


        var hu = testObject.toString();

        assertTrue(hu.contains("test2"));
    }

    /*
    ===============================================================================================================
            System Tests
    ===============================================================================================================
     */

    @Tag("Exploration")
    @Tag("System")
    @Tag("File")
    @Test
    void spoonExploration_readAlterAndWriteElements() throws IOException {
        /**
         * Use one of the above items,
         * Run the launcher and write to a new folder in test resources
         *
         * This is a primary example of how to build the full pipeline and hence very important.
         * However, it may file with the side-effects.
         */
        String pathToOutput = "./src/test/resources/spooned";


        // Fail the test if the file existed - there needs to be a proper cleanup beforehand
        assertFalse(Files.exists(Path.of(pathToOutput,"example.java")));

        Launcher launcher = new Launcher();
        launcher.addInputResource(pathToTestFile);

        launcher.buildModel();

        // Here comes the code from ifwrapper above
        // Ignore for here
            CtMethod testObject = launcher.getModel().filterChildren(u -> u instanceof CtMethod).first();
            Factory factory = launcher.getFactory();
            TypeFactory types = new TypeFactory();

            CtBlock methodBody = testObject.getBody();

            var ifWrapper = factory.createIf();
            ifWrapper.setCondition(factory.createLiteral(true));
            ifWrapper.setThenStatement(methodBody);
            ifWrapper.setElseStatement(factory.createBlock().addStatement(factory.createCodeSnippetStatement("return null")));

            testObject.setBody(ifWrapper);
        // Here starts the interesting code again

        launcher.setSourceOutputDirectory(pathToOutput);
        launcher.prettyprint();
        //var processor = launcher.createOutputWriter();
        //processor.init();
        // processor.process();

        assertTrue(Files.exists(Path.of(pathToOutput,"example.java")));

        // CleanUp
        Files.delete(Path.of(pathToOutput,"example.java"));
        Files.delete(Path.of(pathToOutput));
    }

    /*
    ==================================================================================================================
                                    Other Minor Tests and Stuff
    ==================================================================================================================
     */
    @Tag("Exploration")
    @Test
    void spoonExploration_whatElseCanLauncherDo() {
        Launcher launcher = new Launcher();

        Factory factory = launcher.getFactory();
        CtClass aClass = factory.createClass("my.org.MyClass");

        aClass.setSimpleName("myNewName");
        CtMethod myMethod = factory.createMethod();
        aClass.addMethod(myMethod);

        String result  = aClass.toString();

        assertTrue(result.contains("class myNewName"));
    }


    @Tag("Regression")
    @Tag("Exploration")
    @RepeatedTest(3)
    void applyRenameHundredTimesToAMethod_methodShouldNotLooseParameters(){
        // There was an issue with renaming variables too often, that the parameters are dissappearing
        // With this test, it seems that the issue is somewhere in the Transformer and not in refactoring
        CtClass ast = Launcher.parseClass("class A { " +
                "int sum(int a, int b) { return a + b;} " +
                "}");

        CtMethod sumMethod = (CtMethod) ast.filterChildren(c -> c instanceof CtMethod).list().get(0);

        Random random = new Random();

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();

        for(int i = 0; i<100;i++) {
            List<CtVariable> localVars = sumMethod.filterChildren(c -> c instanceof CtVariable).list();

            CtRenameGenericVariableRefactoring refac = new CtRenameGenericVariableRefactoring();
            refac.setTarget(localVars.get(random.nextInt(sumMethod.getParameters().size())));
            refac.setNewName("var_"+i);
            refac.refactor();
        }
        String result = sumMethod.toString();

        assertFalse(result.contains("return a + b;"));
        assertEquals(2,sumMethod.getParameters().size());
    }

    @Tag("Exploration")
    @Tag("System")
    @Tag("File")
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

    @Tag("Exploration")
    @Test
    void exploreSpoon_CheckForImports(){
        Launcher launcher = new Launcher();
        launcher.addInputResource("./src/test/resources/javafiles_with_import/ImportExample.java");
        //launcher.getEnvironment().setAutoImports(true);
        launcher.buildModel();

        var model = launcher.getModel();
        var mm = model.getRootPackage();
        //.getPackage("test.lampion.examples").getType("ImportExample");

        var uhh = model.getAllTypes();

        var myCU = launcher.getFactory().CompilationUnit().getOrCreate("src/test/java/resources/javafiles_with_import");

        var imports = myCU.getImports();

        var p = launcher.getFactory().createReference("java.util.ArrayList");

        var xxi = model.getElements(nnn -> nnn instanceof CtImport);
        
        return;
    }

    @Tag("Regression")
    @Tag("Exploration")
    @Test
    public void testRemoveComments_usingPrinter_shouldNotThrowErrors(){
        // There is an issue that produces the following error
        /*
        16:37:04.071 [main] ERROR com.github.ciselab.lampion.program.Engine - Received a SpoonException while removing comments
        spoon.SpoonException: cannot insert in this context (use insertEnd?)
        at spoon.support.reflect.code.CtStatementImpl.insertBefore(CtStatementImpl.java:65) ~[spoon-core-9.1.0.jar:?]
        at spoon.support.reflect.code.CtStatementImpl.insertBefore(CtStatementImpl.java:57) ~[spoon-core-9.1.0.jar:?]
        at spoon.support.reflect.code.CtStatementImpl.insertBefore(CtStatementImpl.java:245) ~[spoon-core-9.1.0.jar:?]
        at spoon.support.compiler.SnippetCompilationHelper.addDummyStatements(SnippetCompilationHelper.java:154) ~[spoon-core-9.1.0.jar:?]
        at spoon.support.compiler.SnippetCompilationHelper.compileAndReplaceSnippetsIn(SnippetCompilationHelper.java:81) ~[spoon-core-9.1.0.jar:?]
        at spoon.support.reflect.declaration.CtTypeImpl.compileAndReplaceSnippets(CtTypeImpl.java:443) ~[spoon-core-9.1.0.jar:?]
        at com.github.ciselab.lampion.transformations.transformers.BaseTransformer.restoreAstAndImports(BaseTransformer.java:111) ~[classes/:?]
        */
        CtClass ast = Launcher.parseClass("" +
                "package something; \n" +
                "// Comment \n" +
                "class A { \n" +
                "// Comment \n" +
                "int sum(int a, int b) \n " +
                "{ \n" +
                "/* \n " +
                "*Comment \n" +
                "*/ \n" +
                "   return a + b; \n" +
                "/* Comment */\n" +
                "} \n" +
                "// Comment \n" +
                "}\n");

        ast.getFactory().getEnvironment().setCommentEnabled(false);
        ast.compileAndReplaceSnippets();

        String prettyPrinted = ast.prettyprint();

        assertFalse(prettyPrinted.contains("Comment"));
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
