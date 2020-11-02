package com.github.ciselab.lampion.transformations;

import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class TransformationResultTests {

    @Test
    void testEmptyTransformationResult_isEqualToEachOther(){
        TransformationResult a = new EmptyTransformationResult();
        TransformationResult b = new EmptyTransformationResult();

        assertEquals(a,b);
        assertEquals(b,a);
    }

    @Test
    void testSimpleTransformationResult_equalToItself(){
        CtElement element = sumExample();

        TransformationResult result = new SimpleTransformationResult("Test",element,new HashSet<>());

        assertTrue(result.equals(result));
    }


    @Test
    void testSimpleTransformationResult_NotEqualToSomethingCompletelyDifferent(){
        CtElement element = sumExample();

        TransformationResult result = new SimpleTransformationResult("Test",element,new HashSet<>());

        assertFalse(result.equals("1"));
    }

    @Test
    void testSimpleTransformationResult_NotEqualEmptyTransformationResult(){
        CtElement element = sumExample();

        TransformationResult result = new SimpleTransformationResult("Test",element,new HashSet<>());

        assertFalse(result.equals(new EmptyTransformationResult()));
    }

    @Test
    void testSimpleTransformationResult_equalToSameResult(){
        CtElement element = sumExample();

        TransformationResult result = new SimpleTransformationResult("Test",element,new HashSet<>());

        TransformationResult other = new SimpleTransformationResult("Test",element,new HashSet<>());

        assertTrue(result.equals(other));
    }

    @Test
    void testSimpleTransformationResult_differentCategories_notEqual(){
        CtElement element = sumExample();

        HashSet<TransformationCategory> categories = new HashSet<>();
        categories.add(TransformationCategory.BYTECODE);

        TransformationResult result = new SimpleTransformationResult("Test",element,categories);

        TransformationResult other = new SimpleTransformationResult("Test",element,new HashSet<>());

        assertFalse(result.equals(other));
    }

    @Test
    void testSimpleTransformationResult_oneHasOptions_isIgnored(){
        CtElement element = sumExample();

        TransformationResult result = new SimpleTransformationResult("Test",element,new HashSet<>());

        TransformationResult other = new SimpleTransformationResult("Test",element,new HashSet<>(), "hi");

        assertTrue(result.equals(other));
    }

    @Test
    void testSimpleTransformationResult_oneHasTwoOptions_isIgnored(){
        CtElement element = sumExample();

        TransformationResult result = new SimpleTransformationResult("Test",element,new HashSet<>(),element);

        TransformationResult other = new SimpleTransformationResult("Test",element,new HashSet<>(), "hi",element);

        assertTrue(result.equals(other));
    }

    @Test
    void testEquals_TwoDifferentTransformationResults_AreNotEqual(){
        CtElement elementA = sumExample();
        CtElement elementB = classWithoutReturnMethod();

        TransformationResult result = new SimpleTransformationResult("Test",elementA,new HashSet<>());

        TransformationResult other = new SimpleTransformationResult("Test",elementB,new HashSet<>());

        assertFalse(result.equals(other));
    }

    @Test
    void testEquals_TwoDifferentNames_AreNotEqual(){
        CtElement elementA = sumExample();

        TransformationResult result = new SimpleTransformationResult("Test",elementA,new HashSet<>());

        TransformationResult other = new SimpleTransformationResult("Different",elementA,new HashSet<>());

        assertFalse(result.equals(other));
    }

    @Test
    void testHashCode_SameElements_HaveSameHashCodes(){
        CtElement elementA = sumExample();

        TransformationResult result = new SimpleTransformationResult("Test",elementA,new HashSet<>());

        TransformationResult other = new SimpleTransformationResult("Test",elementA,new HashSet<>());

        assertEquals(result.hashCode(),other.hashCode());
    }

    @Test
    void testHashCode_TwoDifferentTransformationResults_haveDifferentHashCode(){
        CtElement elementA = sumExample();
        CtElement elementB = classWithoutReturnMethod();

        TransformationResult result = new SimpleTransformationResult("Test",elementA,new HashSet<>());

        TransformationResult other = new SimpleTransformationResult("Test",elementB,new HashSet<>());

        assertNotEquals(result.hashCode(),other.hashCode());
    }

    @Test
    void testEmptyResult_TestGetters(){
        TransformationResult emptyResult = new EmptyTransformationResult();

        assertEquals("EmptyTransformationResult",emptyResult.getTransformationName());
        assertNull(emptyResult.getTransformedElement());
        assertTrue(emptyResult.getCategories().isEmpty());
        assertTrue(emptyResult.getBeforeAfterComparison().isEmpty());
        assertTrue(emptyResult.getInitialScopeOfTransformation().isEmpty());

        assertNotEquals(0,emptyResult.hashCode());
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
