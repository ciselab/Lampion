package com.github.ciselab.lampion.core.transformations;

import com.github.ciselab.lampion.core.transformations.transformers.EmptyMethodTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.IfFalseElseTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.IfFalseElseTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.IfTrueTransformer;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

public class IfFalseElseTransformerTests {

    @Test
    void applyToMethodWithReturn_ASTshouldHaveElseBlock(){
        CtElement ast = sumExample();

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.applyAtRandom(ast);

        assertTrue(ast.toString().contains("if (false)"));
        assertTrue(ast.toString().contains("else"));
        assertTrue(ast.toString().contains("return 0;"));
    }

    @Test
    void applyToMethodWithoutReturn_ASTshouldHaveElseBlock(){
        CtElement ast = classWithoutReturnMethod();

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.applyAtRandom(ast);

        assertTrue(ast.toString().contains("if (false)"));
        assertTrue(ast.toString().contains("else"));
        assertFalse(ast.toString().contains("return null;"));
    }


    @Tag("Regression")
    @Test
    void applyToMethodWithFloatReturn_ElseBlockShouldHave0fInIt(){
        CtClass testObject = Launcher.parseClass(
                "package lampion.test.examples; class A { float sum(float a, float b) { return a + b;} }");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.applyAtRandom(testObject);

        assertTrue(testObject.toString().contains("if (false)"));
        assertTrue(testObject.toString().contains("return 0.0F;"));
        assertFalse(testObject.toString().contains("return null;"));
    }

    @Tag("Regression")
    @Test
    void applyToMethodWithDoubleReturn_ElseBlockShouldHave0dInIt(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { double sum(double a, double b) { return a + b;} }");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.applyAtRandom(testObject);

        assertTrue(testObject.toString().contains("if (false)"));
        assertTrue(testObject.toString().contains("return 0.0;"));
        assertFalse(testObject.toString().contains("return null;"));
    }

    @Tag("Regression")
    @Test
    void applyInMethod_ShouldHaveClassParent(){
        // This appeared after adding either the Lambda Transformer or IfFalseElse Transformer
        // There was an issue that there was no parent method element which (can) be true for lambdas
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { double sum(double a, double b) { return a + b;} }");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        var result = transformer.applyAtRandom(testObject);

        var classParent = result.getTransformedElement().getParent(u -> u instanceof CtClass);

        assertNotNull(classParent);
    }

    @Tag("Regression")
    @Test
    void applyInMethod_changedElementShouldBeMethod_andHaveSimpleName(){
        // This appeared after adding either the Lambda Transformer or IfFalseElse Transformer
        // There was an issue that there was no parent method element which (can) be true for lambdas
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { double sum(double a, double b) { return a + b;} }");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        var result = transformer.applyAtRandom(testObject);

        var elem = result.getTransformedElement();

        assertTrue(elem instanceof CtMethod);

        assertNotNull(((CtMethod<?>) elem).getSimpleName());
    }

    @Tag("Regression")
    @Test
    void applyToMethodWithStringReturn_ElseBlockShouldReturnNull(){
        CtClass testObject = Launcher.parseClass(
                "package lampion.test.examples; class A { String sum(String a, String b) { return a + b;} }");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.applyAtRandom(testObject);

        assertTrue(testObject.toString().contains("if (false)"));
        assertTrue(testObject.toString().contains("return null;"));
    }

    @Tag("Regression")
    @Test
    void applyToMethodWithFloatReturn_ShouldCompile(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { float sum(float a, float b) { return a + b;} }");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.applyAtRandom(testObject);

        testObject.compileAndReplaceSnippets();
    }

    @RepeatedTest(5)
    void applyToClassWithTwoMethods_onlyOneIsAltered(){
        CtClass ast = Launcher.parseClass("package lampion.test.examples; class A { " +
                "int sum(int a, int b) { return a + b;} " +
                "void some(){System.out.println(\"hey!\");}" +
                "}");

        CtMethod methodA = (CtMethod) ast.filterChildren(c -> c instanceof CtMethod).list().get(0);
        CtMethod methodB = (CtMethod) ast.filterChildren(c -> c instanceof CtMethod).list().get(1);

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.applyAtRandom(ast);

        Predicate<CtMethod> isAltered = m -> m.toString().contains("if (false)");

        boolean methodAAltered = isAltered.test(methodA);
        boolean methodBAltered = isAltered.test(methodB);

        // The operator "^" is the XOR operator
        assertTrue(methodAAltered ^ methodBAltered);
    }


