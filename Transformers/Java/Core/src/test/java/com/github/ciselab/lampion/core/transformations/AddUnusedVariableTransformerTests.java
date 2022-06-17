package com.github.ciselab.lampion.core.transformations;

import com.github.ciselab.lampion.core.transformations.transformers.AddNeutralElementTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.AddUnusedVariableTransformer;
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
        AddUnusedVariableTransformer elementTransformer = new AddUnusedVariableTransformer();

        CtElement ast =  Launcher.parseClass("package lampion.test.examples; class A { " +
                "void some(){}" +
                "}");

        var result1 = variableTransformer.applyAtRandom(ast);
        var result2 = elementTransformer.applyAtRandom(ast);

        assertNotEquals(new EmptyTransformationResult(),result1);
        assertTrue(ast.toString().contains("="));
        assertNotEquals(new EmptyTransformationResult(),result2);
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
                    public abstract void abstract_method() {
                        System.out.println("Hello Abstract World");
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertNotEquals(new EmptyTransformationResult(),result);
    }


    @Tag("Regression")
    @Test
    public void applyToAbstractClassWithConcreteMethod_shouldBeApplied(){
        String raw = """
                public abstract class example_abstract_class {
                    public void normal_method(){
                        System.out.println("Hello World");
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertNotEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @RepeatedTest(3)
    public void applyToAbstractClassWithConcreteAndAbstractMethod_shouldBeApplied(){
        String raw = """
                public abstract class example_abstract_class {
                    public void normal_method(){
                        System.out.println("Hello World");
                    }
                    
                    public abstract void abstract_method() {
                        System.out.println("Hello Abstract World");
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();
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

        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToInterface_shouldNotBeApplied(){
        String raw = """
                public interface example_interface {
                    
                    public int doSomething();
                }
                """;
        var launcher = new Launcher();
        CtElement testObject = launcher.getFactory().createCodeSnippetStatement(raw);

        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToInterfaceWithDefaultMethod_shouldNotBeApplied(){
        String raw = """
                public interface example_interface {
                    
                    public default int doSomething(){
                        return 5;
                    }
                    
                }
                """;

        var launcher = new Launcher();
        CtElement testObject = launcher.getFactory().createCodeSnippetStatement(raw);

        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();
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

        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();
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

        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();
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

        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();
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

        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();
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

        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToSynchronizedMethod_shouldBeApplied(){
        String raw = """
                public class example_class {
                    public synchronized void someMethod() {
                        System.out.println("Hello Synchronized World");
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertNotEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToSynchronizedStaticMethod_shouldBeApplied(){
        String raw = """
                public class example_class {
                    public static synchronized void someMethod() {
                        System.out.println("Hello Synchronized World");
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertNotEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToMethodWithSynchronizedBlock_shouldBeApplied(){
        String raw = """
                public class example_class {
                    public void someMethod() {
                        synchronized (this) {
                            System.out.println("Hello Synchronized World");
                        }
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer();
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
        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer(2022);

        assertEquals(transformer,transformer);
    }

    @Test
    void testEquals_TwoTransformers_differentSeeds_areNotEquals(){
        AddUnusedVariableTransformer t1 = new AddUnusedVariableTransformer(1);
        AddUnusedVariableTransformer t2 = new AddUnusedVariableTransformer(2);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoTransformers_differentSeeds_seedsAreChangedAfterCreation_areNotEquals(){
        AddUnusedVariableTransformer t1 = new AddUnusedVariableTransformer(1);
        AddUnusedVariableTransformer t2 = new AddUnusedVariableTransformer(1);
        t2.setSeed(2);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoTransformers_differentTryingToCompile_areNotEquals(){
        AddUnusedVariableTransformer t1 = new AddUnusedVariableTransformer(1);
        t1.setTryingToCompile(false);
        AddUnusedVariableTransformer t2 = new AddUnusedVariableTransformer(1);
        t2.setTryingToCompile(true);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoTransformers_differentAutoImports_areNotEquals(){
        AddUnusedVariableTransformer t1 = new AddUnusedVariableTransformer(1);
        t1.setSetsAutoImports(false);
        AddUnusedVariableTransformer t2 = new AddUnusedVariableTransformer(1);
        t2.setSetsAutoImports(true);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoFreshTransformers_areEqual(){
        AddUnusedVariableTransformer t1 = new AddUnusedVariableTransformer(5);
        AddUnusedVariableTransformer t2 = new AddUnusedVariableTransformer(5);

        assertEquals(t1,t2);
    }


    @Test
    void testEquals_TransformerWithDifferentStringRandomness_areNotEqual(){
        AddUnusedVariableTransformer t1 = new AddUnusedVariableTransformer(1);
        t1.setFullRandomStrings(true);
        AddUnusedVariableTransformer t2 = new AddUnusedVariableTransformer(1);
        t2.setFullRandomStrings(false);

        assertNotEquals(t1,t2);
    }

    @Test
    void testHashCode_FreshTransformer_isNotNull(){
        AddUnusedVariableTransformer transformer = new AddUnusedVariableTransformer(10);

        int result = transformer.hashCode();

        assertNotNull(result);
        assertNotEquals(0,result);
    }

    @Test
    void testHashCode_TransformerWithSameSeeds_haveSameHashCode(){
        AddUnusedVariableTransformer t1 = new AddUnusedVariableTransformer(1);
        AddUnusedVariableTransformer t2 = new AddUnusedVariableTransformer(1);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithDifferentStringRandomness_haveSameDifferentHashCode(){
        AddUnusedVariableTransformer t1 = new AddUnusedVariableTransformer(1);
        t1.setFullRandomStrings(true);
        AddUnusedVariableTransformer t2 = new AddUnusedVariableTransformer(1);
        t2.setFullRandomStrings(false);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertNotEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithDifferentSeeds_haveDifferentHashCode(){
        AddUnusedVariableTransformer t1 = new AddUnusedVariableTransformer(1);
        AddUnusedVariableTransformer t2 = new AddUnusedVariableTransformer(2);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertNotEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithTryingToCompile_haveDifferentHashCode(){
        AddUnusedVariableTransformer t1 = new AddUnusedVariableTransformer(5);
        t1.setTryingToCompile(true);
        AddUnusedVariableTransformer t2 = new AddUnusedVariableTransformer(5);
        t2.setTryingToCompile(false);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertNotEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithSetsAutoImports_haveDifferentHashCode(){
        AddUnusedVariableTransformer t1 = new AddUnusedVariableTransformer(5);
        t1.setSetsAutoImports(true);
        AddUnusedVariableTransformer t2 = new AddUnusedVariableTransformer(5);
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

    static CtElement classWithoutReturnMethod(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { void m() { System.out.println(\"yeah\");} }");

        return testObject;
    }

    static CtElement sumExample(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { int sum(int a, int b) { return a + b;} }");

        return testObject;
    }

}
