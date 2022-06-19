package com.github.ciselab.lampion.core.regression;

import com.github.ciselab.lampion.core.program.Engine;
import com.github.ciselab.lampion.core.program.EngineResult;
import com.github.ciselab.lampion.core.transformations.TransformerRegistry;
import com.github.ciselab.lampion.core.transformations.transformers.*;
import org.junit.jupiter.api.*;
import spoon.Launcher;
import spoon.reflect.CtModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EngineEdgeCases {
    /**
     * This Test-Class covers Edge Cases found during production use.
     * Most of it is documented in Issues 91 and 109.
     * They were individually assessed in the respective transformers,
     * but in this class we check specifically that the engine does not break.
     */

    private final static String filepathPrefix = "./src/test/resources/javafiles/unsupported_corner_cases";
    private final static String outputTestFolder = "./src/test/resources/corner_cases/engine_spooned/";

    @BeforeAll
    @AfterAll
    private static void folderCleanup() throws IOException {
        if(Files.exists(Paths.get(outputTestFolder))) {
            Files.walk(Paths.get(outputTestFolder))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Tag("Slow")
    @Tag("System")
    @Tag("Regression")
    @Tag("File")
    @RepeatedTest(3)
    public void testEngine_RunOnAbstractMethod_DoesNotThrowException(){
        String pathToTestFile = filepathPrefix + "/abstract_class.java";

        Engine testObject = new Engine(pathToTestFile,outputTestFolder,defaultRegistry());

        testObject.setWriteJavaOutput(false);
        testObject.setNumberOfTransformationsPerScope(50, Engine.TransformationScope.perMethod);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);

        testObject.run(codeRoot);

        return;
    }

    @Tag("Slow")
    @Tag("System")
    @Tag("Regression")
    @Tag("File")
    @RepeatedTest(3)
    public void testEngine_RunOnEnum_DoesNotThrowException(){
        String pathToTestFile = filepathPrefix + "/example_enum.java";

        Engine testObject = new Engine(pathToTestFile,outputTestFolder,defaultRegistry());

        testObject.setWriteJavaOutput(false);
        testObject.setNumberOfTransformationsPerScope(50, Engine.TransformationScope.perMethod);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);

        testObject.run(codeRoot);

        return;
    }

    @Tag("Slow")
    @Tag("System")
    @Tag("Regression")
    @Tag("File")
    @RepeatedTest(3)
    public void testEngine_RunOnInterface_DoesNotThrowException(){
        String pathToTestFile = filepathPrefix + "/example_interface.java";

        Engine testObject = new Engine(pathToTestFile,outputTestFolder,defaultRegistry());

        testObject.setWriteJavaOutput(false);
        testObject.setNumberOfTransformationsPerScope(50, Engine.TransformationScope.perMethod);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);

        testObject.run(codeRoot);
        return;
    }

    @Tag("Slow")
    @Tag("System")
    @Tag("Regression")
    @Tag("File")
    @RepeatedTest(3)
    public void testEngine_RunOnInnerClass_DoesNotThrowException(){
        String pathToTestFile = filepathPrefix + "/inner_class.java";

        Engine testObject = new Engine(pathToTestFile,outputTestFolder,defaultRegistry());

        testObject.setWriteJavaOutput(false);
        testObject.setNumberOfTransformationsPerScope(50, Engine.TransformationScope.perMethod);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);

        testObject.run(codeRoot);
        return;
    }

    @Tag("Slow")
    @Tag("System")
    @Tag("Regression")
    @Tag("File")
    @RepeatedTest(5)
    public void testEngine_RunOnEdgeCaseFolder_DoesNotThrowException(){
        String pathToTestFile = filepathPrefix;

        Engine testObject = new Engine(pathToTestFile,outputTestFolder,defaultRegistry());

        testObject.setWriteJavaOutput(false);
        testObject.setNumberOfTransformationsPerScope(100, Engine.TransformationScope.perMethod);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);

        testObject.run(codeRoot);
        return;
    }
    /*
    =============================================
                    Helpers
    =============================================
     */

    public TransformerRegistry defaultRegistry(){
        TransformerRegistry registry = new TransformerRegistry("RegressionTest");
        registry.registerTransformer(new IfFalseElseTransformer());
        registry.registerTransformer(new IfTrueTransformer());
        registry.registerTransformer(new LambdaIdentityTransformer());
        registry.registerTransformer(new AddNeutralElementTransformer());
        registry.registerTransformer(new RenameVariableTransformer());
        registry.registerTransformer(new RandomParameterNameTransformer());
        registry.registerTransformer(new AddUnusedVariableTransformer());
        registry.registerTransformer(new EmptyMethodTransformer());

        return registry;
    }
}