    @Test
    void testApplyTo_ClassWithNoMethods_ShouldNotAlterAnything(){
        CtClass ast = Launcher.parseClass("package lampion.test.examples; \n " +
                "class A { \n" +
                "}");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer(25);
        transformer.applyAtRandom(ast);

        Predicate<CtElement> isAltered = m -> m.toString().contains("if (false)");
        assertFalse(isAltered.test(ast));
    }

    @Test
    void testApplyTo_ClassWithNoMethods_ShouldReturnEmptyResult(){
        CtClass ast = Launcher.parseClass("package lampion.test.examples; \n " +
                "class A { \n" +
                "}");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer(25);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Test
    void testExclusive_isExclusiveWithNothing(){
        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        assertTrue(transformer.isExclusiveWith().isEmpty());
    }

    @Test
    void constraintsAreNotSatisfied_ReturnsEmptyResult(){
        CtClass emptyClass = Launcher.parseClass("class A { }");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();
        TransformationResult result = transformer.applyAtRandom(emptyClass);

        assertEquals(new EmptyTransformationResult(), result);
    }

    @Test
    void applyToMethod_CheckTransformationResult_ElementInResultIsNotAltered(){
        CtElement ast = sumExample();

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        String toCheck = result.getTransformedElement().toString();

        assertFalse(toCheck.contains("if(false)"));
        assertFalse(toCheck.contains("else"));
        assertFalse(toCheck.contains("return null;"));
    }

    @Test
    void applyToMethod_CheckTransformationResult_nameIsIfTrue(){
        CtElement ast = sumExample();

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertEquals("IfFalseElse",result.getTransformationName());
    }

    @Test
    void applyToMethod_CheckTransformationResult_categoriesNotEmpty(){
        CtElement ast = sumExample();

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getCategories().isEmpty());
    }

