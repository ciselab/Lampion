package com.github.ciselab.lapion.cli.program;

import static com.github.ciselab.lampion.cli.program.App.WriteAST;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.ciselab.lampion.cli.program.App;
import com.github.ciselab.lampion.core.program.Engine;
import com.github.ciselab.lampion.core.program.EngineResult;
import com.github.ciselab.lampion.core.transformations.TransformerRegistry;
import com.github.ciselab.lampion.core.transformations.transformers.IfTrueTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.RandomInlineCommentTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.RandomParameterNameTransformer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.reflect.CtModel;

public class AppTest {

    private static String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_simple";
    private static String outputTestFolder = "./src/test/resources/engine_spooned/";
    // Note: The file matching is case sensitive on some systems, be careful!
    private static String expectedJavaFile = "./lampion/test/examples/example.java";

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

    // TODO: Fill me again !

    /*
    @Test
    public void runApp_noArgs_shouldNotBreak(){
        App.main(new String[]{});

        assertTrue(true);
    }
    */

    @Tag("Regression")
    @Test
    void testDefaultRegistry_ShouldNotBeEmpty(){
        /*
        This issue occurred due to an issue with the Java runtime.
        The transformers register their delegate at "static"-time, that is when the class is loaded.
        However, the class is only loaded when needed.
        Without further measures taken, this leads to an empty registry at runtime.

        For this issue, this simple test is added and should always succeed while relying on the default registry!
         */
        Assertions.assertFalse(App.globalRegistry.getRegisteredTransformers().isEmpty());
    }

    @Tag("System")
    @Tag("File")
    @Test
    void testRun_shouldCreateFile() throws IOException {
        //Short check whether there was proper cleanup
        assertFalse(Files.exists(Path.of(outputTestFolder,expectedJavaFile)));

        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfTrueTransformer());
        registry.registerTransformer(new RandomInlineCommentTransformer());
        registry.registerTransformer(new RandomParameterNameTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setNumberOfTransformationsPerScope(10, Engine.TransformationScope.global);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);
        WriteAST(result, launcher);

        assertTrue(Files.exists(Path.of(outputTestFolder,expectedJavaFile)));

        // CleanUp
        Files.delete(Path.of(outputTestFolder,expectedJavaFile));
    }

    @Tag("System")
    @Tag("File")
    @Test
    void testRun_WritingSetFalse_shouldNotCreateFile() throws IOException {
        //Short check whether there was proper cleanup
        assertFalse(Files.exists(Path.of(outputTestFolder,expectedJavaFile)));

        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfTrueTransformer());
        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);
        testObject.setNumberOfTransformationsPerScope(10, Engine.TransformationScope.global);

        testObject.setWriteJavaOutput(false);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);
        WriteAST(result, launcher);

        assertFalse(Files.exists(Path.of(outputTestFolder,expectedJavaFile)));
    }

    @Tag("System")
    @Tag("File")
    @Test
    void testRun_shouldBeAlteredAST() throws IOException {
        //Short check whether there was proper cleanup
        assertFalse(Files.exists(Path.of(outputTestFolder,expectedJavaFile)));

        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfTrueTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setNumberOfTransformationsPerScope(5, Engine.TransformationScope.global);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);
        WriteAST(result, launcher);

        String file = Files.readString(Path.of(outputTestFolder,expectedJavaFile));
        file.contains("if (true)");

        // CleanUp
        Files.delete(Path.of(outputTestFolder,expectedJavaFile));
    }

    @Tag("System")
    @Tag("File")
    @Test
    void testRun_WithNonCompilingTransformer_worksNormally() throws IOException {
        //Short check whether there was proper cleanup
        assertFalse(Files.exists(Path.of(outputTestFolder,expectedJavaFile)));

        int transformations = 1;

        var transformer = new IfTrueTransformer();
        transformer.setTryingToCompile(false);

        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(transformer);

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setNumberOfTransformationsPerScope(transformations, Engine.TransformationScope.global);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);
        WriteAST(result, launcher);

        // CleanUp

        Files.delete(Path.of(outputTestFolder,expectedJavaFile));
    }

    @Tag("System")
    @Tag("File")
    @Test
    void testRun_WithNonCompilingTransformer_OnFileWithMissingMethodReferences_works() throws IOException {
        String pathToTestFileFolder = "./src/test/resources/javafiles/bad_javafiles";
        String outputTestFolder = "./src/test/resources/bad_javafiles_output/";

        int transformations = 2;

        var transformer = new IfTrueTransformer();
        transformer.setTryingToCompile(false);

        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(transformer);

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setNumberOfTransformationsPerScope(transformations, Engine.TransformationScope.global);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);
        WriteAST(result, launcher);

        assertTrue(Files.exists(Paths.get("./src/test/resources/bad_javafiles_output/lampion/tests/examples/Misser.java")));

        // Cleanup
        if(Files.exists(Paths.get("./src/test/resources/bad_javafiles_output"))) {
            Files.walk(Paths.get("./src/test/resources/bad_javafiles_output"))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

}
