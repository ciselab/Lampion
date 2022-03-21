package com.github.ciselab.lampion.core.program;

import com.github.ciselab.lampion.core.program.EngineResult;
import com.github.ciselab.lampion.core.transformations.TransformationResult;
import com.github.ciselab.lampion.core.transformations.Transformer;
import com.github.ciselab.lampion.core.transformations.TransformerRegistry;
import com.github.ciselab.lampion.core.transformations.transformers.*;
import org.junit.jupiter.api.*;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class EngineTests {

    private static String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_simple";
    private static String outputTestFolder = "./src/test/resources/engine_spooned/";
    // Note: The file matching is case sensitive on some systems, be careful!
    private static String expectedJavaFile = "./lampion/test/examples/Example.java";

    @BeforeAll
    @AfterAll
    private static void folder_cleanup() throws IOException {
        if(Files.exists(Paths.get(outputTestFolder))) {
            Files.walk(Paths.get(outputTestFolder))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @BeforeEach
    private void createOutputFolderIfNotExists() throws IOException {
        if(!Files.exists(Paths.get(outputTestFolder))){
            Files.createDirectory(Paths.get(outputTestFolder));
        }
    }

    @Test
    void testSetDistribution_DistributionHasUknownElements_ShouldThrowException(){
        Transformer in = new IfTrueTransformer();

        Transformer notIn = new RandomParameterNameTransformer();

        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(in);

        Map<Transformer,Integer> faultyDistribution = new HashMap<>();
        faultyDistribution.put(notIn,5);

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder+"_exploration",registry);

        assertThrows(UnsupportedOperationException.class, () -> testObject.setDistribution(faultyDistribution));
    }

    @Test
    void testSetDistribution_DistributionHasNegativeValues_ShouldThrowException(){
        Transformer in = new IfTrueTransformer();

        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(in);

        Map<Transformer,Integer> faultyDistribution = new HashMap<>();
        faultyDistribution.put(in,-2);

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        assertThrows(UnsupportedOperationException.class, () -> testObject.setDistribution(faultyDistribution));
    }

    @Test
    void testSetDistribution_DistributionIsCorrect_shouldBeSet(){
        Transformer in = new IfTrueTransformer();
        Transformer inToo = new RandomParameterNameTransformer();

        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(in);
        registry.registerTransformer(inToo);

        Map<Transformer,Integer> dist = new HashMap<>();
        dist.put(in,5);

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setDistribution(dist);
        return;
    }

    @Test
    void testSetTransformationScope_negativeNumberOfTransformations_shouldThrowException(){
        TransformerRegistry registry = new TransformerRegistry("Test");

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        assertThrows(UnsupportedOperationException.class,
                () -> testObject.setNumberOfTransformationsPerScope(-1, Engine.TransformationScope.global));
    }

    @Test
    void testSetTransformationScope_allGood_shouldBeSet(){
        TransformerRegistry registry = new TransformerRegistry("Test");

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setNumberOfTransformationsPerScope(5, Engine.TransformationScope.global);
        return;
    }

    @Test
    void testGetFinishedResults_ZeroTransformations_ShouldGiveEmptyList() {
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_perMethodEach";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setNumberOfTransformationsPerScope(0, Engine.TransformationScope.perClassEach);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        assertTrue(result.getTransformationResults().isEmpty());
    }

    @Test
    void testGetFinishedResults_RunEngine_ShouldNotGiveEmptyList(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_perMethodEach";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setWriteJavaOutput(false);

        testObject.setNumberOfTransformationsPerScope(5, Engine.TransformationScope.perClassEach);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        assertFalse(result.getTransformationResults().isEmpty());
    }

    @Tag("System")
    @Tag("File")
    @RepeatedTest(3)
    void testPerClassEachScope_ShouldApplyEvenlyToMethods(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_perMethodEach";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setWriteJavaOutput(false);

        testObject.setNumberOfTransformationsPerScope(5, Engine.TransformationScope.perClassEach);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        // Easy Check on Size
        assertEquals(10,result.getTransformationResults().size());

        // Distribution Checks
        result.getTransformationResults().stream()
                .collect(Collectors.groupingBy(t -> ((CtClass)t.getTransformedElement().getParent(p -> p instanceof CtClass)).getSimpleName()))
                .values().stream().mapToLong(u -> u.size())
                .forEach(f -> assertEquals(5,f));
        assertEquals(2,result.getTransformationResults().stream()
                .collect(Collectors.groupingBy(t -> ((CtClass)t.getTransformedElement().getParent(p -> p instanceof CtClass)).getSimpleName())).entrySet().size());
    }


    @Tag("System")
    @Tag("File")
    @RepeatedTest(3)
    void testPerClassScope_ShouldApplyEvenlyToMethods(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_perMethodEach";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setWriteJavaOutput(false);

        testObject.setNumberOfTransformationsPerScope(5, Engine.TransformationScope.perClass);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        // Easy Check on Size
        assertEquals(10,result.getTransformationResults().size());
    }

    @Tag("System")
    @Tag("File")
    @RepeatedTest(3)
    void testPerMethodEachScope_ShouldApplyEvenlyToMethods(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_perMethodEach";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setWriteJavaOutput(false);

        testObject.setNumberOfTransformationsPerScope(3, Engine.TransformationScope.perMethodEach);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        // Easy Check on Size
        assertEquals(12,result.getTransformationResults().size());
        // Distribution Checks
        result.getTransformationResults().stream()
                .collect(Collectors.groupingBy(t -> ((CtMethod)t.getTransformedElement()).getSimpleName()))
                .values().stream().mapToLong(u -> u.size())
                .forEach(f -> assertEquals(3,f));
        assertEquals(4,result.getTransformationResults().stream()
                .collect(Collectors.groupingBy(t -> ((CtMethod)t.getTransformedElement()).getSimpleName())).entrySet().size());
    }


    @Tag("System")
    @Tag("File")
    @RepeatedTest(3)
    void testPerMethodScope_ShouldApplyMultipleOfMethods(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_perMethodEach";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setWriteJavaOutput(false);

        testObject.setNumberOfTransformationsPerScope(3, Engine.TransformationScope.perMethod);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        // Easy Check on Size
        assertEquals(12,result.getTransformationResults().size());
    }

    @Tag("System")
    @Tag("File")
    @Test
    void testEngineRun_withDeleteComments_ShouldDeleteJavaDocs(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_with_comments";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        // Do just little transformations to be faster
        testObject.setNumberOfTransformationsPerScope(3, Engine.TransformationScope.perClassEach);

        testObject.setRemoveAllComments(true);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        result.getTransformationResults().forEach(
                p -> assertFalse(p.getTransformedElement().toString().contains("Comment") ||p.getTransformedElement().toString().contains("JavaDoc"))
        );
    }

    @Tag("System")
    @Tag("File")
    @Test
    void testEngineRun_withDeleteComments_ShouldDeleteInlineComments(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_with_comments";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        // Do just little transformations to be faster
        testObject.setNumberOfTransformationsPerScope(3, Engine.TransformationScope.perClassEach);

        testObject.setRemoveAllComments(true);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        result.getTransformationResults().forEach(
                p -> assertFalse(p.getTransformedElement().toString().contains("Comment") ||p.getTransformedElement().toString().contains("JavaDoc"))
        );
    }

    @Tag("System")
    @Tag("File")
    @Test
    void testEngineRun_withDeleteComments_ShouldDeleteBlockComments(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_with_comments";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);
        // Do just little transformations to be faster
        testObject.setNumberOfTransformationsPerScope(3, Engine.TransformationScope.perClassEach);

        testObject.setRemoveAllComments(true);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        for(TransformationResult p : result.getTransformationResults()){
            assertFalse(p.getTransformedElement().toString().contains("Comment") ||p.getTransformedElement().toString().contains("JavaDoc"));
        }
    }

    @Tag("System")
    @Tag("File")
    @Test
    void testEngineRun_withDeleteComments_SingleTrans_ShouldDeleteBlockComments(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_with_comments";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        // Do just little transformations to be faster
        testObject.setNumberOfTransformationsPerScope(1, Engine.TransformationScope.global);

        testObject.setRemoveAllComments(true);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        for(TransformationResult p : result.getTransformationResults()){
            assertFalse(p.getTransformedElement().toString().contains("Comment") ||p.getTransformedElement().toString().contains("JavaDoc"));
        }
    }

    @Tag("System")
    @Tag("File")
    @Tag("Regression")
    @Test
    void testRemoveComments_doNotRemoveComments_commentsShouldBeKept(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_with_comments";
        TransformerRegistry registry = new TransformerRegistry("Test");

        Transformer ifFalse = new IfFalseElseTransformer(2);
        registry.registerTransformer(ifFalse);

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        // Do just little transformations to be faster
        testObject.setNumberOfTransformationsPerScope(4, Engine.TransformationScope.global);

        testObject.setRemoveAllComments(false);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        for(TransformationResult p : result.getTransformationResults()){
            assertTrue(p.getTransformedElement().toString().contains("Comment") ||p.getTransformedElement().toString().contains("JavaDoc"));
        }
    }

    @Tag("System")
    @Tag("File")
    @Test
    void testEngineRun_withDeleteComments_ZeroTrans_ShouldDeleteBlockComments(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_with_comments";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        // Do just little transformations to be faster
        testObject.setNumberOfTransformationsPerScope(0, Engine.TransformationScope.global);

        testObject.setRemoveAllComments(true);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        for(TransformationResult p : result.getTransformationResults()){
            assertFalse(p.getTransformedElement().toString().contains("Comment"));
        }
    }

    @Tag("System")
    @Tag("File")
    @Tag("Regression")
    @Test
    void testEngineRun_withDeleteComments_ShouldHaveRemovalInManifest(){
        // At the beginning, the comment-removal-transformations were outside of the manifest logic
        // After 1.2 they are run separate (after transformations) but also added to the manifest
        // They were often / usually run, but they threw an error while somewhat still working

        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_with_comments";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setNumberOfTransformationsPerScope(5, Engine.TransformationScope.global);

        testObject.setRemoveAllComments(true);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        boolean haveSeenRemoveComments = false;
        for(TransformationResult p : result.getTransformationResults()){
            haveSeenRemoveComments = haveSeenRemoveComments || p.getTransformationName().equals("RemoveAllComments");
        }
        assertTrue(haveSeenRemoveComments);
    }


    @Tag("System")
    @Tag("File")
    @Tag("Regression")
    @Test
    void testEngineRun_withDeleteComments_noTransformations_ShouldHaveRemovalInManifest(){
        // At the beginning, the comment-removal-transformations were outside of the manifest logic
        // After 1.2 they are run separate (after transformations) but also added to the manifest
        // They were often / usually run, but they threw an error while somewhat still working

        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_with_comments";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setNumberOfTransformationsPerScope(0, Engine.TransformationScope.global);

        testObject.setRemoveAllComments(true);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        boolean haveSeenRemoveComments = false;
        for(TransformationResult p : result.getTransformationResults()){
            haveSeenRemoveComments = haveSeenRemoveComments || p.getTransformationName().equals("RemoveAllComments");
        }
        assertTrue(haveSeenRemoveComments);
    }

    @Tag("System")
    @Tag("File")
    @Test
    void testEngineRun_withoutDeleteComments_ShouldHaveNoRemovalInManifest(){
        // At the beginning, the comment-removal-transformations were outside of the manifest logic
        // After 1.2 they are run separate (after transformations) but also added to the manifest
        // This is the negative test, checking that without removal they are not in the manifest

        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_with_comments";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setNumberOfTransformationsPerScope(5, Engine.TransformationScope.global);

        testObject.setRemoveAllComments(false);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        boolean haveSeenRemoveComments = false;
        for(TransformationResult p : result.getTransformationResults()){
            haveSeenRemoveComments = haveSeenRemoveComments || p.getTransformationName().equals("RemoveAllComments");
        }
        assertFalse(haveSeenRemoveComments);
    }

    @Test
    void testConstructor_NullCodeDirectory_shouldThrowException(){
        TransformerRegistry registry = new TransformerRegistry("Test");

        assertThrows(UnsupportedOperationException.class, () ->  new Engine(null,outputTestFolder,registry));
    }

    @Test
    void testConstructor_EmptyCodeDirectory_shouldThrowException(){
        TransformerRegistry registry = new TransformerRegistry("Test");

        assertThrows(UnsupportedOperationException.class, () ->  new Engine("",outputTestFolder,registry));
    }

    @Test
    void testConstructor_BlankCodeDirectory_shouldThrowException(){
        TransformerRegistry registry = new TransformerRegistry("Test");

        assertThrows(UnsupportedOperationException.class, () ->  new Engine("  \n",outputTestFolder,registry));
    }

    @Test
    void testConstructor_NullOutDirectory_shouldThrowException(){
        TransformerRegistry registry = new TransformerRegistry("Test");

        assertThrows(UnsupportedOperationException.class, () ->  new Engine(pathToTestFileFolder,null,registry));
    }

    @Test
    void testConstructor_EmptyOutDirectory_shouldThrowException(){
        TransformerRegistry registry = new TransformerRegistry("Test");

        assertThrows(UnsupportedOperationException.class, () ->  new Engine(pathToTestFileFolder,"",registry));
    }

    @Test
    void testConstructor_BlankOutDirectory_shouldThrowException(){
        TransformerRegistry registry = new TransformerRegistry("Test");

        assertThrows(UnsupportedOperationException.class, () ->  new Engine(pathToTestFileFolder,"  \n",registry));
    }

    @Test
    void testSetSeed_ChangesSeed(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_with_comments";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setRandomSeed(250);
    }

    @Test
    void testConstructor_NullRegistry_shouldThrowException(){
        assertThrows(UnsupportedOperationException.class, () ->  new Engine(pathToTestFileFolder,outputTestFolder,null));
    }

}
