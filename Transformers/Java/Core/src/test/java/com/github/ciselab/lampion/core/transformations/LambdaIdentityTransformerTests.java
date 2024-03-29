package com.github.ciselab.lampion.core.transformations;

import com.github.ciselab.lampion.core.transformations.transformers.IfTrueTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.LambdaIdentityTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.LambdaIdentityTransformer;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;

import static org.junit.jupiter.api.Assertions.*;

public class LambdaIdentityTransformerTests {

    @Tag("File")
    @Test
    void testApplyToClassWithoutLiterals_returnsEmptyResult(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        CtElement ast = classWithoutLiteral();

        var result = transformer.applyAtRandom(ast);

        assertEquals(new EmptyTransformationResult(), result);
    }

    @Tag("File")
    @Test
    void testApplyToClassWithLiterals_shouldBeApplied(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        CtElement ast = addOneExample();

        transformer.applyAtRandom(ast);

        assertTrue(ast.toString().contains("()"));
        assertTrue(ast.toString().contains("->"));
        assertTrue(ast.toString().contains("1"));
        assertTrue(ast.toString().contains(".get()"));
    }

    @Tag("File")
    @Test
    void testApplyToClassWithLiteralsAsFields_shouldBeApplied(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        CtClass ast = (CtClass) heyExample();

        transformer.applyAtRandom(ast);

        assertTrue(ast.toString().contains("()"));
        assertTrue(ast.toString().contains("->"));
        assertTrue(ast.toString().contains("hey"));
        assertTrue(ast.toString().contains(".get()"));
    }


    @Tag("File")
    @Test
    void testApplyToClassWithLiterals_StringLiteral_shouldBeApplied(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        CtClass ast = (CtClass) bPlusHeyExample();

        transformer.applyAtRandom(ast);

        assertTrue(ast.toString().contains("()"));
        assertTrue(ast.toString().contains("->"));
        assertTrue(ast.toString().contains("hey"));
        assertTrue(ast.toString().contains(".get()"));
    }

    @Tag("File")
    @Test
    void testApplyToClassWithMultipleLiterals_Apply20Times_shouldNotBreak(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        CtClass ast = (CtClass) bPlusHeyExample();

        TransformationResult lastResult = null;
        for(int i = 0; i<20;i++)
            lastResult = transformer.applyAtRandom(ast);

        assertNotNull(lastResult);
        assertNotEquals(new EmptyTransformationResult(),lastResult);
    }

    @Tag("Regression")
    @Tag("File")
    @Test
    void testApplyToClassWithLiterals_applyTwice_shouldBeAppliedTwice(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();
        transformer.setSetsAutoImports(true);

        CtElement ast = addOneExample();

        transformer.applyAtRandom(ast);
        var result = transformer.applyAtRandom(ast);

        assertNotEquals(new EmptyTransformationResult(), result);
    }

    @Tag("Regression")
    @Tag("File")
    @Test
    void testApplyToClassWithLiterals_applyTwice_noAutoImports_shouldBeAppliedTwice(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();
        transformer.setSetsAutoImports(false);

        CtElement ast = addOneExample();

        transformer.applyAtRandom(ast);
        var result = transformer.applyAtRandom(ast);

        assertNotEquals(new EmptyTransformationResult(), result);
    }

    @Tag("Regression")
    @Tag("File")
    @Test
    void testApplyToClassWithLiterals_applyTwice_noAutoImports_NoCompile_shouldBeAppliedOnce(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();
        transformer.setSetsAutoImports(false);
        transformer.setTryingToCompile(false);

        CtElement ast = addOneExample();

        transformer.applyAtRandom(ast);
        var result = transformer.applyAtRandom(ast);

        assertEquals(new EmptyTransformationResult(), result);
    }

    @Test
    void testExclusive_isExclusiveWithNothing(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        assertTrue(transformer.isExclusiveWith().isEmpty());
    }

    @Tag("Regression")
    @Test
    void applyOnGlobalVariable_ShouldNotHaveMethodParent_ShouldHaveClassParent(){
        // This appeared after adding either the Lambda Transformer or IfFalseElse Transformer
        // There was an issue that there was no parent method element which (can) be true for lambdas

        CtElement ast = Launcher.parseClass("package lampion.test.examples; class A {" +
                "int a = 2;" +
                "}");

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        var methodParent = result.getTransformedElement().getParent(u -> u instanceof CtMethod);
        var classParent = result.getTransformedElement().getParent(u -> u instanceof CtClass);

        assertNull(methodParent);
        assertNotNull(classParent);
    }

