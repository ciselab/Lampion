package com.github.ciselab.lampion.core.transformations;

import com.github.ciselab.lampion.core.transformations.transformers.IfFalseElseTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.IfTrueTransformer;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import spoon.Launcher;
import spoon.reflect.declaration.*;

import java.util.function.Predicate;

public class IfTrueTransformerTests {

    @Test
    void applyToMethodWithReturn_ASTshouldHaveElseBlock(){
        CtElement ast = sumExample();

        IfTrueTransformer transformer = new IfTrueTransformer();

        transformer.applyAtRandom(ast);

        assertTrue(ast.toString().contains("if (true)"));
        assertTrue(ast.toString().contains("else"));
        assertTrue(ast.toString().contains("return 0;"));
    }

    @Test
    void applyToMethodWithoutReturn_ASTshouldHaveNoElseBlock(){
        CtElement ast = classWithoutReturnMethod();

        IfTrueTransformer transformer = new IfTrueTransformer();

        transformer.applyAtRandom(ast);

        assertTrue(ast.toString().contains("if (true)"));
        assertFalse(ast.toString().contains("else"));
        assertFalse(ast.toString().contains("return null;"));
    }

    @Tag("Regression")
    @Test
    void applyInMethod_ShouldHaveClassParent(){
        // This appeared after adding either the Lambda Transformer or IfFalseElse Transformer
        // There was an issue that there was no parent method element which (can) be true for lambdas
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { double sum(double a, double b) { return a + b;} }");

        IfTrueTransformer transformer = new IfTrueTransformer();

        var result = transformer.applyAtRandom(testObject);

        var classParent = result.getTransformedElement().getParent(u -> u instanceof CtClass);

        assertNotNull(classParent);
    }

    @Tag("Regression")
    @Test
    void applyToMethodWithFloatReturn_ElseBlockShouldHave0fInIt(){
        CtClass testObject = Launcher.parseClass(
                "package lampion.test.examples; class A { float sum(float a, float b) { return a + b;} }");

        IfTrueTransformer transformer = new IfTrueTransformer();

        transformer.applyAtRandom(testObject);

        assertTrue(testObject.toString().contains("if (true)"));
        assertTrue(testObject.toString().contains("return 0.0F;"));
        assertFalse(testObject.toString().contains("return null;"));
    }

    @Tag("Regression")
    @Test
    void applyToMethodWithCharReturn_ElseBlockShouldHaveCharacterMinValueInIt(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { char getA() { return 'a';} }");

        IfTrueTransformer transformer = new IfTrueTransformer();

        transformer.applyAtRandom(testObject);

        assertTrue(testObject.toString().contains("if (true)"));
        assertTrue(testObject.toString().contains("return Character.MIN_VALUE;"));
        assertFalse(testObject.toString().contains("return null;"));
    }

    @Tag("Regression")
    @Test
    void applyToMethodWithCharReturn_ElseBlockShouldCompile(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { char getA() { return 'a';} }");

        IfTrueTransformer transformer = new IfTrueTransformer();

        transformer.setTryingToCompile(true);

        // This could throw an error
        transformer.applyAtRandom(testObject);
        // Pass test if no error is thrown
        return;
    }

    @Tag("Regression")
    @Test
    void applyToMethodWithDoubleReturn_ElseBlockShouldHave0dInIt(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { double sum(double a, double b) { return a + b;} }");

        IfTrueTransformer transformer = new IfTrueTransformer();

        transformer.applyAtRandom(testObject);

        assertTrue(testObject.toString().contains("if (true)"));
        assertTrue(testObject.toString().contains("return 0.0;"));
        assertFalse(testObject.toString().contains("return null;"));
    }

    @Tag("Regression")
    @Test
    void applyToMethodWithStringReturn_ElseBlockShouldReturnNull(){
        CtClass testObject = Launcher.parseClass(
                "package lampion.test.examples; class A { String sum(String a, String b) { return a + b;} }");

        IfTrueTransformer transformer = new IfTrueTransformer();

        transformer.applyAtRandom(testObject);

        assertTrue(testObject.toString().contains("if (true)"));
        assertTrue(testObject.toString().contains("return null;"));
    }

    @Tag("Regression")
    @Test
    void applyToMethodWithFloatReturn_ShouldCompile(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { float sum(float a, float b) { return a + b;} }");

        IfTrueTransformer transformer = new IfTrueTransformer();

        transformer.applyAtRandom(testObject);

        testObject.compileAndReplaceSnippets();
    }

    @RepeatedTest(10)
    void applyToClassWithTwoMethods_onlyOneIsAltered(){
        CtClass ast = Launcher.parseClass("package lampion.test.examples; class A { " +
                "int sum(int a, int b) { return a + b;} " +
                "void some(){System.out.println(\"hey!\");}" +
                "}");

        CtMethod methodA = (CtMethod) ast.filterChildren(c -> c instanceof CtMethod).list().get(0);
        CtMethod methodB = (CtMethod) ast.filterChildren(c -> c instanceof CtMethod).list().get(1);

        IfTrueTransformer transformer = new IfTrueTransformer();

        transformer.applyAtRandom(ast);

        Predicate<CtMethod> isAltered = m -> m.toString().contains("if (true)");

        boolean methodAAltered = isAltered.test(methodA);
        boolean methodBAltered = isAltered.test(methodB);

        // The operator "^" is the XOR operator
        assertTrue(methodAAltered ^ methodBAltered);
    }

