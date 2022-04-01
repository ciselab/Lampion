package com.github.ciselab.lampion.core.transformations;

import com.github.ciselab.lampion.core.transformations.transformers.RandomInlineCommentTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.EmptyMethodTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.RandomInlineCommentTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.RandomParameterNameTransformer;
import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;

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
    void applyToClassWithoutMethods_attempt2_returnsEmptyTransformationResult(){
        CtClass ast = Launcher.parseClass("package lampion.test; \n " +
                "class A {\n " +
                "}");

        RandomInlineCommentTransformer transformer = new RandomInlineCommentTransformer();

        var result = transformer.applyAtRandom(ast);

        assertEquals(new EmptyTransformationResult(),result);
    }


    @Test
    void applyToClassWithOneEmptyMethod_returnsNonEmptyTransformationResult(){
        CtClass ast = Launcher.parseClass("package lampion.test; \n " +
                "class A {\n " +
                "public void some() { \n" +
                        "}\n" +
                "}");

        RandomInlineCommentTransformer transformer = new RandomInlineCommentTransformer(2);

        var result = transformer.applyAtRandom(ast);

        assertNotEquals(new EmptyTransformationResult(),result);
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

    @Test
    void applyWithFullRandom_shouldGiveNonEmptyResult(){
        CtElement ast = sumExample();

        RandomInlineCommentTransformer transformer = new RandomInlineCommentTransformer();

        transformer.setFullRandomStrings(true);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertNotEquals(new EmptyTransformationResult(),result);
    }


    @Test
    void testGetStringRandomness_onFullRandom_ShouldGiveFullRandom(){
        RandomInlineCommentTransformer transformer = new RandomInlineCommentTransformer();
        transformer.setFullRandomStrings(true);

        assertTrue(transformer.isFullRandomStrings());
    }

    @Test
    void testGetStringRandomness_onPseudoRandom_ShouldGivePseudoRandom(){
        RandomInlineCommentTransformer transformer = new RandomInlineCommentTransformer();
        transformer.setFullRandomStrings(false);

        assertFalse(transformer.isFullRandomStrings());
    }

    /*
    ========================================================
                   Equality & HashCode Tests
    ========================================================
     */

    @Test
    void testEquals_Reflexivity(){
        RandomInlineCommentTransformer transformer = new RandomInlineCommentTransformer(2022);

        assertEquals(transformer,transformer);
    }

    @Test
    void testEquals_TwoTransformers_differentSeeds_areNotEquals(){
        RandomInlineCommentTransformer t1 = new RandomInlineCommentTransformer(1);
        RandomInlineCommentTransformer t2 = new RandomInlineCommentTransformer(2);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoTransformers_differentSeeds_seedsAreChangedAfterCreation_areNotEquals(){
        RandomInlineCommentTransformer t1 = new RandomInlineCommentTransformer(1);
        RandomInlineCommentTransformer t2 = new RandomInlineCommentTransformer(1);
        t2.setSeed(2);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoTransformers_differentTryingToCompile_areNotEquals(){
        RandomInlineCommentTransformer t1 = new RandomInlineCommentTransformer(1);
        t1.setTryingToCompile(false);
        RandomInlineCommentTransformer t2 = new RandomInlineCommentTransformer(1);
        t2.setTryingToCompile(true);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoTransformers_differentAutoImports_areNotEquals(){
        RandomInlineCommentTransformer t1 = new RandomInlineCommentTransformer(1);
        t1.setSetsAutoImports(false);
        RandomInlineCommentTransformer t2 = new RandomInlineCommentTransformer(1);
        t2.setSetsAutoImports(true);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoFreshTransformers_areEqual(){
        RandomInlineCommentTransformer t1 = new RandomInlineCommentTransformer(5);
        RandomInlineCommentTransformer t2 = new RandomInlineCommentTransformer(5);

        assertEquals(t1,t2);
    }


    @Test
    void testEquals_TransformerWithDifferentStringRandomness_areNotEqual(){
        RandomInlineCommentTransformer t1 = new RandomInlineCommentTransformer(1);
        t1.setFullRandomStrings(true);
        RandomInlineCommentTransformer t2 = new RandomInlineCommentTransformer(1);
        t2.setFullRandomStrings(false);

        assertNotEquals(t1,t2);
    }

    @Test
    void testHashCode_FreshTransformer_isNotNull(){
        RandomInlineCommentTransformer transformer = new RandomInlineCommentTransformer(10);

        int result = transformer.hashCode();

        assertNotNull(result);
        assertNotEquals(0,result);
    }

    @Test
    void testHashCode_TransformerWithSameSeeds_haveSameHashCode(){
        RandomInlineCommentTransformer t1 = new RandomInlineCommentTransformer(1);
        RandomInlineCommentTransformer t2 = new RandomInlineCommentTransformer(1);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithDifferentStringRandomness_haveSameDifferentHashCode(){
        RandomInlineCommentTransformer t1 = new RandomInlineCommentTransformer(1);
        t1.setFullRandomStrings(true);
        RandomInlineCommentTransformer t2 = new RandomInlineCommentTransformer(1);
        t2.setFullRandomStrings(false);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertNotEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithDifferentSeeds_haveDifferentHashCode(){
        RandomInlineCommentTransformer t1 = new RandomInlineCommentTransformer(1);
        RandomInlineCommentTransformer t2 = new RandomInlineCommentTransformer(2);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertNotEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithTryingToCompile_haveDifferentHashCode(){
        RandomInlineCommentTransformer t1 = new RandomInlineCommentTransformer(5);
        t1.setTryingToCompile(true);
        RandomInlineCommentTransformer t2 = new RandomInlineCommentTransformer(5);
        t2.setTryingToCompile(false);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertNotEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithSetsAutoImports_haveDifferentHashCode(){
        RandomInlineCommentTransformer t1 = new RandomInlineCommentTransformer(5);
        t1.setSetsAutoImports(true);
        RandomInlineCommentTransformer t2 = new RandomInlineCommentTransformer(5);
        t2.setSetsAutoImports(false);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertNotEquals(r1,r2);
    }

    /*
    =============================================================
                   Helper Methods & Factories
    =============================================================
     */

    static CtElement addOneExample(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { int addOne(int a) { return a + 1 }");

        return testObject;
    }

    static CtElement sumExample(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { int sum(int a, int b) { return a + b;} }");

        return testObject;
    }

}