    @Tag("File")
    @Tag("Regression")
    @Test
    void applyInMethod_ShouldHaveMethodParent_ShouldHaveClassParent(){
        // This appeared after adding either the Lambda Transformer or IfFalseElse Transformer
        // There was an issue that there was no parent method element which (can) be true for lambdas

        CtElement ast = addOneExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        var methodParent = result.getTransformedElement().getParent(u -> u instanceof CtMethod);
        var classParent = result.getTransformedElement().getParent(u -> u instanceof CtClass);

        assertNotNull(methodParent);
        assertNotNull(classParent);
    }

    @Test
    void constraintsAreNotSatisfied_ReturnsEmptyResult(){
        CtClass emptyClass = Launcher.parseClass("class A { }");

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();
        TransformationResult result = transformer.applyAtRandom(emptyClass);

        assertEquals(new EmptyTransformationResult(), result);
    }

    @Tag("File")
    @Test
    void applyToMethod_CheckTransformationResult_nameIsLambdaIdentity(){
        CtElement ast = addOneExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertEquals("LambdaIdentity",result.getTransformationName());
    }

    @Tag("File")
    @Test
    void applyToMethod_CheckTransformationResult_categoriesNotEmpty(){
        CtElement ast = addOneExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getCategories().isEmpty());
    }

    @Tag("File")
    @Test
    void applyWithoutDebugSettingOn_TransformationResultShouldHaveNoOptionalInfo(){
        CtElement ast = addOneExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        transformer.setDebug(false);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertFalse(result.getInitialScopeOfTransformation().isPresent());
        assertFalse(result.getBeforeAfterComparison().isPresent());
    }

    @Tag("File")
    @Test
    void applyWithDebugSettingOn_TransformationResultShouldHaveMoreInfo(){
        CtElement ast = addOneExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        transformer.setDebug(true);

        TransformationResult result = transformer.applyAtRandom(ast);

        assertTrue(result.getInitialScopeOfTransformation().isPresent());
        assertTrue(result.getBeforeAfterComparison().isPresent());
    }

    @Tag("File")
    @Test
    void applyToAddOneExample_shouldHaveImportInPrettyPrinted(){
        CtClass ast = (CtClass) addOneExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        var unit = ast.getFactory().CompilationUnit().getOrCreate(ast);

        //The normal "toString" has no imports, so the pretty printer is needed
        DefaultJavaPrettyPrinter defaultJavaPrettyPrinter = new DefaultJavaPrettyPrinter(unit.getFactory().getEnvironment());

        var prettyPrintedFile = defaultJavaPrettyPrinter.prettyprint(unit);
        assertTrue(prettyPrintedFile.contains("import java.util.function.Supplier;"));
    }


    @Tag("File")
    @Test
    void applyToAddOneExample_checkImports_ShouldHaveOne(){
        CtClass ast = (CtClass) addOneExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        var unit = ast.getFactory().CompilationUnit().getOrCreate(ast);

        assertEquals(1,unit.getImports().size());
    }

    @Tag("File")
    @Test
    void applyToAddOneExample_applyTwice_checkImports_ShouldHaveOne(){
        CtClass ast = (CtClass) addOneExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        transformer.applyAtRandom(ast);
        transformer.applyAtRandom(ast);

        var unit = ast.getFactory().CompilationUnit().getOrCreate(ast);

        assertEquals(1,unit.getImports().size());
    }


    @Tag("File")
    @Test
    void applyToHeyExample_shouldHaveImportInPrettyPrinted(){
        CtClass ast = (CtClass) heyExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        var unit = ast.getFactory().CompilationUnit().getOrCreate(ast);

        //The normal "toString" has no imports, so the pretty printer is needed
        DefaultJavaPrettyPrinter defaultJavaPrettyPrinter = new DefaultJavaPrettyPrinter(unit.getFactory().getEnvironment());

        var prettyPrintedFile = defaultJavaPrettyPrinter.prettyprint(unit);
        assertTrue(prettyPrintedFile.contains("import java.util.function.Supplier;"));
    }


    @Tag("File")
    @Test
    void applyToExistingImportsExample_shouldHaveSupplierImportInPrettyPrinted(){
        CtClass ast = (CtClass) existingImportExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        var unit = ast.getFactory().CompilationUnit().getOrCreate(ast);

        //The normal "toString" has no imports, so the pretty printer is needed
        DefaultJavaPrettyPrinter defaultJavaPrettyPrinter = new DefaultJavaPrettyPrinter(unit.getFactory().getEnvironment());

        var prettyPrintedFile = defaultJavaPrettyPrinter.prettyprint(unit);
        assertTrue(prettyPrintedFile.contains("import java.util.function.Supplier;"));
    }

    @Tag("File")
    @Test
    void applyToExistingImportsExample_shouldHaveOldImportInPrettyPrinted(){
        CtClass ast = (CtClass) existingImportExample();

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();

        TransformationResult result = transformer.applyAtRandom(ast);

        var unit = ast.getFactory().CompilationUnit().getOrCreate(ast);

        //The normal "toString" has no imports, so the pretty printer is needed
        DefaultJavaPrettyPrinter defaultJavaPrettyPrinter = new DefaultJavaPrettyPrinter(unit.getFactory().getEnvironment());

        var prettyPrintedFile = defaultJavaPrettyPrinter.prettyprint(unit);
        assertTrue(prettyPrintedFile.contains("import java.util.Hashset;"));
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
                        System.out.println("Hello Abstract World");
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToAbstractClassWithConcreteMethod_shouldBeApplied(){
        String raw = """
                public abstract class example_abstract_class {
                    public void normal_method(){
                        int b = 1;
                        System.out.println("Hello World");
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertNotEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @RepeatedTest(3)
    public void applyToAbstractClassWithConcreteAndAbstractMethod_shouldBeApplied(){
        String raw = """
                public abstract class example_abstract_class {
                    public void normal_method(){
                        int a = 5;
                        System.out.println("Hello World");
                    }
                    
                    public abstract void abstract_method() {
                        int b = 5;
                        System.out.println("Hello Abstract World");
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();
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

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();
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

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToInterfaceWithDefaultMethod_shouldNotBeApplied(){
        String raw = """
                public interface example_interface {
                    
                    public default int doSomething(){
                        int b = 5;
                        return 5;
                    }
                    
                }
                """;

        var launcher = new Launcher();
        CtElement testObject = launcher.getFactory().createCodeSnippetStatement(raw);

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();
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

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();
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

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();
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

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();
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

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();
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

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToSynchronizedMethod_shouldBeApplied(){
        String raw = """
                public class example_class {
                    public synchronized void someMethod() {
                        int b = 5;
                        System.out.println("Hello Synchronized World");
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();
        TransformationResult result = transformer.applyAtRandom(testObject);

        assertNotEquals(new EmptyTransformationResult(),result);
    }

    @Tag("Regression")
    @Test
    public void applyToSynchronizedStaticMethod_shouldBeApplied(){
        String raw = """
                public class example_class {
                    public static synchronized void someMethod() {
                        int b = 5;
                        System.out.println("Hello Synchronized World");
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();
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
                            int b = 5;
                            System.out.println("Hello Synchronized World");
                        }
                    }
                }
                """;
        CtElement testObject = Launcher.parseClass(raw);

        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer();
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
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer(2022);

        assertEquals(transformer,transformer);
    }

    @Test
    void testEquals_TwoTransformers_differentSeeds_areNotEquals(){
        LambdaIdentityTransformer t1 = new LambdaIdentityTransformer(1);
        LambdaIdentityTransformer t2 = new LambdaIdentityTransformer(2);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoTransformers_differentSeeds_seedsAreChangedAfterCreation_areNotEquals(){
        LambdaIdentityTransformer t1 = new LambdaIdentityTransformer(1);
        LambdaIdentityTransformer t2 = new LambdaIdentityTransformer(1);
        t2.setSeed(2);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoTransformers_differentTryingToCompile_areNotEquals(){
        LambdaIdentityTransformer t1 = new LambdaIdentityTransformer(1);
        t1.setTryingToCompile(false);
        LambdaIdentityTransformer t2 = new LambdaIdentityTransformer(1);
        t2.setTryingToCompile(true);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoTransformers_differentAutoImports_areNotEquals(){
        LambdaIdentityTransformer t1 = new LambdaIdentityTransformer(1);
        t1.setSetsAutoImports(false);
        LambdaIdentityTransformer t2 = new LambdaIdentityTransformer(1);
        t2.setSetsAutoImports(true);

        assertNotEquals(t1,t2);
    }

    @Test
    void testEquals_TwoFreshTransformers_areEqual(){
        LambdaIdentityTransformer t1 = new LambdaIdentityTransformer(5);
        LambdaIdentityTransformer t2 = new LambdaIdentityTransformer(5);

        assertEquals(t1,t2);
    }

    @Test
    void testHashCode_FreshTransformer_isNotNull(){
        LambdaIdentityTransformer transformer = new LambdaIdentityTransformer(10);

        int result = transformer.hashCode();

        assertNotNull(result);
        assertNotEquals(0,result);
    }

    @Test
    void testHashCode_TransformerWithSameSeeds_haveSameHashCode(){
        LambdaIdentityTransformer t1 = new LambdaIdentityTransformer(1);
        LambdaIdentityTransformer t2 = new LambdaIdentityTransformer(1);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithDifferentSeeds_haveDifferentHashCode(){
        LambdaIdentityTransformer t1 = new LambdaIdentityTransformer(1);
        LambdaIdentityTransformer t2 = new LambdaIdentityTransformer(2);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertNotEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithTryingToCompile_haveDifferentHashCode(){
        LambdaIdentityTransformer t1 = new LambdaIdentityTransformer(5);
        t1.setTryingToCompile(true);
        LambdaIdentityTransformer t2 = new LambdaIdentityTransformer(5);
        t2.setTryingToCompile(false);

        int r1 = t1.hashCode();
        int r2 = t2.hashCode();

        assertNotEquals(r1,r2);
    }

    @Test
    void testHashCode_TransformerWithSetsAutoImports_haveDifferentHashCode(){
        LambdaIdentityTransformer t1 = new LambdaIdentityTransformer(5);
        t1.setSetsAutoImports(true);
        LambdaIdentityTransformer t2 = new LambdaIdentityTransformer(5);
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

    static CtElement classWithoutLiteral(){
        String pathToTestFile = "./src/test/resources/javafiles/javafiles_for_lambda_identity_tests/NoLiteralExample.java";

        Launcher launcher = new Launcher();
        launcher.addInputResource(pathToTestFile);
        launcher.buildModel();

        CtClass testObject = launcher.getModel().filterChildren(t -> t instanceof CtClass).first();

        return testObject;
    }

    static CtElement heyExample(){
        String pathToTestFile = "./src/test/resources/javafiles/javafiles_for_lambda_identity_tests/HeyExample.java";

        Launcher launcher = new Launcher();
        launcher.addInputResource(pathToTestFile);
        launcher.buildModel();

        CtClass testObject = launcher.getModel().filterChildren(t -> t instanceof CtClass).first();

        return testObject;
    }

    static CtElement bPlusHeyExample(){
        String pathToTestFile = "./src/test/resources/javafiles/javafiles_for_lambda_identity_tests/BPlusHeyExample.java";

        Launcher launcher = new Launcher();
        launcher.addInputResource(pathToTestFile);
        launcher.buildModel();

        CtClass testObject = launcher.getModel().filterChildren(t -> t instanceof CtClass).first();

        return testObject;
    }

    static CtElement addOneExample(){
        String pathToTestFile = "./src/test/resources/javafiles/javafiles_for_lambda_identity_tests/AddOneExample.java";

        Launcher launcher = new Launcher();
        launcher.addInputResource(pathToTestFile);
        launcher.buildModel();

        CtClass testObject = launcher.getModel().filterChildren(t -> t instanceof CtClass).first();

        return testObject;
    }

    static CtElement existingImportExample(){
        String pathToTestFile = "./src/test/resources/javafiles/javafiles_for_lambda_identity_tests/ExistingImportExample.java";

        Launcher launcher = new Launcher();
        launcher.addInputResource(pathToTestFile);
        launcher.buildModel();

        CtClass testObject = launcher.getModel().filterChildren(t -> t instanceof CtClass).first();

        return testObject;
    }

}
