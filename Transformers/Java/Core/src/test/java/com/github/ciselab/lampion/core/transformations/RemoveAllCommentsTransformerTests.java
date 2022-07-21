package com.github.ciselab.lampion.core.transformations;

import com.github.ciselab.lampion.core.transformations.transformers.RandomInlineCommentTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.RemoveAllCommentsTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.IfTrueTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.RemoveAllCommentsTransformer;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
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

    /*
    ==============================================
                    Known CornerCases
    ==============================================

    These are issues we found with rare and currently not supported Java Elements.
    For example, inner classes crash virtually everything.
    See https://github.com/ciselab/Lampion/issues/109 and https://github.com/ciselab/Lampion/issues/91
     */

    @Tag("Regression")
    @Test
    public void applyToAbstractClassWithAbstractMethod_shouldNotBeApplied(){
        String raw = """
                public abstract class example_abstract_class {
                    public abstract void abstract_method() {
                        int a = 5;
                        // Hello 
                        System.out.println("Hello Abstract World");
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToAbstractClassWithConcreteMethod_shouldNotBeApplied(){
        String raw = """
                public abstract class example_abstract_class {
                // Hello Too
                    public void normal_method(){
                        int b = 1;
                        // Hello 
                        System.out.println("Hello World");
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @RepeatedTest(3)
    public void applyToAbstractClassWithConcreteAndAbstractMethod_shouldNotBeApplied(){
        String raw = """
                public abstract class example_abstract_class {
                    public void normal_method(){
                        int a = 5;
                        // A
                        System.out.println("Hello World");
                    }
                    
                    public abstract void abstract_method() {
                        int b = 5;
                        // B
                        System.out.println("Hello Abstract World");
                    }
                    // C
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToEmptyInterface_shouldNotBeApplied(){
        String raw = """
                public interface example_interface {
                
                }
                """;
        var launcher = new Launcher();
        CtElement testObject = launcher.getFactory().createCodeSnippetStatement(raw);

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToInterface_shouldNotBeApplied(){
        String raw = """
                public interface example_interface {
                    // A
                    public int doSomething();
                }
                """;
        var launcher = new Launcher();
        CtElement testObject = launcher.getFactory().createCodeSnippetStatement(raw);

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToInterfaceWithDefaultMethod_shouldNotBeApplied(){
        String raw = """
                public interface example_interface {
                    // A
                    public default int doSomething(){
                        int b = 5;
                        return 5;
                        // B
                    }
                    
                }
                """;

        var launcher = new Launcher();
        CtElement testObject = launcher.getFactory().createCodeSnippetStatement(raw);

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToEnum_shouldNotBeApplied(){
        // This does not match the transformers purpose - but it should also not throw an error!
        String raw = """
                enum Directions {
                    NORTH,
                    EAST,
                    SOUTH,
                    // A
                    WEST
                }
                """;
        var launcher = new Launcher();
        CtElement testObject = launcher.getFactory().createCodeSnippetStatement(raw);

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToInnerClassWithoutMethods_shouldNotBeApplied(){
        // This does not match the transformers purpose - but it should also not throw an error!
        String raw = """
                public class Outer {  
                // A
                        private class Inner {}
                        // C
                }
                """;
        var launcher = new Launcher();
        CtElement testObject = launcher.getFactory().createCodeSnippetStatement(raw);

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @RepeatedTest(5)
    public void applyToInnerClassWithMethods_shouldNotBeApplied(){
        // This does not match the transformers purpose - but it should also not throw an error!
        String raw = """
                public class Outer {  
                // A
                        private class Inner {
                            public int innerDoSomething(int i){
                                int something = 15;
                                // B
                                return i + something;
                            }
                        }
                }
                """;
        var launcher = new Launcher();
        CtElement testObject = launcher.getFactory().createCodeSnippetStatement(raw);

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @RepeatedTest(5)
    public void applyToInnerClass_InnerHasNotAndOuterHasMethod_shouldNotBeApplied(){
        // This does not match the transformers purpose - but it should also not throw an error!
        String raw = """
                public class Outer {
                // B
                        public int outerDoSomething(int o){
                        // A
                            int something = 5;
                            return o + something;
                        }
                                // C
                        private class Inner {
                            // D
                        }
                    }
                """;
        var launcher = new Launcher();
        CtElement testObject = launcher.getFactory().createCodeSnippetStatement(raw);

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @RepeatedTest(7)
    public void applyToInnerClass_InnerAndOuterHaveMethods_shouldNotBeApplied(){
        // This does not match the transformers purpose - but it should also not throw an error!
        String raw = """
                public class Outer {
                        public int outerDoSomething(int o){
                            int con = 5;
                            // A
                            return o + con;
                            // B
                        }
                                //C 
                        private class Inner {
                            public int innerDoSomething(int i){
                                int cin = 15;
                                return i + cin;
                                // D
                            }
                        }
                    }
                """;
        var launcher = new Launcher();
        CtElement testObject = launcher.getFactory().createCodeSnippetStatement(raw);

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToSynchronizedMethod_shouldNotBeApplied(){
        String raw = """
                public class example_class {
                // B
                    public synchronized void someMethod() {
                        int b = 5;
                        // A
                        System.out.println("Hello Synchronized World");
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToSynchronizedStaticMethod_shouldBeApplied(){
        String raw = """
                public class example_class {
                    public static synchronized void someMethod() {
                        int b = 5;
                        System.out.println("Hello Synchronized World");
                        // A
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertNotEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @RepeatedTest(3)
    public void applyToMethodWithSynchronizedBlock_shouldBeApplied(){
        String raw = """
                public class example_class {
                    public void someMethod() {
                        synchronized (this) {
                            int b = 5;
                            // B
                            System.out.println("Hello Synchronized World");
                        }
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertNotEquals(new EmptyTransformationResult(),result);
    }

    /*
    ========================================================
                   Equality & HashCode Tests
    ========================================================
     */

    @Test
    void testEquals_Reflexivity(){
        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer(2022);

        assertEquals(transformer,transformer);
    }

    @Test
    void testEquals_TwoTransformers_differentSeeds_areNotEquals(){
        RemoveAllCommentsTransformer t1 = new RemoveAllCommentsTransformer(1);
        RemoveAllCommentsTransformer t2 = new RemoveAllCommentsTransformer(2);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoTransformers_differentSeeds_seedsAreChangedAfterCreation_areNotEquals(){
        RemoveAllCommentsTransformer t1 = new RemoveAllCommentsTransformer(1);
        RemoveAllCommentsTransformer t2 = new RemoveAllCommentsTransformer(1);
        t2.setSeed(2);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoTransformers_differentTryingToCompile_areNotEquals(){
        RemoveAllCommentsTransformer t1 = new RemoveAllCommentsTransformer(1);
        t1.setTryingToCompile(false);
        RemoveAllCommentsTransformer t2 = new RemoveAllCommentsTransformer(1);
        t2.setTryingToCompile(true);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoTransformers_differentAutoImports_areNotEquals(){
        RemoveAllCommentsTransformer t1 = new RemoveAllCommentsTransformer(1);
        t1.setSetsAutoImports(false);
        RemoveAllCommentsTransformer t2 = new RemoveAllCommentsTransformer(1);
        t2.setSetsAutoImports(true);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoFreshTransformers_areEqual(){
        RemoveAllCommentsTransformer t1 = new RemoveAllCommentsTransformer(5);
        RemoveAllCommentsTransformer t2 = new RemoveAllCommentsTransformer(5);

        assertEquals(t1,t2);
    }

    @Test
    void testHashCode_FreshTransformer_isNotNull(){
        RemoveAllCommentsTransformer transformer = new RemoveAllCommentsTransformer(10);

        int result = transformer.hashCode();

        assertNotNull(result);
        assertNotEquals(0,result);
    }

    @Test
    void testHashCode_TransformerWithSameSeeds_haveSameHashCode(){
        RemoveAllCommentsTransformer t1 = new RemoveAllCommentsTransformer(1);
        RemoveAllCommentsTransformer t2 = new RemoveAllCommentsTransformer(1);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithDifferentSeeds_haveDifferentHashCode(){
        RemoveAllCommentsTransformer t1 = new RemoveAllCommentsTransformer(1);
        RemoveAllCommentsTransformer t2 = new RemoveAllCommentsTransformer(2);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertNotEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithTryingToCompile_haveDifferentHashCode(){
        RemoveAllCommentsTransformer t1 = new RemoveAllCommentsTransformer(5);
        t1.setTryingToCompile(true);
        RemoveAllCommentsTransformer t2 = new RemoveAllCommentsTransformer(5);
        t2.setTryingToCompile(false);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertNotEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithSetsAutoImports_haveDifferentHashCode(){
        RemoveAllCommentsTransformer t1 = new RemoveAllCommentsTransformer(5);
        t1.setSetsAutoImports(true);
        RemoveAllCommentsTransformer t2 = new RemoveAllCommentsTransformer(5);
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

    static CtElement commentExample(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; // some comment \n class A { int sum(int a, int b) { return a + b;} }");

        return testObject;
    }

}
