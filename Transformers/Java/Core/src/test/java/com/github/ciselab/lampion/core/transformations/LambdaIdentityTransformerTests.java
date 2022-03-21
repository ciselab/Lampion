package com.github.ciselab.lampion.core.transformations;

import com.github.ciselab.lampion.core.transformations.transformers.LambdaIdentityTransformer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;

import static org.junit.jupiter.api.Assertions.*;

public class LambdaIdentityTransformerTests {

    @Tag("File")
    @Test
    void testApplyToClassWithoutLiterals_returnsEmptyResult(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        CtElement ast = classWithoutLiteral();

        var result = transformer.applyAtRandom(ast);

        assertEquals(new EmptyTransformationResult(), result);
    }

    @Tag("File")
    @Test
    void testApplyToClassWithLiterals_shouldBeApplied(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        CtElement ast = addOneExample();

        transformer.applyAtRandom(ast);

        assertTrue(ast.toString().contains("()"));
        assertTrue(ast.toString().contains("->"));
        assertTrue(ast.toString().contains("1"));
        assertTrue(ast.toString().contains(".get()"));
    }

    @Tag("File")
    @Test
    void testApplyToClassWithLiteralsAsFields_shouldBeApplied(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        CtClass ast = (CtClass) heyExample();

        transformer.applyAtRandom(ast);

        assertTrue(ast.toString().contains("()"));
        assertTrue(ast.toString().contains("->"));
        assertTrue(ast.toString().contains("hey"));
        assertTrue(ast.toString().contains(".get()"));
    }


    @Tag("File")
    @Test
    void testApplyToClassWithLiterals_StringLiteral_shouldBeApplied(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        CtClass ast = (CtClass) bPlusHeyExample();

        transformer.applyAtRandom(ast);

        assertTrue(ast.toString().contains("()"));
        assertTrue(ast.toString().contains("->"));
        assertTrue(ast.toString().contains("hey"));
        assertTrue(ast.toString().contains(".get()"));
    }

    @Tag("File")
    @Test
    void testApplyToClassWithMultipleLiterals_Apply20Times_shouldNotBreak(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        CtClass ast = (CtClass) bPlusHeyExample();

        TransformationResult lastResult = null;
        for(int i = 0; i<20;i++)
            lastResult = transformer.applyAtRandom(ast);

        assertNotNull(lastResult);
        assertNotEquals(new EmptyTransformationResult(),lastResult);
    }

    @Tag("Regression")
    @Tag("File")
    @Test
    void testApplyToClassWithLiterals_applyTwice_shouldBeAppliedTwice(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();
        transformer.setSetsAutoImports(true);

        CtElement ast = addOneExample();

        transformer.applyAtRandom(ast);
        var result = transformer.applyAtRandom(ast);

        assertNotEquals(new EmptyTransformationResult(), result);
    }

    @Tag("Regression")
    @Tag("File")
    @Test
    void testApplyToClassWithLiterals_applyTwice_noAutoImports_shouldBeAppliedTwice(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();
        transformer.setSetsAutoImports(false);

        CtElement ast = addOneExample();

        transformer.applyAtRandom(ast);
        var result = transformer.applyAtRandom(ast);

        assertNotEquals(new EmptyTransformationResult(), result);
    }

    @Tag("Regression")
    @Tag("File")
    @Test
    void testApplyToClassWithLiterals_applyTwice_noAutoImports_NoCompile_shouldBeAppliedOnce(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();
        transformer.setSetsAutoImports(false);
        transformer.setTryingToCompile(false);

        CtElement ast = addOneExample();

        transformer.applyAtRandom(ast);
        var result = transformer.applyAtRandom(ast);

        assertEquals(new EmptyTransformationResult(), result);
    }

    @Test
    void testExclusive_isExclusiveWithNothing(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        assertTrue(transformer.isExclusiveWith().isEmpty());
    }

