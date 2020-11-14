package com.github.ciselab.lampion.transformations;

import com.github.ciselab.lampion.transformations.transformers.EmptyMethodTransformer;
import com.github.ciselab.lampion.transformations.transformers.RandomParameterNameTransformer;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EmptyMethodTransformerTests {

    @Test
    void applyToSumMethod_ClassShouldHaveTwoMethodsAfterwards(){
        CtClass ast = (CtClass) sumExample();

        EmptyMethodTransformer transformer = new EmptyMethodTransformer();

        transformer.applyAtRandom(ast);

        assertEquals(2, ast.getMethods().size());
    }

    @Test
    void applyToSumMethod_applyTwice_ClassShouldHaveThreeMethodsAfterwards(){
        CtClass ast = (CtClass) sumExample();

        EmptyMethodTransformer transformer = new EmptyMethodTransformer();

        transformer.applyAtRandom(ast);

        transformer.applyAtRandom(ast);

        assertEquals(3, ast.getMethods().size());
    }

    @Test
    void applyToSumMethod_methodShouldInvokeAnAdditionalStatement(){
        CtClass ast = (CtClass) sumExample();

        EmptyMethodTransformer transformer = new EmptyMethodTransformer();
        CtMethod sumMethod = (CtMethod) ast.getMethodsByName("sum").get(0);
        int initialStatements = sumMethod.getBody().getStatements().size();

        var result = transformer.applyAtRandom(ast);

        assertEquals(initialStatements+1,sumMethod.getBody().getStatements().size());
    }


    @Test
    void applyToSumMethod_applyTenTimes_OnlySumMethodIsEnriched_OtherMethodsAreAllEmpty(){
        CtClass ast = (CtClass) sumExample();

        EmptyMethodTransformer transformer = new EmptyMethodTransformer();
        CtMethod sumMethod = (CtMethod) ast.getMethodsByName("sum").get(0);
        int initialStatements = sumMethod.getBody().getStatements().size();

        for(int i = 0; i<10; i++) {
            transformer.applyAtRandom(ast);
        }

        assertEquals(initialStatements+10,sumMethod.getBody().getStatements().size());

        long emptyMethods =
                ast.getMethods().stream()
                        .filter(c -> c instanceof CtMethod)
                        .map(u -> (CtMethod) u)
                        .filter(v -> ((CtMethod<?>) v).getBody().getStatements().isEmpty())
                        .count();
        assertEquals(10,emptyMethods);
    }

    @Test
    void applyWithFullRandom_shouldGiveNonEmptyResult(){
        CtElement ast = sumExample();

        EmptyMethodTransformer transformer = new EmptyMethodTransformer();

        transformer.setFullRandomStrings(true);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertNotEquals(new EmptyTransformationResult(),result);
    }


    @Tag("Regression")
    @RepeatedTest(3)
    void applyToClassWithoutMethods_returnsEmptyTransformationResult(){
        CtClass ast = Launcher.parseClass("package lampion.test.examples; class A { " +
                "}");

        EmptyMethodTransformer transformer = new EmptyMethodTransformer();

        var result = transformer.applyAtRandom(ast);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Test
    void testExclusive_isExclusiveWithNothing(){
        EmptyMethodTransformer transformer = new EmptyMethodTransformer();

        assertTrue(transformer.isExclusiveWith().isEmpty());
    }

    @Test
    void applyToMethod_CheckTransformationResult_categoriesNotEmpty(){
        CtElement ast = sumExample();

        EmptyMethodTransformer transformer = new EmptyMethodTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getCategories().isEmpty());
    }

    @Test
    void applyWithoutDebugSettingOn_TransformationResultShouldHaveNoOptionalInfo(){
        CtElement ast = sumExample();

        EmptyMethodTransformer transformer = new EmptyMethodTransformer();

        transformer.setDebug(false);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getInitialScopeOfTransformation().isPresent());
        assertFalse(result.getBeforeAfterComparison().isPresent());
    }

    @Test
    void applyWithDebugSettingOn_TransformationResultShouldHaveMoreInfo(){
        CtElement ast = sumExample();

        EmptyMethodTransformer transformer = new EmptyMethodTransformer();

        transformer.setDebug(true);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertTrue(result.getInitialScopeOfTransformation().isPresent());
        assertTrue(result.getBeforeAfterComparison().isPresent());
    }

    static CtElement addOneExample(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { int addOne(int a) { return a + 1 }");

        return testObject;
    }

    static CtElement sumExample(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { int sum(int a, int b) { return a + b;} }");

        return testObject;
    }
}
