package com.github.ciselab.lampion.transformations;

import com.github.ciselab.lampion.transformations.transformers.RandomInlineCommentTransformer;
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

public class RandomInlineCommentTransformerTests {

    @Test
    void applyToMethod_containsComment(){
        CtElement ast = sumExample();

        RandomInlineCommentTransformer transformer = new RandomInlineCommentTransformer();

        transformer.applyAtRandom(ast);

        assertTrue(ast.toString().contains("//"));
    }

    @Test
    void applyToMethodTwice_doesNotGiveEmptyResults(){
        CtElement ast = sumExample();

        RandomInlineCommentTransformer transformer = new RandomInlineCommentTransformer();

        transformer.applyAtRandom(ast);
        var result = transformer.applyAtRandom(ast);

        assertNotEquals(new EmptyTransformationResult(), result);
    }

    @Test
    void applyToClassWithoutMethods_returnsEmptyTransformationResult(){
        CtClass ast = Launcher.parseClass("package lampion.test; class A { }");

        RandomInlineCommentTransformer transformer = new RandomInlineCommentTransformer();

        var result = transformer.applyAtRandom(ast);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Test
    void testExclusive_isExclusiveWithNothing(){
        RandomInlineCommentTransformer transformer = new RandomInlineCommentTransformer();

        assertTrue(transformer.isExclusiveWith().isEmpty());
    }


    @Test
    void applyToMethod_CheckTransformationResult_nameIsRandomInlineCommentName(){
        CtElement ast = sumExample();

        RandomInlineCommentTransformer transformer = new RandomInlineCommentTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertEquals("RandomInlineComment",result.getTransformationName());
    }

    @Test
    void applyToMethod_CheckTransformationResult_categoriesNotEmpty(){
        CtElement ast = sumExample();

        RandomInlineCommentTransformer transformer = new RandomInlineCommentTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getCategories().isEmpty());
    }

    @Test
    void applyWithoutDebugSettingOn_TransformationResultShouldHaveNoOptionalInfo(){
        CtElement ast = sumExample();

        RandomInlineCommentTransformer transformer = new RandomInlineCommentTransformer();

        transformer.setDebug(false);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getInitialScopeOfTransformation().isPresent());
        assertFalse(result.getBeforeAfterComparison().isPresent());
    }

    @Test
    void applyWithDebugSettingOn_TransformationResultShouldHaveMoreInfo(){
        CtElement ast = sumExample();

        RandomInlineCommentTransformer transformer = new RandomInlineCommentTransformer();

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
