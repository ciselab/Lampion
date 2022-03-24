package com.github.ciselab.lampion.core.transformations;

import com.github.ciselab.lampion.core.transformations.transformers.AddNeutralElementTransformer;

import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;

import static org.junit.jupiter.api.Assertions.*;

public class AddNeutralElementTransformerTests {

    @Test
    void applyToMethodWithIntegerLiteral_shouldHaveAZero(){
        CtElement testClass = returnOneExample();

        AddNeutralElementTransformer transformer = new AddNeutralElementTransformer();
        TransformationResult result = transformer.applyAtRandom(testClass);

        assertTrue(testClass.toString().contains("1 + 0"));
    }

    @Test
    void applyToMethodWithIntegerVariable_shouldHaveAZero(){
        // The variable in the example is called "i"
        CtElement testClass = returnIntegerExample();

        AddNeutralElementTransformer transformer = new AddNeutralElementTransformer();
        TransformationResult result = transformer.applyAtRandom(testClass);

        assertTrue(testClass.toString().contains("i + 0"));
    }


    @Test
    void applyToMethodWithIntegerVariable_TransformerWithSeed_shouldHaveAZero(){
        // The variable in the example is called "i"
        CtElement testClass = returnIntegerExample();

        AddNeutralElementTransformer transformer = new AddNeutralElementTransformer(125);
        TransformationResult result = transformer.applyAtRandom(testClass);

        assertTrue(testClass.toString().contains("i + 0"));
    }

    @Test
    void applyToMethodWithIntegerVariable_applyTwice_shouldNotGiveEmptyResult(){
        // The variable in the example is called "i"
        CtElement testClass = returnIntegerExample();

        AddNeutralElementTransformer transformer = new AddNeutralElementTransformer();

        transformer.applyAtRandom(testClass);
        TransformationResult result = transformer.applyAtRandom(testClass);

        assertNotEquals(new EmptyTransformationResult(),result);
    }


    @Test
    void applyToMethodWithFloatLiteral_shouldHaveAZero(){
        CtElement testClass = returnOneFloatExample();

        AddNeutralElementTransformer transformer = new AddNeutralElementTransformer();
        TransformationResult result = transformer.applyAtRandom(testClass);

        assertTrue(testClass.toString().contains("1.0F + 0.0F"));
    }

    @Test
    void applyToMethodWithDoubleLiteral_shouldHaveAZero(){
        // One can specify 1.0d but its not gonna change anything, .x values are seen as double by default
        CtElement testClass = returnOneDoubleExample();

        AddNeutralElementTransformer transformer = new AddNeutralElementTransformer();
        TransformationResult result = transformer.applyAtRandom(testClass);

        assertTrue(testClass.toString().contains("1.0 + 0.0"));
    }

    @Test
    void applyToMethodWithLongLiteral_shouldHaveAZero(){
        CtElement testClass = returnOneLongExample();

        AddNeutralElementTransformer transformer = new AddNeutralElementTransformer();
        TransformationResult result = transformer.applyAtRandom(testClass);

        assertTrue(testClass.toString().contains("1L + 0L"));
    }

    @Test
    void applyToMethodWithStringLiteral_applyTenTimes_shouldNotGiveEmptyResult(){
        // The variable in the example is called "i"
        CtElement testClass = stringLiteralExample();

        AddNeutralElementTransformer transformer = new AddNeutralElementTransformer();

        for(int i=0; i<10; i++) {
            transformer.applyAtRandom(testClass);
        }
        TransformationResult result = transformer.applyAtRandom(testClass);

        assertNotEquals(new EmptyTransformationResult(),result);
    }

    @Test
    void applyToMethodWithStringLiteral_applyTenTimes_withCompilationOff_shouldNotGiveEmptyResult(){
        // The variable in the example is called "i"
        CtElement testClass = stringLiteralExample();

        AddNeutralElementTransformer transformer = new AddNeutralElementTransformer();
        transformer.setTryingToCompile(false);

        for(int i=0; i<10; i++) {
            transformer.applyAtRandom(testClass);
        }
        TransformationResult result = transformer.applyAtRandom(testClass);

        assertNotEquals(new EmptyTransformationResult(),result);
    }