    @Tag("Regression")
    @Test
    void applyOnGlobalVariable_ShouldNotHaveMethodParent_ShouldHaveClassParent(){
        // This appeared after adding either the Lambda Transformer or IfFalseElse Transformer
        // There was an issue that there was no parent method element which (can) be true for lambdas

        CtElement ast = Launcher.parseClass("package lampion.test.examples; class A {" +
                "int a = 2;" +
                "}");

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        var methodParent = result.getTransformedElement().getParent(u -> u instanceof CtMethod);
        var classParent = result.getTransformedElement().getParent(u -> u instanceof CtClass);

        assertNull(methodParent);
        assertNotNull(classParent);
    }

    @Tag("File")
    @Tag("Regression")
    @Test
    void applyInMethod_ShouldHaveMethodParent_ShouldHaveClassParent(){
        // This appeared after adding either the Lambda Transformer or IfFalseElse Transformer
        // There was an issue that there was no parent method element which (can) be true for lambdas

        CtElement ast = addOneExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        var methodParent = result.getTransformedElement().getParent(u -> u instanceof CtMethod);
        var classParent = result.getTransformedElement().getParent(u -> u instanceof CtClass);

        assertNotNull(methodParent);
        assertNotNull(classParent);
    }

    @Test
    void constraintsAreNotSatisfied_ReturnsEmptyResult(){
        CtClass emptyClass = Launcher.parseClass("class A { }");

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();
        TransformationResult result = transformer.applyAtRandom(emptyClass);

        assertEquals(new EmptyTransformationResult(), result);
    }

    @Tag("File")
    @Test
    void applyToMethod_CheckTransformationResult_nameIsLambdaIdentity(){
        CtElement ast = addOneExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertEquals("LambdaIdentity",result.getTransformationName());
    }

