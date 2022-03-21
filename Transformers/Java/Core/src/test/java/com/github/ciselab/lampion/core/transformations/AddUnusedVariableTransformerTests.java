package com.github.ciselab.lampion.core.transformations;

import com.github.ciselab.lampion.core.transformations.transformers.AddNeutralElementTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.AddUnusedVariableTransformer;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

public class AddUnusedVariableTransformerTests {

    @Test
    void applyToMethod_withPseudoNames_shouldBeChanged(){
        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();
        transformer.setFullRandomStrings(false);

        CtElement ast = sumExample();

        transformer.applyAtRandom(ast);

        assertTrue(ast.toString().contains("="));
    }


    @Test
    void applyToMethod_withPseudoNames_10Times_shouldBeApplied(){
        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();
        transformer.setFullRandomStrings(false);

        CtElement ast = sumExample();

        TransformationResult result = null;

        for(int i = 0; i<10;i++) {
            result = transformer.applyAtRandom(ast);
        }

        assertTrue(ast.toString().contains("="));
        assertNotEquals(new EmptyTransformationResult(),result);
    }

    @Test
    void applyToMethod_withFullRandomNames_shouldBeApplied(){
        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();
        transformer.setFullRandomStrings(true);

        CtElement ast = sumExample();

        transformer.applyAtRandom(ast);

        assertTrue(ast.toString().contains("="));
    }

    @Test
    void applyFromSeed_shouldBeTheSameForSameTwoSeeds(){
        AddUnusedVariableTransformer transformerA = new AddUnusedVariableTransformer(200);
        AddUnusedVariableTransformer transformerB = new AddUnusedVariableTransformer(200);


        CtElement astA = sumExample();
        CtElement astB = sumExample();

        transformerA.applyAtRandom(astA);
        transformerB.applyAtRandom(astB);

        assertEquals(astA.toString(),astB.toString());
    }

    @Test
    void applyFromSeed_twoDifferentSeeds_shouldBeTheDifferentForTwoSeeds(){
        AddUnusedVariableTransformer transformerA = new AddUnusedVariableTransformer(1);
        AddUnusedVariableTransformer transformerB = new AddUnusedVariableTransformer(2);

        CtElement astA = sumExample();
        CtElement astB = sumExample();

        transformerA.applyAtRandom(astA);
        transformerB.applyAtRandom(astB);

        assertNotEquals(astA.toString(),astB.toString());
    }

    @Test
    void applyToMethod_withKnownNameOverlapping_shouldRedrawANewName_andBeSuccessfullyApplied(){
        // The variable was chosen by a seed so there would be a forced overlap in names
        // The transformer should re-draw a name after detecting the collision
        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer(1);

        CtElement ast =  Launcher.parseClass(
                "package lampion.test.examples; class A { " +
                "int some(int agedWaleNurse){return agedWaleNurse;}" +
                "}");

        var result = transformer.applyAtRandom(ast);

        assertTrue(ast.toString().contains("="));
        assertNotEquals(new EmptyTransformationResult(),result);
    }

    @Test
    void applyToMethod_withDifferentSeeds_ShouldBeDifferentOutputs(){
        // This is more a test of the "setSeed" Method from BaseTransformer
        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer(100);

        CtElement ast1 =  Launcher.parseClass(
                "package lampion.test.examples; \n" +
                        "class A { \n" +
                        "int some(int a){return a;} \n" +
                        "}");

        CtElement ast2 =  Launcher.parseClass(
                "package lampion.test.examples; \n" +
                        "class A { \n" +
                        "int some(int a){return a;} \n" +
                        "}");

        var result1 = transformer.applyAtRandom(ast1);

        transformer.setSeed(200);
        var result2 = transformer.applyAtRandom(ast2);

        assertNotEquals(new EmptyTransformationResult(),result1);
        assertNotEquals(new EmptyTransformationResult(),result2);
        assertNotEquals(ast1.toString(),ast2.toString());
    }

