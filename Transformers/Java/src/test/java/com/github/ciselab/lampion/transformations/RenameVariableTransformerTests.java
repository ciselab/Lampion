package com.github.ciselab.lampion.transformations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.ciselab.lampion.transformations.transformers.RenameVariableTransformer;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;

/**
 * This is the test suite for the RenameVariableTransformer class.
 * In order to resolve the issue of a new variable beginning with the same letter as the old one (int a -> int abba).
 * When this happens we can no longer test whether a change is made by running !string.contains("int a"),
 * since this might return false whilst the variable has indeed been renamed.
 * To solve this we use the extra space at the end of !string.contains("int a ").
 * This should resolve this issue and make sure that the tests are not flaky.
 */
public class RenameVariableTransformerTests {

    @RepeatedTest(5)
    void testApply_ApplyToMethodTwice_withTwoLocalVariables_firstThenSecondAltered(){
        CtElement ast = addBothExample();

        RenameVariableTransformer transformer = new RenameVariableTransformer();

        CtVariable varA = (CtVariable) ast.filterChildren(c -> c instanceof CtLocalVariable).list().get(0);
        CtVariable varB = (CtVariable) ast.filterChildren(c -> c instanceof CtLocalVariable).list().get(1);

        transformer.applyAtRandom(ast);

        boolean aAltered = ! varA.toString().contains("int a ");
        boolean bAltered = ! varB.toString().contains("int b ");

        assertTrue(aAltered ^ bAltered);

        transformer.applyAtRandom(ast);

        aAltered = ! varA.toString().contains("int a ");
        bAltered = ! varB.toString().contains("int b ");

        assertTrue(aAltered && bAltered);
    }

