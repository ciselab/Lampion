package com.github.ciselab.lampion.core.transformations;

import com.github.ciselab.lampion.core.transformations.transformers.*;
import com.github.ciselab.lampion.core.transformations.transformers.RandomParameterNameTransformer;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
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

    @RepeatedTest(5)
    void applyToClassWithTwoMethods_onlyOneIsAltered(){
        CtClass ast = Launcher.parseClass("package lampion.test; class A { " +
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

    @RepeatedTest(5)
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
    void applyToMethodWithMainMethod_returnsEmptyTransformationResult(){
        //This test checks that the main method is kept untouched
        CtElement ast = Launcher.parseClass("" +
                "package lampion.test.examples; class A {" +
                "public void main(String[] args){" +
                "System.out.println(\"Hello World!\");" +
                "}" +
                "}" +
                "");

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();

        transformer.applyAtRandom(ast);
        var result = transformer.applyAtRandom(ast);

        assertEquals(new EmptyTransformationResult(),result);
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
        CtClass ast = Launcher.parseClass("package lampion.test; class A { " +
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

    @Tag("Regression")
    @Test
    void applyTwentyTimesToAMethod_methodShouldNotLooseParameters(){
        // There was an issue with renaming variables too often, that it is not applied
        CtClass ast = Launcher.parseClass("package lampion.test; class A { " +
                "int sum(int a, int b) { return a + b;} " +
                "}");

        CtMethod sumMethod = (CtMethod) ast.filterChildren(c -> c instanceof CtMethod).list().get(0);

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();

        for(int i = 0; i<20;i++) {
            transformer.applyAtRandom(ast);
        }
        assertEquals(2,sumMethod.getParameters().size());
    }

    @Test
    void applyWithFullRandom_shouldGiveNonEmptyResult(){
        CtElement ast = sumExample();

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();

        transformer.setFullRandomStrings(true);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertNotEquals(new EmptyTransformationResult(),result);
    }

    @Test
    void applyToMethodWithoutParameters_returnsEmptyTransformationResult(){
        CtClass ast = Launcher.parseClass("package lampion.test; class A { " +
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
    void applyToClassWithoutMethods_withPackage_returnsEmptyTransformationResult(){
        CtClass ast = Launcher.parseClass("package lampion.test.package; \n" +
                "class A { \n" +
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

    @Test
    void testGetStringRandomness_onFullRandom_ShouldGiveFullRandom(){
        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();
        transformer.setFullRandomStrings(true);

        assertTrue(transformer.isFullRandomStrings());
    }

    @Test
    void testGetStringRandomness_onPseudoRandom_ShouldGivePseudoRandom(){
        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();
        transformer.setFullRandomStrings(false);

        assertFalse(transformer.isFullRandomStrings());
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
    public void applyToAbstractClassWithAbstractMethod_shouldBeApplied(){
        String raw = """
                public abstract class example_abstract_class {
                    public abstract void abstract_method(int b) {
                        int a = 5;
                        System.out.println("Hello Abstract World" + b);
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertNotEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToAbstractClassWithConcreteMethod_shouldBeApplied(){
        String raw = """
                public abstract class example_abstract_class {
                    public void normal_method(int b){
                        System.out.println("Hello World");
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertNotEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @RepeatedTest(3)
    public void applyToAbstractClassWithConcreteAndAbstractMethod_shouldBeApplied(){
        String raw = """
                public abstract class example_abstract_class {
                    public void normal_method(int aa){
                        int a = 5;
                        System.out.println("Hello World");
                    }
                    
                    public abstract void abstract_method(int bb) {
                        int b = 5;
                        System.out.println("Hello Abstract World");
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertNotEquals(new EmptyTransformationResult(),result);
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

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToInterface_shouldNotBeApplied(){
        String raw = """
                public interface example_interface {
                    
                    public int doSomething(int b);
                }
                """;
        var launcher = new Launcher();
        CtElement testObject = launcher.getFactory().createCodeSnippetStatement(raw);

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToInterfaceWithDefaultMethod_shouldNotBeApplied(){
        String raw = """
                public interface example_interface {
                    
                    public default int doSomething(int a){
                        int b = 5;
                        return 5;
                    }
                    
                }
                """;

        var launcher = new Launcher();
        CtElement testObject = launcher.getFactory().createCodeSnippetStatement(raw);

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();
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
                    WEST
                }
                """;
        var launcher = new Launcher();
        CtElement testObject = launcher.getFactory().createCodeSnippetStatement(raw);

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToInnerClassWithoutMethods_shouldNotBeApplied(){
        // This does not match the transformers purpose - but it should also not throw an error!
        String raw = """
                public class Outer {  
                        private class Inner {}
                }
                """;
        var launcher = new Launcher();
        CtElement testObject = launcher.getFactory().createCodeSnippetStatement(raw);

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @RepeatedTest(5)
    public void applyToInnerClassWithMethods_shouldNotBeApplied(){
        // This does not match the transformers purpose - but it should also not throw an error!
        String raw = """
                public class Outer {  
                        private class Inner {
                            public int innerDoSomething(int i){
                                int something = 15;
                                return i + something;
                            }
                        }
                }
                """;
        var launcher = new Launcher();
        CtElement testObject = launcher.getFactory().createCodeSnippetStatement(raw);

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @RepeatedTest(5)
    public void applyToInnerClass_InnerHasNotAndOuterHasMethod_shouldNotBeApplied(){
        // This does not match the transformers purpose - but it should also not throw an error!
        String raw = """
                public class Outer {
                        public int outerDoSomething(int o){
                            int something = 5;
                            return o + something;
                        }
                                
                        private class Inner {}
                    }
                """;
        var launcher = new Launcher();
        CtElement testObject = launcher.getFactory().createCodeSnippetStatement(raw);

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();
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
                            return o + con;
                        }
                                
                        private class Inner {
                            public int innerDoSomething(int i){
                                int cin = 15;
                                return i + cin;
                            }
                        }
                    }
                """;
        var launcher = new Launcher();
        CtElement testObject = launcher.getFactory().createCodeSnippetStatement(raw);

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToSynchronizedMethod_shouldBeApplied(){
        String raw = """
                public class example_class {
                    public synchronized void someMethod(int a) {
                        int b = 5;
                        System.out.println("Hello Synchronized World");
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertNotEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToSynchronizedStaticMethod_shouldBeApplied(){
        String raw = """
                public class example_class {
                    public static synchronized void someMethod(int a) {
                        int b = 5;
                        System.out.println("Hello Synchronized World");
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertNotEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToMethodWithSynchronizedBlock_shouldBeApplied(){
        String raw = """
                public class example_class {
                    public void someMethod(int a) {
                        synchronized (this) {
                            int b = 5;
                            System.out.println("Hello Synchronized World");
                        }
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer();
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
        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer(2022);

        assertEquals(transformer,transformer);
    }

    @Test
    void testEquals_TwoTransformers_differentSeeds_areNotEquals(){
        RandomParameterNameTransformer t1 = new RandomParameterNameTransformer(1);
        RandomParameterNameTransformer t2 = new RandomParameterNameTransformer(2);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoTransformers_differentSeeds_seedsAreChangedAfterCreation_areNotEquals(){
        RandomParameterNameTransformer t1 = new RandomParameterNameTransformer(1);
        RandomParameterNameTransformer t2 = new RandomParameterNameTransformer(1);
        t2.setSeed(2);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoTransformers_differentTryingToCompile_areNotEquals(){
        RandomParameterNameTransformer t1 = new RandomParameterNameTransformer(1);
        t1.setTryingToCompile(false);
        RandomParameterNameTransformer t2 = new RandomParameterNameTransformer(1);
        t2.setTryingToCompile(true);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoTransformers_differentAutoImports_areNotEquals(){
        RandomParameterNameTransformer t1 = new RandomParameterNameTransformer(1);
        t1.setSetsAutoImports(false);
        RandomParameterNameTransformer t2 = new RandomParameterNameTransformer(1);
        t2.setSetsAutoImports(true);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoFreshTransformers_areEqual(){
        RandomParameterNameTransformer t1 = new RandomParameterNameTransformer(5);
        RandomParameterNameTransformer t2 = new RandomParameterNameTransformer(5);

        assertEquals(t1,t2);
    }


    @Test
    void testEquals_TransformerWithDifferentStringRandomness_areNotEqual(){
        RandomParameterNameTransformer t1 = new RandomParameterNameTransformer(1);
        t1.setFullRandomStrings(true);
        RandomParameterNameTransformer t2 = new RandomParameterNameTransformer(1);
        t2.setFullRandomStrings(false);

        assertNotEquals(t1,t2);
    }

    @Test
    void testHashCode_FreshTransformer_isNotNull(){
        RandomParameterNameTransformer transformer = new RandomParameterNameTransformer(10);

        int result = transformer.hashCode();

        assertNotNull(result);
        assertNotEquals(0,result);
    }

    @Test
    void testHashCode_TransformerWithSameSeeds_haveSameHashCode(){
        RandomParameterNameTransformer t1 = new RandomParameterNameTransformer(1);
        RandomParameterNameTransformer t2 = new RandomParameterNameTransformer(1);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithDifferentStringRandomness_haveSameDifferentHashCode(){
        RandomParameterNameTransformer t1 = new RandomParameterNameTransformer(1);
        t1.setFullRandomStrings(true);
        RandomParameterNameTransformer t2 = new RandomParameterNameTransformer(1);
        t2.setFullRandomStrings(false);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertNotEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithDifferentSeeds_haveDifferentHashCode(){
        RandomParameterNameTransformer t1 = new RandomParameterNameTransformer(1);
        RandomParameterNameTransformer t2 = new RandomParameterNameTransformer(2);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertNotEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithTryingToCompile_haveDifferentHashCode(){
        RandomParameterNameTransformer t1 = new RandomParameterNameTransformer(5);
        t1.setTryingToCompile(true);
        RandomParameterNameTransformer t2 = new RandomParameterNameTransformer(5);
        t2.setTryingToCompile(false);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertNotEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithSetsAutoImports_haveDifferentHashCode(){
        RandomParameterNameTransformer t1 = new RandomParameterNameTransformer(5);
        t1.setSetsAutoImports(true);
        RandomParameterNameTransformer t2 = new RandomParameterNameTransformer(5);
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
