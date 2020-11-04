package com.github.ciselab.lampion.program;

import com.github.ciselab.lampion.manifest.MockWriter;
import com.github.ciselab.lampion.transformations.Transformer;
import com.github.ciselab.lampion.transformations.TransformerRegistry;
import com.github.ciselab.lampion.transformations.transformers.IfTrueTransformer;
import com.github.ciselab.lampion.transformations.transformers.RandomInlineCommentTransformer;
import com.github.ciselab.lampion.transformations.transformers.RandomParameterNameTransformer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class EngineTests {

    private static String pathToTestFileFolder = "./src/test/resources/javafiles";
    private static String outputTestFolder = "./src/test/resources/engine_spooned";

    @Tag("System")
    @Tag("File")
    @Test
    void testRun_shouldCreateFile() throws IOException {
        //Short check whether there was proper cleanup
        assertFalse(Files.exists(Path.of(outputTestFolder,"example.java")));

        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfTrueTransformer());
        registry.registerTransformer(new RandomInlineCommentTransformer());
        registry.registerTransformer(new RandomParameterNameTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setNumberOfTransformationsPerScope(10, Engine.TransformationScope.global);

        testObject.run();

        assertTrue(Files.exists(Path.of(outputTestFolder,"example.java")));

        // CleanUp
        Files.delete(Path.of(outputTestFolder,"example.java"));
        Files.delete(Path.of(outputTestFolder));
    }

    @Tag("System")
    @Tag("File")
    @Test
    void testRun_shouldBeAlteredAST() throws IOException {
        //Short check whether there was proper cleanup
        assertFalse(Files.exists(Path.of(outputTestFolder,"example.java")));

        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfTrueTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setNumberOfTransformationsPerScope(5, Engine.TransformationScope.global);

        testObject.run();

        String file = Files.readString(Path.of(outputTestFolder,"example.java"));
        file.contains("if (true)");

        // CleanUp
        Files.delete(Path.of(outputTestFolder,"example.java"));
        Files.delete(Path.of(outputTestFolder));
    }

    @Tag("System")
    @Tag("File")
    @Test
    void testRun_WithMockManifest_MockManifestIsTouchedAndHasResults() throws IOException {
        //Short check whether there was proper cleanup
        assertFalse(Files.exists(Path.of(outputTestFolder,"example.java")));

        int transformations = 5;

        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfTrueTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setNumberOfTransformationsPerScope(transformations, Engine.TransformationScope.global);

        MockWriter mock = new MockWriter();
        testObject.setManifestWriter(mock);

        testObject.run();

        assertTrue(mock.wasTouched);
        assertEquals(transformations,mock.receivedResults.size());

        // CleanUp
        Files.delete(Path.of(outputTestFolder,"example.java"));
        Files.delete(Path.of(outputTestFolder));
    }

    @Tag("System")
    @Tag("File")
    @Test
    void testRun_WithMockManifest_ManifestSetAfterRun_MockManifestIsNotTouched() throws IOException {
        //Short check whether there was proper cleanup
        assertFalse(Files.exists(Path.of(outputTestFolder,"example.java")));

        int transformations = 5;

        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfTrueTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setNumberOfTransformationsPerScope(transformations, Engine.TransformationScope.global);

        testObject.run();

        MockWriter mock = new MockWriter();
        testObject.setManifestWriter(mock);

        assertFalse(mock.wasTouched);

        // CleanUp
        Files.delete(Path.of(outputTestFolder,"example.java"));
        Files.delete(Path.of(outputTestFolder));
    }

    @Tag("System")
    @Tag("File")
    @Tag("Exploration")
    @Test
    void testRun_shouldWork_noCleanup_separateFolder() throws IOException {
        //Short check whether there was proper cleanup
        assertFalse(Files.exists(Path.of(outputTestFolder+"_exploration","example.java")));

        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfTrueTransformer());
        registry.registerTransformer(new RandomInlineCommentTransformer());
        registry.registerTransformer(new RandomParameterNameTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder+"_exploration",registry);

        testObject.setNumberOfTransformationsPerScope(10, Engine.TransformationScope.global);

        MockWriter mock = new MockWriter();
        testObject.setManifestWriter(mock);

        testObject.run();

        assertTrue(Files.exists(Path.of(outputTestFolder+"_exploration","example.java")));
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
    void testSetWriter_nullWriter_shouldThrowException(){
        TransformerRegistry registry = new TransformerRegistry("Test");

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        assertThrows(UnsupportedOperationException.class, () -> testObject.setManifestWriter(null));
    }

    @Test
    void testSetWriter_MockWriter_shouldWork(){
        TransformerRegistry registry = new TransformerRegistry("Test");

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        MockWriter mock = new MockWriter();
        testObject.setManifestWriter(mock);

        return;
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
    void testConstructor_NullRegistry_shouldThrowException(){
        assertThrows(UnsupportedOperationException.class, () ->  new Engine(pathToTestFileFolder,outputTestFolder,null));
    }

}