    @Tag("File")
    @Test
    void applyToMethod_CheckTransformationResult_categoriesNotEmpty(){
        CtElement ast = addOneExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getCategories().isEmpty());
    }

    @Tag("File")
    @Test
    void applyWithoutDebugSettingOn_TransformationResultShouldHaveNoOptionalInfo(){
        CtElement ast = addOneExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        transformer.setDebug(false);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getInitialScopeOfTransformation().isPresent());
        assertFalse(result.getBeforeAfterComparison().isPresent());
    }

    @Tag("File")
    @Test
    void applyWithDebugSettingOn_TransformationResultShouldHaveMoreInfo(){
        CtElement ast = addOneExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        transformer.setDebug(true);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertTrue(result.getInitialScopeOfTransformation().isPresent());
        assertTrue(result.getBeforeAfterComparison().isPresent());
    }

    @Tag("File")
    @Test
    void applyToAddOneExample_shouldHaveImportInPrettyPrinted(){
        CtClass ast = (CtClass) addOneExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        var unit = ast.getFactory().CompilationUnit().getOrCreate(ast);

        //The normal "toString" has no imports, so the pretty printer is needed
        DefaultJavaPrettyPrinter defaultJavaPrettyPrinter = new DefaultJavaPrettyPrinter(unit.getFactory().getEnvironment());

        var prettyPrintedFile = defaultJavaPrettyPrinter.prettyprint(unit);
        assertTrue(prettyPrintedFile.contains("import java.util.function.Supplier;"));
    }


    @Tag("File")
    @Test
    void applyToAddOneExample_checkImports_ShouldHaveOne(){
        CtClass ast = (CtClass) addOneExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        var unit = ast.getFactory().CompilationUnit().getOrCreate(ast);

        assertEquals(1,unit.getImports().size());
    }

    @Tag("File")
    @Test
    void applyToAddOneExample_applyTwice_checkImports_ShouldHaveOne(){
        CtClass ast = (CtClass) addOneExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        transformer.applyAtRandom(ast);
        transformer.applyAtRandom(ast);

        var unit = ast.getFactory().CompilationUnit().getOrCreate(ast);

        assertEquals(1,unit.getImports().size());
    }


    @Tag("File")
    @Test
    void applyToHeyExample_shouldHaveImportInPrettyPrinted(){
        CtClass ast = (CtClass) heyExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        var unit = ast.getFactory().CompilationUnit().getOrCreate(ast);

        //The normal "toString" has no imports, so the pretty printer is needed
        DefaultJavaPrettyPrinter defaultJavaPrettyPrinter = new DefaultJavaPrettyPrinter(unit.getFactory().getEnvironment());

        var prettyPrintedFile = defaultJavaPrettyPrinter.prettyprint(unit);
        assertTrue(prettyPrintedFile.contains("import java.util.function.Supplier;"));
    }


    @Tag("File")
    @Test
    void applyToExistingImportsExample_shouldHaveSupplierImportInPrettyPrinted(){
        CtClass ast = (CtClass) existingImportExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        var unit = ast.getFactory().CompilationUnit().getOrCreate(ast);

        //The normal "toString" has no imports, so the pretty printer is needed
        DefaultJavaPrettyPrinter defaultJavaPrettyPrinter = new DefaultJavaPrettyPrinter(unit.getFactory().getEnvironment());

        var prettyPrintedFile = defaultJavaPrettyPrinter.prettyprint(unit);
        assertTrue(prettyPrintedFile.contains("import java.util.function.Supplier;"));
    }

    @Tag("File")
    @Test
    void applyToExistingImportsExample_shouldHaveOldImportInPrettyPrinted(){
        CtClass ast = (CtClass) existingImportExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        var unit = ast.getFactory().CompilationUnit().getOrCreate(ast);

        //The normal "toString" has no imports, so the pretty printer is needed
        DefaultJavaPrettyPrinter defaultJavaPrettyPrinter = new DefaultJavaPrettyPrinter(unit.getFactory().getEnvironment());

        var prettyPrintedFile = defaultJavaPrettyPrinter.prettyprint(unit);
        assertTrue(prettyPrintedFile.contains("import java.util.Hashset;"));
    }

    static CtElement classWithoutLiteral(){
        String pathToTestFile = "./src/test/resources/javafiles/javafiles_for_lambda_identity_tests/NoLiteralExample.java";

        Launcher launcher = new Launcher();
        launcher.addInputResource(pathToTestFile);
        launcher.buildModel();

        CtClass testObject = launcher.getModel().filterChildren(t -> t instanceof CtClass).first();

        return testObject;
    }

    static CtElement heyExample(){
        String pathToTestFile = "./src/test/resources/javafiles/javafiles_for_lambda_identity_tests/HeyExample.java";

        Launcher launcher = new Launcher();
        launcher.addInputResource(pathToTestFile);
        launcher.buildModel();

        CtClass testObject = launcher.getModel().filterChildren(t -> t instanceof CtClass).first();

        return testObject;
    }

    static CtElement bPlusHeyExample(){
        String pathToTestFile = "./src/test/resources/javafiles/javafiles_for_lambda_identity_tests/BPlusHeyExample.java";

        Launcher launcher = new Launcher();
        launcher.addInputResource(pathToTestFile);
        launcher.buildModel();

        CtClass testObject = launcher.getModel().filterChildren(t -> t instanceof CtClass).first();

        return testObject;
    }

    static CtElement addOneExample(){
        String pathToTestFile = "./src/test/resources/javafiles/javafiles_for_lambda_identity_tests/AddOneExample.java";

        Launcher launcher = new Launcher();
        launcher.addInputResource(pathToTestFile);
        launcher.buildModel();

        CtClass testObject = launcher.getModel().filterChildren(t -> t instanceof CtClass).first();

        return testObject;
    }

    static CtElement existingImportExample(){
        String pathToTestFile = "./src/test/resources/javafiles/javafiles_for_lambda_identity_tests/ExistingImportExample.java";

        Launcher launcher = new Launcher();
        launcher.addInputResource(pathToTestFile);
        launcher.buildModel();

        CtClass testObject = launcher.getModel().filterChildren(t -> t instanceof CtClass).first();

        return testObject;
    }

}