    @Test
    void applyToMethodWithStringLiteral_ShouldHaveEmptyString(){
        CtElement testClass = stringLiteralExample();

        AddNeutralElementTransformer transformer = new AddNeutralElementTransformer();
        TransformationResult result = transformer.applyAtRandom(testClass);

        String content = testClass.toString();

        assertTrue(testClass.toString().contains("\"\""));
    }

    @Test
    void applyToMethodWithStringVariable_ShouldHaveEmptyString(){
        CtElement testClass = stringVariableExample();

        AddNeutralElementTransformer transformer = new AddNeutralElementTransformer();
        TransformationResult result = transformer.applyAtRandom(testClass);

        String content = testClass.toString();

        assertTrue(testClass.toString().contains("\"\""));
    }

    @Test
    void applyToMethodWithUnsupportedVariableType_shouldReturnEmptyResult(){
        CtElement testClass = charVariableExample();

        AddNeutralElementTransformer transformer = new AddNeutralElementTransformer();
        TransformationResult result = transformer.applyAtRandom(testClass);

        assertEquals(new EmptyTransformationResult(), result);
    }

    @Test
    void applyToMethodWithUnsupportedLiteralType_shouldReturnEmptyResult(){
        CtElement testClass = charLiteralExample();

        AddNeutralElementTransformer transformer = new AddNeutralElementTransformer();
        TransformationResult result = transformer.applyAtRandom(testClass);

        assertEquals(new EmptyTransformationResult(), result);
    }


    @Test
    void testExclusive_isExclusiveWithNothing(){
        AddNeutralElementTransformer transformer = new AddNeutralElementTransformer();

        assertTrue(transformer.isExclusiveWith().isEmpty());
    }

    @Test
    void constraintsAreNotSatisfied_ReturnsEmptyResult(){
        CtClass emptyClass = Launcher.parseClass("class A { }");

        AddNeutralElementTransformer transformer = new AddNeutralElementTransformer();
        TransformationResult result = transformer.applyAtRandom(emptyClass);

        assertEquals(new EmptyTransformationResult(), result);
    }

    @Test
    void applyToMethod_CheckTransformationResult_ElementInResultIsNotAltered(){
        CtElement ast = sumExample();

        AddNeutralElementTransformer transformer = new AddNeutralElementTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        String toCheck = result.getTransformedElement().toString();

        assertFalse(toCheck.contains("if(true)"));
        assertFalse(toCheck.contains("else"));
        assertFalse(toCheck.contains("return null;"));
    }

    @Test
    void applyToMethod_CheckTransformationResult_nameIsIfTrue(){
        CtElement ast = sumExample();

        AddNeutralElementTransformer transformer = new AddNeutralElementTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertEquals("AddNeutralElement",result.getTransformationName());
    }