    @Test
    void testApply_ApplyToMethodTwice_withOneParameter_returnsEmptyTransformationResult(){
        CtElement ast = addOneLocalVariableExample();

        RenameVariableTransformer transformer = new RenameVariableTransformer();

        transformer.applyAtRandom(ast);
        var result = transformer.applyAtRandom(ast);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @RepeatedTest(3)
    void testApply_ApplyToMethodFiveTimes_withClassWithTwoMethods_returnsEmptyTransformationResult_AndAltersAll(){
        CtClass ast = Launcher.parseClass("""
                package lampion.test; class A {
                int sum() { int a = 1, b = 1;\n return a+b;} 
                int sam() { int a = 3;\n int b = 1;\n return a+b;}
                }""");

        CtMethod methodA = (CtMethod) ast.filterChildren(c -> c instanceof CtMethod).list().get(0);
        CtMethod methodB = (CtMethod) ast.filterChildren(c -> c instanceof CtMethod).list().get(1);

        RenameVariableTransformer transformer = new RenameVariableTransformer();

        TransformationResult result = new EmptyTransformationResult();
        for(int i = 0; i<5;i++)
            result = transformer.applyAtRandom(ast);

        // Check if both methods still have both parameters as initially set
        // as otherwise the test becomes flaky if the random variable name starts with a or b
        List<CtVariable> variables = methodA.filterChildren(c -> c instanceof CtLocalVariable).list();
        variables.addAll(methodB.filterChildren(c -> c instanceof CtLocalVariable).list());
        Predicate<CtMethod> isAltered = m -> variables.stream().allMatch(n -> !n.getSimpleName().equals("a") || !n.getSimpleName().equals("b"));
        assertEquals((int) variables.stream().distinct().count(), variables.size());

        boolean methodAAltered = isAltered.test(methodA);
        boolean methodBAltered = isAltered.test(methodB);

        assertTrue(methodAAltered && methodBAltered);
        assertEquals(new EmptyTransformationResult(),result);
    }

    @Test
    void testApply_ApplyToMethod_withDifferentSeeds_resultInDifferentResults() {
        CtClass ast = Launcher.parseClass("""
                package lampion.test; class A {
                int sum() { int a = 1;\n return a; }
                }""");

        RenameVariableTransformer transformer = new RenameVariableTransformer(2023);
        RenameVariableTransformer transformerSecond = new RenameVariableTransformer(2022);

        transformer.applyAtRandom(ast);
        CtMethod method = (CtMethod) ast.filterChildren(c -> c instanceof CtMethod).list().get(0);
        String variableOne = ((CtVariable) method.filterChildren(c -> c instanceof CtLocalVariable).list().get(0)).getSimpleName();

        transformerSecond.applyAtRandom(ast);
        CtMethod methodSecond = (CtMethod) ast.filterChildren(c -> c instanceof CtMethod).list().get(0);
        String variableTwo = ((CtVariable) methodSecond.filterChildren(c -> c instanceof CtLocalVariable).list().get(0)).getSimpleName();

        assertNotEquals(variableOne, variableTwo);
    }

    @Test
    void testApply_ApplyToMethod_methodsWithSharedVariableNames_notBothChanged() {
        CtClass ast = Launcher.parseClass("""
                package lampion.test; class A {
                int methodA(int a) { return a; }
                int sum(int b) { int a = 5;\n return a+b;}
                }""");
        RenameVariableTransformer transformer = new RenameVariableTransformer();

        transformer.applyAtRandom(ast);
        CtMethod methodA = (CtMethod) ast.filterChildren(c -> c instanceof CtMethod).list().get(0);
        CtMethod methodB = (CtMethod) ast.filterChildren(c -> c instanceof CtMethod).list().get(1);

        assertTrue(methodA.getParameters().get(0).toString().equals("int a"));
        assertFalse(methodB.filterChildren(c -> c instanceof CtVariable).list().contains("int a"));
    }

    @Test
    void testApply_ApplyToMethodFiveTimes_classVariablesAreNotAltered() {
        CtClass ast = Launcher.parseClass("""
                package lampion.test; class A {
                int c = 4;
                int sum() { int a = 1, b = 1;\n return a+b;}
                }""");
        RenameVariableTransformer transformer = new RenameVariableTransformer();
        for(int i = 0; i<5;i++){
            transformer.applyAtRandom(ast);
        }
        assertTrue(ast.toString().contains("int c = 4;"));
    }

    @Test
    void testApply_ApplyToMethod_oneParameterOneVariable_onlyVariablesGetsChanged() {
        CtElement ast = addOneLocalVariableExample();
        CtMethod astMethod = (CtMethod) ast.filterChildren(c -> c instanceof CtMethod).list().get(0);

        RenameVariableTransformer transformer = new RenameVariableTransformer();

        String variable = ((CtVariable) astMethod.filterChildren(c -> c instanceof CtLocalVariable).list().get(0)).getSimpleName();
        String param = astMethod.getParameters().get(0).toString();
        transformer.applyAtRandom(astMethod);
        String variableAfter = ((CtVariable) astMethod.filterChildren(c -> c instanceof CtLocalVariable).list().get(0)).getSimpleName();
        String paramAfter = astMethod.getParameters().get(0).toString();
        assertNotEquals(variable, variableAfter);
        assertEquals(param, paramAfter);
    }

    @Tag("Regression")
    @Test
    void testApply_ApplyToMethodTwentyTimes_amountOfParametersWillNotChange() {
        CtElement ast = differentDeclarationExample();
        CtMethod astMethod = (CtMethod) ast.filterChildren(c -> c instanceof CtMethod).list().get(0);

        RenameVariableTransformer transformer = new RenameVariableTransformer();

        List<CtVariable> before = astMethod.filterChildren(c -> c instanceof CtLocalVariable).list();
        assertEquals(2, before.size());

        for(int i = 0; i<20;i++) {
            transformer.applyAtRandom(astMethod);
        }
        List<CtVariable> after = astMethod.filterChildren(c -> c instanceof CtLocalVariable).list();
        assertEquals(2, after.size());
    }

    @Test
    void testApply_ApplyToMethod_withFullRandom_shouldGiveNonEmptyResult(){
        CtElement ast = addBothExample();

        RenameVariableTransformer transformer = new RenameVariableTransformer();

        transformer.setFullRandomStrings(true);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertNotEquals(new EmptyTransformationResult(),result);
    }

    @Test
    void testApply_ApplyToMethod_withoutParametersOrVariables_returnsEmptyTransformationResult(){
        CtClass ast = Launcher.parseClass("""
                package lampion.test; class A {
                int noParams() { return 1;}
                }""");

        RenameVariableTransformer transformer = new RenameVariableTransformer();

        var result = transformer.applyAtRandom(ast);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Test
    void testApply_ApplyToMethod_withParametersOnly_returnsEmptyTransformationResult(){
        CtClass ast = Launcher.parseClass("""
                package lampion.test; class A {
                int sum(int a, int b) { return a+b; }
                }""");

        RenameVariableTransformer transformer = new RenameVariableTransformer();

        var result = transformer.applyAtRandom(ast);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Test
    void testApply_ApplyToClassWithoutMethods_returnsEmptyTransformationResult(){
        CtClass ast = Launcher.parseClass("class A { }");

        RenameVariableTransformer transformer = new RenameVariableTransformer();

        var result = transformer.applyAtRandom(ast);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Test
    void testApply_ApplyToClassWithoutMethods_withPackage_returnsEmptyTransformationResult(){
        CtClass ast = Launcher.parseClass("""
                package lampion.test.package;
                class A {
                }""");

        RenameVariableTransformer transformer = new RenameVariableTransformer();

        var result = transformer.applyAtRandom(ast);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Test
    void testExclusive_isExclusiveWithNothing(){
        RenameVariableTransformer transformer = new RenameVariableTransformer();

        assertTrue(transformer.isExclusiveWith().isEmpty());
    }

    @Test
    void testApply_ApplyToMethod_constraintsAreNotSatisfied_noVariablesToChange_ReturnsEmptyResult(){
        CtClass emptyClass = Launcher.parseClass("""
                package lampion.test.examples;
                class A {
                    int addOne(int a) {
                        return a + 1 }""");

        RenameVariableTransformer transformer = new RenameVariableTransformer();
        TransformationResult result = transformer.applyAtRandom(emptyClass);

        assertEquals(new EmptyTransformationResult(), result);
    }

    @Test
    void testApply_ApplyToMethod_CheckTransformationResult_nameIsRandomVariableName(){
        CtElement ast = addOneLocalVariableExample();

        RenameVariableTransformer transformer = new RenameVariableTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertEquals("RenameVariableTransformer",result.getTransformationName());
    }

    @Test
    void testApply_ApplyToClass_CheckTransformationResult_categoriesNotEmpty(){
        CtElement ast = addOneLocalVariableExample();

        RenameVariableTransformer transformer = new RenameVariableTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getCategories().isEmpty());
    }

    @Test
    void testApply_ApplyToClass_withoutDebugSettingOn_TransformationResultShouldHaveNoOptionalInfo(){
        CtElement ast = addOneLocalVariableExample();

        RenameVariableTransformer transformer = new RenameVariableTransformer();

        transformer.setDebug(false);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getInitialScopeOfTransformation().isPresent());
        assertFalse(result.getBeforeAfterComparison().isPresent());
    }

    @Test
    void testApply_ApplyToClass_WithDebugSettingOn_TransformationResultShouldHaveMoreInfo(){
        CtElement ast = addOneLocalVariableExample();

        RenameVariableTransformer transformer = new RenameVariableTransformer();

        transformer.setDebug(true);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertTrue(result.getInitialScopeOfTransformation().isPresent());
        assertTrue(result.getBeforeAfterComparison().isPresent());
    }

    @Test
    void testGetStringRandomness_onFullRandom_ShouldGiveFullRandom(){
        RenameVariableTransformer transformer = new RenameVariableTransformer();
        transformer.setFullRandomStrings(true);

        assertTrue(transformer.isFullRandomStrings());
    }

    @Test
    void testGetStringRandomness_onPseudoRandom_ShouldGivePseudoRandom(){
        RenameVariableTransformer transformer = new RenameVariableTransformer();
        transformer.setFullRandomStrings(false);

        assertFalse(transformer.isFullRandomStrings());
    }

    static CtElement addOneLocalVariableExample(){
        CtClass testObject = Launcher.parseClass("""
                                                        package lampion.test.examples;
                                                        class A {
                                                            int addOne(int a) {
                                                                int b = 1;
                                                                return a + b }""");
        return testObject;
    }

    static CtElement addBothExample(){
        CtClass testObject = Launcher.parseClass("""
                                                        package lampion.test.examples;
                                                        class A {
                                                            int sum() {
                                                                int a = 1;
                                                                int b = 1;
                                                                return a + b }""");
        return testObject;
    }

    static CtElement differentDeclarationExample(){
        CtClass testObject = Launcher.parseClass("""
                                                        package lampion.test.examples;
                                                        class A {
                                                        int sum() {
                                                        int a = 1, b = 1;
                                                        return a + b }""");
        return testObject;
    }
}
