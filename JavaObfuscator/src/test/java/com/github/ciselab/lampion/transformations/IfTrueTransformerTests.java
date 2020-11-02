package com.github.ciselab.lampion.transformations;

import org.junit.jupiter.api.RepeatedTest;
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
        assertTrue(ast.toString().contains("return null;"));
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

    @RepeatedTest(10)
    void applyToClassWithTwoMethods_onlyOneIsAltered(){
        CtClass ast = Launcher.parseClass("class A { " +
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


    static CtElement classWithoutReturnMethod(){
        CtClass testObject = Launcher.parseClass("class A { void m() { System.out.println(\"yeah\");} }");

        return testObject;
    }

    static CtElement sumExample(){
        CtClass testObject = Launcher.parseClass("class A { int sum(int a, int b) { return a + b;} }");

        return testObject;
    }

}