    @Test
    void applyToMethod_CheckTransformationResult_categoriesNotEmpty(){
        CtElement ast = sumExample();

        AddNeutralElementTransformer transformer = new AddNeutralElementTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getCategories().isEmpty());
    }

    @Test
    void applyWithoutDebugSettingOn_TransformationResultShouldHaveNoOptionalInfo(){
        CtElement ast = sumExample();

        AddNeutralElementTransformer transformer = new AddNeutralElementTransformer();

        transformer.setDebug(false);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getInitialScopeOfTransformation().isPresent());
        assertFalse(result.getBeforeAfterComparison().isPresent());
    }

    @Test
    void applyWithDebugSettingOn_TransformationResultShouldHaveMoreInfo(){
        CtElement ast = sumExample();

        AddNeutralElementTransformer transformer = new AddNeutralElementTransformer();

        transformer.setDebug(true);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertTrue(result.getInitialScopeOfTransformation().isPresent());
        assertTrue(result.getBeforeAfterComparison().isPresent());
    }

    /*
    ========================================================
                   Equality & HashCode Tests
    ========================================================
     */

    @Test
    void testEquals_Reflexivity(){
        AddNeutralElementTransformer transformer = new AddNeutralElementTransformer(2022);

        assertEquals(transformer,transformer);
    }

    @Test
    void testEquals_TwoTransformers_differentSeeds_areNotEquals(){
        AddNeutralElementTransformer t1 = new AddNeutralElementTransformer(1);
        AddNeutralElementTransformer t2 = new AddNeutralElementTransformer(2);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoTransformers_differentSeeds_seedsAreChangedAfterCreation_areNotEquals(){
        AddNeutralElementTransformer t1 = new AddNeutralElementTransformer(1);
        AddNeutralElementTransformer t2 = new AddNeutralElementTransformer(1);
        t2.setSeed(2);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoTransformers_differentTryingToCompile_areNotEquals(){
        AddNeutralElementTransformer t1 = new AddNeutralElementTransformer(1);
        t1.setTryingToCompile(false);
        AddNeutralElementTransformer t2 = new AddNeutralElementTransformer(1);
        t2.setTryingToCompile(true);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoTransformers_differentAutoImports_areNotEquals(){
        AddNeutralElementTransformer t1 = new AddNeutralElementTransformer(1);
        t1.setSetsAutoImports(false);
        AddNeutralElementTransformer t2 = new AddNeutralElementTransformer(1);
        t2.setSetsAutoImports(true);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoFreshTransformers_areEqual(){
        AddNeutralElementTransformer t1 = new AddNeutralElementTransformer(5);
        AddNeutralElementTransformer t2 = new AddNeutralElementTransformer(5);

        assertEquals(t1,t2);
    }

    @Test
    void testHashCode_FreshTransformer_isNotNull(){
        AddNeutralElementTransformer transformer = new AddNeutralElementTransformer(10);

        int result = transformer.hashCode();

        assertNotNull(result);
        assertNotEquals(0,result);
    }

    @Test
    void testHashCode_TransformerWithSameSeeds_haveSameHashCode(){
        AddNeutralElementTransformer t1 = new AddNeutralElementTransformer(1);
        AddNeutralElementTransformer t2 = new AddNeutralElementTransformer(1);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithDifferentSeeds_haveDifferentHashCode(){
        AddNeutralElementTransformer t1 = new AddNeutralElementTransformer(1);
        AddNeutralElementTransformer t2 = new AddNeutralElementTransformer(2);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertNotEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithTryingToCompile_haveDifferentHashCode(){
        AddNeutralElementTransformer t1 = new AddNeutralElementTransformer(5);
        t1.setTryingToCompile(true);
        AddNeutralElementTransformer t2 = new AddNeutralElementTransformer(5);
        t2.setTryingToCompile(false);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertNotEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithSetsAutoImports_haveDifferentHashCode(){
        AddNeutralElementTransformer t1 = new AddNeutralElementTransformer(5);
        t1.setSetsAutoImports(true);
        AddNeutralElementTransformer t2 = new AddNeutralElementTransformer(5);
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

    static CtElement stringLiteralExample(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { void m() { System.out.println(\"yeah\");} }");

        return testObject;
    }

    static CtElement stringVariableExample(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { void m(String u) { System.out.println(u);} }");

        return testObject;
    }


    static CtElement charVariableExample() {
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { char m(char c) {return c;} }");

        return testObject;
    }


    static CtElement charLiteralExample() {
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { char m() {return 'm';} }");

        return testObject;
    }

    static CtElement returnOneExample(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { int m() { return 1;} }");

        return testObject;
    }

    static CtElement returnOneFloatExample(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { float m() { return 1.0F;} }");

        return testObject;
    }
    static CtElement returnOneDoubleExample(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { double m() { return 1.0d;} }");

        return testObject;
    }
    static CtElement returnOneLongExample(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { long m() { return 1L;} }");

        return testObject;
    }
    static CtElement returnIntegerExample(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { int m(int i) { return i;} }");

        return testObject;
    }

    static CtElement sumExample(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { int sum(int a, int b) { return a + b;} }");

        return testObject;
    }

}
