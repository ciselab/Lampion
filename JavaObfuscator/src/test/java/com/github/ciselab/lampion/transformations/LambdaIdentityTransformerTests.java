package com.github.ciselab.lampion.transformations;

import com.github.ciselab.lampion.transformations.transformers.LambdaIdentityTransformer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;

import static org.junit.jupiter.api.Assertions.*;

public class LambdaIdentityTransformerTests {

    @Test
    void testApplyToClassWithoutLiterals_returnsEmptyResult(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        CtElement ast = classWithoutLiteral();

        var result = transformer.applyAtRandom(ast);

        assertEquals(new EmptyTransformationResult(), result);
    }

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

    @Test
    void testApplyToClassWithLiteralsAsFields_shouldBeApplied(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        CtClass ast = Launcher.parseClass("package lampion.test; class A { public String text = \"hey\";}");

        transformer.applyAtRandom(ast);

        assertTrue(ast.toString().contains("()"));
        assertTrue(ast.toString().contains("->"));
        assertTrue(ast.toString().contains("hey"));
        assertTrue(ast.toString().contains(".get()"));
    }


    @Test
    void testApplyToClassWithLiterals_StringLiteral_shouldBeApplied(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        CtClass ast = Launcher.parseClass("package lampion.test; class A { String m(String b) {return b+\"hey\";}");

        transformer.applyAtRandom(ast);

        assertTrue(ast.toString().contains("()"));
        assertTrue(ast.toString().contains("->"));
        assertTrue(ast.toString().contains("hey"));
        assertTrue(ast.toString().contains(".get()"));
    }

    @Test
    void testApplyToClassWithMultipleLiterals_Apply20Times_shouldNotBreak(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        CtClass ast = Launcher.parseClass("package lampion.test; class A { String m(String b) {return b+\"hey\";}");

        TransformationResult lastResult = null;
        for(int i = 0; i<20;i++)
            lastResult = transformer.applyAtRandom(ast);

        assertNotNull(lastResult);
        assertNotEquals(new EmptyTransformationResult(),lastResult);
    }

    @Tag("Regression")
    @Test
    void testApplyToClassWithLiterals_applyTwice_shouldBeAppliedTwice(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        CtElement ast = addOneExample();

        transformer.applyAtRandom(ast);
        var result = transformer.applyAtRandom(ast);

        assertNotEquals(new EmptyTransformationResult(), result);
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

    @Test
    void applyToMethod_CheckTransformationResult_nameIsLambdaIdentity(){
        CtElement ast = addOneExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertEquals("LambdaIdentity",result.getTransformationName());
    }

    @Test
    void applyToMethod_CheckTransformationResult_categoriesNotEmpty(){
        CtElement ast = addOneExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getCategories().isEmpty());
    }

    @Test
    void applyWithoutDebugSettingOn_TransformationResultShouldHaveNoOptionalInfo(){
        CtElement ast = addOneExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        transformer.setDebug(false);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getInitialScopeOfTransformation().isPresent());
        assertFalse(result.getBeforeAfterComparison().isPresent());
    }

    @Test
    void applyWithDebugSettingOn_TransformationResultShouldHaveMoreInfo(){
        CtElement ast = addOneExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        transformer.setDebug(true);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertTrue(result.getInitialScopeOfTransformation().isPresent());
        assertTrue(result.getBeforeAfterComparison().isPresent());
    }


    static CtElement classWithoutLiteral(){
        CtClass testObject = Launcher.parseClass("package lampion.test; class A { int m(int b) {return b;}");

        return testObject;
    }

    static CtElement addOneExample(){
        CtClass testObject = Launcher.parseClass("package lampion.test; class A { int addOne(int a) { return a + 1;} }");

        return testObject;
    }

}