    @Test
    void testApplyTo_ClassWithNoMethods_ShouldNotAlterAnything(){
        CtClass ast = Launcher.parseClass("package lampion.test.examples; \n " +
                "class A { \n" +
                "}");

        IfTrueTransformer transformer = new IfTrueTransformer(25);
        transformer.applyAtRandom(ast);

        Predicate<CtElement> isAltered = m -> m.toString().contains("if (true)");
        assertFalse(isAltered.test(ast));
    }

    @Test
    void testApplyTo_ClassWithNoMethods_ShouldReturnEmptyResult(){
        CtClass ast = Launcher.parseClass("package lampion.test.examples; \n " +
                "class A { \n" +
                "}");

        IfTrueTransformer transformer = new IfTrueTransformer(25);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Test
    void testExclusive_isExclusiveWithNothing(){
        IfTrueTransformer transformer = new IfTrueTransformer();

        assertTrue(transformer.isExclusiveWith().isEmpty());
    }

    @Test
    void constraintsAreNotSatisfied_ReturnsEmptyResult(){
        CtClass emptyClass = Launcher.parseClass("class A { }");

        IfTrueTransformer transformer = new IfTrueTransformer();
        TransformationResult result = transformer.applyAtRandom(emptyClass);

        assertEquals(new EmptyTransformationResult(), result);
    }

    @Test
    void applyToMethod_CheckTransformationResult_ElementInResultIsNotAltered(){
        CtElement ast = sumExample();

        IfTrueTransformer transformer = new IfTrueTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        String toCheck = result.getTransformedElement().toString();

        assertFalse(toCheck.contains("if(true)"));
        assertFalse(toCheck.contains("else"));
        assertFalse(toCheck.contains("return null;"));
    }

    @Test
    void applyToMethod_CheckTransformationResult_nameIsIfTrue(){
        CtElement ast = sumExample();

        IfTrueTransformer transformer = new IfTrueTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertEquals("IfTrue",result.getTransformationName());
    }

    @Test
    void applyToMethod_CheckTransformationResult_categoriesNotEmpty(){
        CtElement ast = sumExample();

        IfTrueTransformer transformer = new IfTrueTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getCategories().isEmpty());
    }

    @Test
    void applyWithoutDebugSettingOn_TransformationResultShouldHaveNoOptionalInfo(){
        CtElement ast = sumExample();

        IfTrueTransformer transformer = new IfTrueTransformer();

        transformer.setDebug(false);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getInitialScopeOfTransformation().isPresent());
        assertFalse(result.getBeforeAfterComparison().isPresent());
    }

    @Test
    void applyWithDebugSettingOn_TransformationResultShouldHaveMoreInfo(){
        CtElement ast = sumExample();

        IfTrueTransformer transformer = new IfTrueTransformer();

        transformer.setDebug(true);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertTrue(result.getInitialScopeOfTransformation().isPresent());
        assertTrue(result.getBeforeAfterComparison().isPresent());
    }


    @Tag("Regression")
    @Test
    void applyToMethodWithShortReturn_ifBlockShouldCompile(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { short getA() { return 1;} }");

        IfTrueTransformer transformer = new IfTrueTransformer();

        transformer.setTryingToCompile(true);

        // This could throw an error
        transformer.applyAtRandom(testObject);
        // Pass test if no error is thrown
        return;
    }

    @Tag("Regression")
    @Test
    void applyToMethodWithByteReturn_ifBlockShouldCompile(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { byte getA() { return 1;} }");

        IfTrueTransformer transformer = new IfTrueTransformer();

        transformer.setTryingToCompile(true);

        // This could throw an error
        transformer.applyAtRandom(testObject);
        // Pass test if no error is thrown
        return;
    }

    @Tag("Regression")
    @Test
    void applyToMethodWithLongReturn_ifBlockShouldCompile(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { long getA() { return 1;} }");

        IfTrueTransformer transformer = new IfTrueTransformer();

        transformer.setTryingToCompile(true);

        // This could throw an error
        transformer.applyAtRandom(testObject);
        // Pass test if no error is thrown
        return;
    }

    @Test
    void applyToMethodWithShortReturn_elseBlockShouldHaveDefaultValue(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { short getA() { return 1;} }");

        IfTrueTransformer transformer = new IfTrueTransformer();

        transformer.applyAtRandom(testObject);
        assertTrue(testObject.toString().contains("return 0;"));
    }

    @Test
    void applyToMethodWithByteReturn_elseBlockShouldHaveDefaultValue(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { byte getA() { return 1;} }");

        IfTrueTransformer transformer = new IfTrueTransformer();

        transformer.applyAtRandom(testObject);

        assertTrue(testObject.toString().contains("return 0;"));
    }

    @Test
    void applyToMethodWithLongReturn_elseBlockShouldHaveDefaultValue(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { long getA() { return 1;} }");

        IfTrueTransformer transformer = new IfTrueTransformer();

        transformer.applyAtRandom(testObject);
        assertTrue(testObject.toString().contains("return 0L;"));
    }


    static CtElement classWithoutReturnMethod(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { void m() { System.out.println(\"yeah\");} }");

        return testObject;
    }

    static CtElement sumExample(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { int sum(int a, int b) { return a + b;} }");

        return testObject;
    }

}
