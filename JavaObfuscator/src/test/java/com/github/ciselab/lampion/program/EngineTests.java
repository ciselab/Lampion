package com.github.ciselab.lampion.program;

import com.github.ciselab.lampion.transformations.TransformerRegistry;
import com.github.ciselab.lampion.transformations.transformers.IfTrueTransformer;
import com.github.ciselab.lampion.transformations.transformers.RandomInlineCommentTransformer;
import com.github.ciselab.lampion.transformations.transformers.RandomParameterNameTransformer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class EngineTests {

    private static String pathToTestFileFolder = "./src/test/resources/javafiles";
    private static String outputTestFolder = "./src/test/resources/engine_spooned";

    @Tag("System")
    @Tag("File")
    @Test
    void testRun_shouldWork() throws IOException {
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

        testObject.run();

        assertTrue(Files.exists(Path.of(outputTestFolder+"_exploration","example.java")));
    }
}