    @Test
    void applyWithoutDebugSettingOn_TransformationResultShouldHaveNoOptionalInfo(){
        CtElement ast = sumExample();

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.setDebug(false);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getInitialScopeOfTransformation().isPresent());
        assertFalse(result.getBeforeAfterComparison().isPresent());
    }

    @Test
    void applyWithDebugSettingOn_TransformationResultShouldHaveMoreInfo(){
        CtElement ast = sumExample();

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.setDebug(true);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertTrue(result.getInitialScopeOfTransformation().isPresent());
        assertTrue(result.getBeforeAfterComparison().isPresent());
    }


    @Tag("Regression")
    @Test
    void applyToMethodWithCharReturn_ElseBlockShouldHaveCharacterMinValueInIt(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { char getA() { return 'a';} }");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.applyAtRandom(testObject);

        assertTrue(testObject.toString().contains("if (false)"));
        assertTrue(testObject.toString().contains("return Character.MIN_VALUE;"));
        assertFalse(testObject.toString().contains("return null;"));
    }

    @Tag("Regression")
    @Test
    void applyToMethodWithCharReturn_ElseBlockShouldCompile(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { char getA() { return 'a';} }");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.setTryingToCompile(true);

        // This could throw an error
        transformer.applyAtRandom(testObject);
        // Pass test if no error is thrown
        return;
    }

    @Tag("Regression")
    @Test
    void applyToMethodWithShortReturn_ElseBlockShouldCompile(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { short getA() { return 1;} }");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.setTryingToCompile(true);

        // This could throw an error
        transformer.applyAtRandom(testObject);
        // Pass test if no error is thrown
        return;
    }

    @Tag("Regression")
    @Test
    void applyToMethodWithByteReturn_ElseBlockShouldCompile(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { byte getA() { return 1;} }");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.setTryingToCompile(true);

        // This could throw an error
        transformer.applyAtRandom(testObject);
        // Pass test if no error is thrown
        return;
    }

    @Tag("Regression")
    @Test
    void applyToMethodWithLongReturn_ElseBlockShouldCompile(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { long getA() { return 1;} }");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.setTryingToCompile(true);

        // This could throw an error
        transformer.applyAtRandom(testObject);
        // Pass test if no error is thrown
        return;
    }

    @Test
    void applyToMethodWithShortReturn_ifBlockShouldHaveDefaultValue(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { short getA() { return 1;} }");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.applyAtRandom(testObject);
        assertTrue(testObject.toString().contains("return 0;"));
    }

    @Test
    void applyToMethodWithByteReturn_ifBlockShouldHaveDefaultValue(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { byte getA() { return 1;} }");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.applyAtRandom(testObject);

        assertTrue(testObject.toString().contains("return 0;"));
    }

    @Test
    void applyToMethodWithLongReturn_ifBlockShouldHaveDefaultValue(){
        CtClass testObject = Launcher.parseClass("package lampion.test.examples; class A { long getA() { return 1;} }");

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();

        transformer.applyAtRandom(testObject);
        assertTrue(testObject.toString().contains("return 0L;"));
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
                        System.out.println("Hello Abstract World");
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
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

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();
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

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();
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

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();
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

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();
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

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();
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

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();
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

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();
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

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();
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

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();
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

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();
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

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();
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

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();
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

        IfFalseElseTransformer transformer = new IfFalseElseTransformer();
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
        IfFalseElseTransformer transformer = new IfFalseElseTransformer(2022);

        assertEquals(transformer,transformer);
    }

    @Test
    void testEquals_TwoTransformers_differentSeeds_areNotEquals(){
        IfFalseElseTransformer t1 = new IfFalseElseTransformer(1);
        IfFalseElseTransformer t2 = new IfFalseElseTransformer(2);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoTransformers_differentSeeds_seedsAreChangedAfterCreation_areNotEquals(){
        IfFalseElseTransformer t1 = new IfFalseElseTransformer(1);
        IfFalseElseTransformer t2 = new IfFalseElseTransformer(1);
        t2.setSeed(2);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoTransformers_differentTryingToCompile_areNotEquals(){
        IfFalseElseTransformer t1 = new IfFalseElseTransformer(1);
        t1.setTryingToCompile(false);
        IfFalseElseTransformer t2 = new IfFalseElseTransformer(1);
        t2.setTryingToCompile(true);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoTransformers_differentAutoImports_areNotEquals(){
        IfFalseElseTransformer t1 = new IfFalseElseTransformer(1);
        t1.setSetsAutoImports(false);
        IfFalseElseTransformer t2 = new IfFalseElseTransformer(1);
        t2.setSetsAutoImports(true);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoFreshTransformers_areEqual(){
        IfFalseElseTransformer t1 = new IfFalseElseTransformer(5);
        IfFalseElseTransformer t2 = new IfFalseElseTransformer(5);

        assertEquals(t1,t2);
    }

    @Test
    void testHashCode_FreshTransformer_isNotNull(){
        IfFalseElseTransformer transformer = new IfFalseElseTransformer(10);

        int result = transformer.hashCode();

        assertNotNull(result);
        assertNotEquals(0,result);
    }

    @Test
    void testHashCode_TransformerWithSameSeeds_haveSameHashCode(){
        IfFalseElseTransformer t1 = new IfFalseElseTransformer(1);
        IfFalseElseTransformer t2 = new IfFalseElseTransformer(1);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithDifferentSeeds_haveDifferentHashCode(){
        IfFalseElseTransformer t1 = new IfFalseElseTransformer(1);
        IfFalseElseTransformer t2 = new IfFalseElseTransformer(2);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertNotEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithTryingToCompile_haveDifferentHashCode(){
        IfFalseElseTransformer t1 = new IfFalseElseTransformer(5);
        t1.setTryingToCompile(true);
        IfFalseElseTransformer t2 = new IfFalseElseTransformer(5);
        t2.setTryingToCompile(false);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertNotEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithSetsAutoImports_haveDifferentHashCode(){
        IfFalseElseTransformer t1 = new IfFalseElseTransformer(5);
        t1.setSetsAutoImports(true);
        IfFalseElseTransformer t2 = new IfFalseElseTransformer(5);
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