    @Tag("Integration")
    @Test
    void applyNeutralElementTransformer_AfterUnusedVariableTransformer_shouldWork(){
        // Be careful: If the variable Transformer picks a boolean, the element transformer fails
        // So pick a seed that produces one of the supported types
        AddUnusedVariableTransformer variableTransformer = new AddUnusedVariableTransformer(125);
        AddNeutralElementTransformer elementTransformer = new AddNeutralElementTransformer();

        CtElement ast =  Launcher.parseClass("package lampion.test.examples; class A { " +
                "void some(){}" +
                "}");

        variableTransformer.applyAtRandom(ast);
        var result = elementTransformer.applyAtRandom(ast);

        assertTrue(ast.toString().contains("="));
        assertTrue(ast.toString().contains("+"));
        assertNotEquals(new EmptyTransformationResult(),result);
    }

    @Test
    void applyToEmptyMethod_shouldWork(){
        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();

        CtElement ast =  Launcher.parseClass("package lampion.test.examples; class A { " +
                "void some(){}" +
                "}");


        var result = transformer.applyAtRandom(ast);

        assertTrue(ast.toString().contains("="));
        assertNotEquals(new EmptyTransformationResult(),result);
    }

    @RepeatedTest(5)
    void applyToClassWithTwoMethods_onlyOneIsAltered(){
        CtClass ast = Launcher.parseClass("package lampion.test.examples; class A { " +
                "int sum(int a, int b) { return a + b;} " +
                "void some(){System.out.println(\"hey!\");}" +
                "}");

        CtMethod methodA = (CtMethod) ast.filterChildren(c -> c instanceof CtMethod).list().get(0);
        CtMethod methodB = (CtMethod) ast.filterChildren(c -> c instanceof CtMethod).list().get(1);

        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();

        transformer.applyAtRandom(ast);

        Predicate<CtMethod> isAltered = m -> m.toString().contains("=");

        boolean methodAAltered = isAltered.test(methodA);
        boolean methodBAltered = isAltered.test(methodB);

        // The operator "^" is the XOR operator
        assertTrue(methodAAltered ^ methodBAltered);
    }

    @Test
    void testExclusive_isExclusiveWithNothing(){
        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();

        assertTrue(transformer.isExclusiveWith().isEmpty());
    }

    @Test
    void constraintsAreNotSatisfied_ReturnsEmptyResult(){
        CtClass emptyClass = Launcher.parseClass("class A { }");

        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();
        TransformationResult result = transformer.applyAtRandom(emptyClass);

        assertEquals(new EmptyTransformationResult(), result);
    }

    @Test
    void applyToMethod_CheckTransformationResult_nameIsIfTrue(){
        CtElement ast = sumExample();

        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertEquals("AddUnusedVariable",result.getTransformationName());
    }

    @Test
    void applyToMethod_CheckTransformationResult_categoriesNotEmpty(){
        CtElement ast = sumExample();

        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getCategories().isEmpty());
    }

    @Test
    void applyWithoutDebugSettingOn_TransformationResultShouldHaveNoOptionalInfo(){
        CtElement ast = sumExample();

        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();

        transformer.setDebug(false);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getInitialScopeOfTransformation().isPresent());
        assertFalse(result.getBeforeAfterComparison().isPresent());
    }

    @Test
    void applyWithDebugSettingOn_TransformationResultShouldHaveMoreInfo(){
        CtElement ast = sumExample();

        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();

        transformer.setDebug(true);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertTrue(result.getInitialScopeOfTransformation().isPresent());
        assertTrue(result.getBeforeAfterComparison().isPresent());
    }

    @Test
    void checkAfterCreation_DefaultIsPseudoRandom(){
        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();

        assertFalse(transformer.isFullRandomStrings());
    }

    @Test
    void applyTo_Apply100Times_withCompilation_shouldNotFail(){
        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer(25);
        transformer.setFullRandomStrings(true);
        transformer.setTryingToCompile(true);

        CtElement ast =  Launcher.parseClass("package lampion.test.examples; class A { " +
                "void some(){}" +
                "}");

        for (int i = 0; i<100; i++){
            TransformationResult result = transformer.applyAtRandom(ast);
            assertNotEquals(new EmptyTransformationResult(),result);
        }
    }
    @Test
    void applyTo_Apply100Times_withoutCompilation_shouldNotFail(){
        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer(25);
        transformer.setFullRandomStrings(true);
        transformer.setTryingToCompile(false);

        CtElement ast =  Launcher.parseClass("package lampion.test.examples; class A { " +
                "void some(){}" +
                "}");

        for (int i = 0; i<100; i++){
            TransformationResult result = transformer.applyAtRandom(ast);
            assertNotEquals(new EmptyTransformationResult(),result);
        }
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
