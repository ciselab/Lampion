package com.github.ciselab.lampion.transformations;

import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.github.ciselab.lampion.transformations.transformers.IfTrueTransformer;
import com.github.ciselab.lampion.transformations.transformers.RandomParameterNameTransformer;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RandomParameterNameTransformerTests {
    /**
     * Some of these Tests have the following issue:
     * Checking for contains on "int a" when changing parameters can fail even if the parameter was sucessfully changed.
     * Reason for this is that the random string can start with "a" such as "aXppgKl" and then
     * string.contains("int a") is still true.
     */

    @RepeatedTest(10)
    void applyToClassWithTwoMethods_onlyOneIsAltered(){
        CtClass ast = Launcher.parseClass("class A { " +
                "int sum(int a, int b) { return a + b;} " +
                "int sam(int a, int b) { return a + b;}" +
                "}");

        CtMethod methodA = (CtMethod) ast.filterChildren(c -> c instanceof CtMethod).list().get(0);
        CtMethod methodB = (CtMethod) ast.filterChildren(c -> c instanceof CtMethod).list().get(1);

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();

        transformer.applyAtRandom(ast);

        // Check if both methods still have both parameters as initially set
        // The "," and ")" are important,
        // as otherwise the test becomes flaky if the random variable name starts with a or b
        Predicate<CtMethod> isAltered = m -> ! (m.toString().contains("int a,") && m.toString().contains("int b)"));

        boolean methodAAltered = isAltered.test(methodA);
        boolean methodBAltered = isAltered.test(methodB);

        // For debugging
        if(! methodAAltered ^ methodBAltered){
            String toCheck = ast.toString();
        }

        // The operator "^" is the XOR operator
        assertTrue(methodAAltered ^ methodBAltered);
    }

    @RepeatedTest(20)
    void applyToMethodWithTwoParameters_onlyOneIsAltered(){
        CtElement ast = sumExample();

        CtVariable varA = (CtVariable) ast.filterChildren(c -> c instanceof CtVariable).list().get(0);
        CtVariable varB = (CtVariable) ast.filterChildren(c -> c instanceof CtVariable).list().get(1);

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();

        transformer.applyAtRandom(ast);

        boolean aAltered = ! varA.toString().equals("int a");
        boolean bAltered = ! varB.toString().equals("int b");
        
        // The operator "^" is the XOR operator
        assertTrue(aAltered ^ bAltered);
    }

    @RepeatedTest(5)
    void applyTwiceToMethodWithTwoParameters_bothAreAltered(){
        CtElement ast = sumExample();

        CtVariable varA = (CtVariable) ast.filterChildren(c -> c instanceof CtVariable).list().get(0);
        CtVariable varB = (CtVariable) ast.filterChildren(c -> c instanceof CtVariable).list().get(1);

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();

        transformer.applyAtRandom(ast);
        transformer.applyAtRandom(ast);

        boolean aAltered = ! varA.toString().equals("int a");
        boolean bAltered = ! varB.toString().equals("int b");

        // For debugging
        if(! aAltered && bAltered){
            String toCheck = ast.toString();
        }

        // The operator "^" is the XOR operator
        assertTrue(aAltered && bAltered);
    }


    @Test
    void applyTwiceToMethodWithOneParameter_returnsEmptyTransformationResult(){
        CtElement ast = addOneExample();

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();

        transformer.applyAtRandom(ast);
        var result = transformer.applyAtRandom(ast);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @RepeatedTest(3)
    void applyFiveTimesToClassWithTwoMethods_returnsEmptyTransformationResult_AndAltersAllItems(){
        CtClass ast = Launcher.parseClass("class A { " +
                "int sum(int a, int b) { return a + b;} " +
                "int sam(int a, int b) { return a + b;}" +
                "}");

        CtMethod methodA = (CtMethod) ast.filterChildren(c -> c instanceof CtMethod).list().get(0);
        CtMethod methodB = (CtMethod) ast.filterChildren(c -> c instanceof CtMethod).list().get(1);

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();

        TransformationResult result = new EmptyTransformationResult();
        for(int i = 0; i<5;i++)
            result = transformer.applyAtRandom(ast);

        // Check if both methods still have both parameters as initially set
        // The "," and ")" are important,
        // as otherwise the test becomes flaky if the random variable name starts with a or b
        Predicate<CtMethod> isAltered = m -> ! (m.toString().contains("int a,") && m.toString().contains("int b)"));

        boolean methodAAltered = isAltered.test(methodA);
        boolean methodBAltered = isAltered.test(methodB);


        // The operator "^" is the XOR operator
        assertTrue(methodAAltered && methodBAltered);
        assertEquals(new EmptyTransformationResult(),result);
    }


    @Test
    void applyToMethodWithoutParameters_returnsEmptyTransformationResult(){
        CtClass ast = Launcher.parseClass("class A { " +
                "int noParams() { return 1;} " +
                "}");

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();

        var result = transformer.applyAtRandom(ast);

        assertEquals(new EmptyTransformationResult(),result);
    }


    @Test
    void applyToClassWithoutMethods_returnsEmptyTransformationResult(){
        CtClass ast = Launcher.parseClass("class A { " +
                "}");

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();

        var result = transformer.applyAtRandom(ast);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Test
    void testExclusive_isExclusiveWithNothing(){
        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();

        assertTrue(transformer.isExclusiveWith().isEmpty());
    }

    @Test
    void constraintsAreNotSatisfied_ReturnsEmptyResult(){
        CtClass emptyClass = Launcher.parseClass("class A { }");

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();
        TransformationResult result = transformer.applyAtRandom(emptyClass);

        assertEquals(new EmptyTransformationResult(), result);
    }


    @Test
    void applyToMethod_CheckTransformationResult_nameIsRandomParameterName(){
        CtElement ast = sumExample();

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertEquals("RandomParameterName",result.getTransformationName());
    }

    @Test
    void applyToMethod_CheckTransformationResult_categoriesNotEmpty(){
        CtElement ast = sumExample();

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getCategories().isEmpty());
    }

    @Test
    void applyWithoutDebugSettingOn_TransformationResultShouldHaveNoOptionalInfo(){
        CtElement ast = sumExample();

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();

        transformer.setDebug(false);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getInitialScopeOfTransformation().isPresent());
        assertFalse(result.getBeforeAfterComparison().isPresent());
    }

    @Test
    void applyWithDebugSettingOn_TransformationResultShouldHaveMoreInfo(){
        CtElement ast = sumExample();

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();

        transformer.setDebug(true);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertTrue(result.getInitialScopeOfTransformation().isPresent());
        assertTrue(result.getBeforeAfterComparison().isPresent());
    }


    static CtElement addOneExample(){
        CtClass testObject = Launcher.parseClass("class A { int addOne(int a) { return a + 1 }");

        return testObject;
    }

    static CtElement sumExample(){
        CtClass testObject = Launcher.parseClass("class A { int sum(int a, int b) { return a + b;} }");

        return testObject;
    }

}
