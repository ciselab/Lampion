package com.github.ciselab.lampion.transformations;

import com.github.ciselab.lampion.transformations.transformers.IfFalseElseTransformer;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

public class IfFalseElseTransformerTests {

    @Test
    void applyToMethodWithReturn_ASTshouldHaveElseBlock(){
        CtElement ast = sumExample();

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.applyAtRandom(ast);

        assertTrue(ast.toString().contains("if (false)"));
        assertTrue(ast.toString().contains("else"));
        assertTrue(ast.toString().contains("return 0;"));
    }

    @Test
    void applyToMethodWithoutReturn_ASTshouldHaveElseBlock(){
        CtElement ast = classWithoutReturnMethod();

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.applyAtRandom(ast);

        assertTrue(ast.toString().contains("if (false)"));
        assertTrue(ast.toString().contains("else"));
        assertFalse(ast.toString().contains("return null;"));
    }


    @Tag("Regression")
    @Test
    void applyToMethodWithFloatReturn_ElseBlockShouldHave0fInIt(){
        CtClass testObject = Launcher.parseClass(
                "package lampion.test.examples; class A { float sum(float a, float b) { return a + b;} }");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.applyAtRandom(testObject);

        assertTrue(testObject.toString().contains("if (false)"));
        assertTrue(testObject.toString().contains("return 0.0F;"));
        assertFalse(testObject.toString().contains("return null;"));
    }

    @Tag("Regression")
    @Test
    void applyToMethodWithDoubleReturn_ElseBlockShouldHave0dInIt(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { double sum(double a, double b) { return a + b;} }");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.applyAtRandom(testObject);

        assertTrue(testObject.toString().contains("if (false)"));
        assertTrue(testObject.toString().contains("return 0.0;"));
        assertFalse(testObject.toString().contains("return null;"));
    }

    @Tag("Regression")
    @Test
    void applyInMethod_ShouldHaveClassParent(){
        // This appeared after adding either the Lambda Transformer or IfFalseElse Transformer
        // There was an issue that there was no parent method element which (can) be true for lambdas
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { double sum(double a, double b) { return a + b;} }");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        var result = transformer.applyAtRandom(testObject);

        var classParent = result.getTransformedElement().getParent(u -> u instanceof CtClass);

        assertNotNull(classParent);
    }

    @Tag("Regression")
    @Test
    void applyInMethod_changedElementShouldBeMethod_andHaveSimpleName(){
        // This appeared after adding either the Lambda Transformer or IfFalseElse Transformer
        // There was an issue that there was no parent method element which (can) be true for lambdas
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { double sum(double a, double b) { return a + b;} }");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        var result = transformer.applyAtRandom(testObject);

        var elem = result.getTransformedElement();

        assertTrue(elem instanceof CtMethod);

        assertNotNull(((CtMethod<?>) elem).getSimpleName());
    }

    @Tag("Regression")
    @Test
    void applyToMethodWithStringReturn_ElseBlockShouldReturnNull(){
        CtClass testObject = Launcher.parseClass(
                "package lampion.test.examples; class A { String sum(String a, String b) { return a + b;} }");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.applyAtRandom(testObject);

        assertTrue(testObject.toString().contains("if (false)"));
        assertTrue(testObject.toString().contains("return null;"));
    }

    @Tag("Regression")
    @Test
    void applyToMethodWithFloatReturn_ShouldCompile(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { float sum(float a, float b) { return a + b;} }");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.applyAtRandom(testObject);

        testObject.compileAndReplaceSnippets();
    }

    @RepeatedTest(5)
    void applyToClassWithTwoMethods_onlyOneIsAltered(){
        CtClass ast = Launcher.parseClass("package lampion.test.examples; class A { " +
                "int sum(int a, int b) { return a + b;} " +
                "void some(){System.out.println(\"hey!\");}" +
                "}");

        CtMethod methodA = (CtMethod) ast.filterChildren(c -> c instanceof CtMethod).list().get(0);
        CtMethod methodB = (CtMethod) ast.filterChildren(c -> c instanceof CtMethod).list().get(1);

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.applyAtRandom(ast);

        Predicate<CtMethod> isAltered = m -> m.toString().contains("if (false)");

        boolean methodAAltered = isAltered.test(methodA);
        boolean methodBAltered = isAltered.test(methodB);

        // The operator "^" is the XOR operator
        assertTrue(methodAAltered ^ methodBAltered);
    }

    @Test
    void testExclusive_isExclusiveWithNothing(){
        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        assertTrue(transformer.isExclusiveWith().isEmpty());
    }

    @Test
    void constraintsAreNotSatisfied_ReturnsEmptyResult(){
        CtClass emptyClass = Launcher.parseClass("class A { }");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();
        TransformationResult result = transformer.applyAtRandom(emptyClass);

        assertEquals(new EmptyTransformationResult(), result);
    }

    @Test
    void applyToMethod_CheckTransformationResult_ElementInResultIsNotAltered(){
        CtElement ast = sumExample();

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        String toCheck = result.getTransformedElement().toString();

        assertFalse(toCheck.contains("if(false)"));
        assertFalse(toCheck.contains("else"));
        assertFalse(toCheck.contains("return null;"));
    }

    @Test
    void applyToMethod_CheckTransformationResult_nameIsIfTrue(){
        CtElement ast = sumExample();

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertEquals("IfFalseElse",result.getTransformationName());
    }

    @Test
    void applyToMethod_CheckTransformationResult_categoriesNotEmpty(){
        CtElement ast = sumExample();

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getCategories().isEmpty());
    }

    @Test
    void applyWithoutDebugSettingOn_TransformationResultShouldHaveNoOptionalInfo(){
        CtElement ast = sumExample();

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.setDebug(false);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getInitialScopeOfTransformation().isPresent());
        assertFalse(result.getBeforeAfterComparison().isPresent());
    }

    @Test
    void applyWithDebugSettingOn_TransformationResultShouldHaveMoreInfo(){
        CtElement ast = sumExample();

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.setDebug(true);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertTrue(result.getInitialScopeOfTransformation().isPresent());
        assertTrue(result.getBeforeAfterComparison().isPresent());
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
