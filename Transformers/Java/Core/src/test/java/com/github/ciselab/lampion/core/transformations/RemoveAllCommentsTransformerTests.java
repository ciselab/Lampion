package com.github.ciselab.lampion.core.transformations;

import com.github.ciselab.lampion.core.transformations.transformers.IfTrueTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.RemoveAllCommentsTransformer;
import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;

import static org.junit.jupiter.api.Assertions.*;

public class RemoveAllCommentsTransformerTests {

    @Test
    void applyToMethodWithComment_ShouldHaveNoCommentsAfterwards(){
        CtElement ast = commentExample();

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();

        transformer.applyAtRandom(ast);

        assertFalse(ast.toString().contains("//"));
    }

    @Test
    void applyToMethodWithTwoComments_ShouldHaveNoCommentsAfterwards(){
        CtClass ast = Launcher.parseClass("package lampion.test.examples; // some comment \n class A { int sum(int a, int b) { return a + b; \n // some more comments \n} }");

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();

        transformer.applyAtRandom(ast);

        assertFalse(ast.toString().contains("//"));
    }

    @Test
    void createSeeded_constructorShouldWork(){
        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer(25);
    }

    @Test
    void applyToMethodWithTwoComments_withSeededTransformer_ShouldHaveNoCommentsAfterwards(){
        CtClass ast = Launcher.parseClass("package lampion.test.examples; // some comment \n class A { int sum(int a, int b) { return a + b; \n // some more comments \n} }");

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer(25);

        transformer.applyAtRandom(ast);

        assertFalse(ast.toString().contains("//"));
    }


    @Test
    void applyToMethodWithInnerComment_ShouldHaveNoCommentsAfterwards(){
        CtClass ast = Launcher.parseClass("package lampion.test.examples; class A { int sum(int a, int b) { return a + b; \n // Inner Comment \n} }");

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();

        transformer.applyAtRandom(ast);

        assertFalse(ast.toString().contains("//"));
    }

    @Test
    void applyToMethodWithTwoInnerComments_ShouldHaveNoCommentsAfterwards(){
        CtClass ast = Launcher.parseClass("package lampion.test.examples; class A { int sum(int a, int b) { \n // Inner Comment A \n return a + b; \n // Inner Comment B \n} }");

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();

        transformer.applyAtRandom(ast);

        assertFalse(ast.toString().contains("//"));
    }

    @Test
    void applyToMethodWithThreeComments_ShouldHaveNoCommentsAfterwards(){
        CtClass ast = Launcher.parseClass("package lampion.test.examples; // comment A \n // comment B \n class A { int sum(int a, int b) { return a + b; \n // comment C \n} }");

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();

        transformer.applyAtRandom(ast);

        assertFalse(ast.toString().contains("//"));
    }


    @Test
    void constraintsAreNotSatisfied_ReturnsEmptyResult(){
        CtClass emptyClass = Launcher.parseClass("class A { }");

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();
        TransformationResult result = transformer.applyAtRandom(emptyClass);

        assertEquals(new EmptyTransformationResult(), result);
    }

    @Test
    void applyToMethod_CheckTransformationResult_nameIsRemoveAllComments(){
        CtElement ast = commentExample();

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertEquals("RemoveAllComments",result.getTransformationName());
    }

    @Test
    void applyToMethod_CheckTransformationResult_categoriesNotEmpty(){
        CtElement ast = commentExample();

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getCategories().isEmpty());
    }

    @Test
    void applyWithoutDebugSettingOn_TransformationResultShouldHaveNoOptionalInfo(){
        CtElement ast = commentExample();

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();

        transformer.setDebug(false);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getInitialScopeOfTransformation().isPresent());
        assertFalse(result.getBeforeAfterComparison().isPresent());
    }

    @Test
    void applyWithDebugSettingOn_TransformationResultShouldHaveMoreInfo(){
        CtElement ast = commentExample();

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();

        transformer.setDebug(true);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertTrue(result.getInitialScopeOfTransformation().isPresent());
        assertTrue(result.getBeforeAfterComparison().isPresent());
    }

    @Test
    void checkExclusiveWith_shouldBeEmpty(){
        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();

        assertTrue(transformer.isExclusiveWith().isEmpty());
    }

    static CtElement commentExample(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; // some comment \n class A { int sum(int a, int b) { return a + b;} }");

        return testObject;
    }

}
