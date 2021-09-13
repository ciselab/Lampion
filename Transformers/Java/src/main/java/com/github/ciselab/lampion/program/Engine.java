package com.github.ciselab.lampion.program;

import com.github.ciselab.lampion.manifest.ManifestWriter;
import com.github.ciselab.lampion.transformations.*;
import com.github.ciselab.lampion.transformations.transformers.RemoveAllCommentsTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * This class runs the primary parts of the Program.
 * It is purposefully separated from the app, to be better testable.
 *
 * It takes a registry equipped with all relevant
 * Transformers, and applies them quantified in a certain configuration to a given AST.
 * In the end, if wished the TransformationResults are written to an SQL database
 * and the altered programs are written to files.
 *
 * The default behaviour is to apply all available transformations evenly distributed.
 * If others are wanted, a distribution transformation is required, see "setDistribution".
 *
 * The primary method is "run" and has similar comments laying out what's happening.
 */
public class Engine {
    private static Logger logger = LogManager.getLogger(Engine.class);

    String codeDirectory;
    String outputDirectory;
    TransformerRegistry registry;
    Optional<ManifestWriter> writer = Optional.empty();

    Random random = new Random(App.globalRandomSeed);

    // The scope by which to quantify the number of transformations, "setNumberOfTransformationsPerScope" for more info
    public enum TransformationScope {
        global,            // "X Transformations in total, anywhere, evenly distributed throughout all files"
        perMethod,         // "X Transformations per Method found, but evenly distributed throughout all files"
        perClass,          // "X Transformations per Class found, but evenly distributed throughout all files"
        perClassEach,      // "X Transformations per Class"
        perMethodEach      // "X Transformations per Method"
    }
    long numberOfTransformationsPerScope = 100;
    TransformationScope scope = TransformationScope.global;

    // The distribution on how often to apply the Transformers
    // if every transformer has the same value, they are applied evenly often.
    // if e.g. a transformer has 2 and another one has 1, then the 2-transformer is applied twice as much.
    Map<Transformer,Integer> distribution;

    // These are helpers for "perClassEach" and "perMethodEach"
    // To iterate over the classes and methods until there are no more transformations
    private int classIndex = 0;
    private List<CtClass> classes = new ArrayList<>();
    private int methodIndex = 0;
    private List<CtMethod> methods = new ArrayList<>();

    private boolean removeAllComments = false; // Whether or not to remove all comments before printing

    private boolean writeJavaOutput = true; // This switch enables/disables pretty printing of altered java files


    public Engine(String codeDirectory, String outputDirectory, TransformerRegistry registry){
        // Sanity Checks
        if (codeDirectory == null || codeDirectory.isEmpty() || codeDirectory.isBlank()) {
            throw new UnsupportedOperationException("Code Directory cannot be null or empty");
        }
        if (outputDirectory == null || outputDirectory.isEmpty() || outputDirectory.isBlank()) {
            throw new UnsupportedOperationException("Output Directory cannot be null or empty");
        }
        if (registry == null ) {
            throw new UnsupportedOperationException("Registry cannot be null");
        }
        if (registry.getRegisteredTransformers().size() == 0) {
            logger.warn("Received Registry " + registry.name + " without any registered transformers.");
        }
        // Setting fields
        this.codeDirectory = codeDirectory;
        this.outputDirectory = outputDirectory;
        this.registry = registry;
        distribution = new HashMap<>();
        // default behaviour is to apply all transformations equally often
        for (Transformer t: registry.getRegisteredTransformers()) {
            distribution.put(t,1);
        }
    }

    public void run(){
        logger.info("Starting Engine with Registry " + registry.name + "["+registry.getRegisteredTransformers().size()
            + " transformers] reading from " + codeDirectory + " writing to " + outputDirectory);

        Instant startOfEngine = Instant.now();
        // Step 1:
        // Read the Code in
        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(codeDirectory);
        // The CodeRoot is the highest level of available information regarding the AST
        CtModel codeRoot = launcher.buildModel();
        // With the imports set to true, on second application the import will disappear, making Lambdas uncompilable.
        launcher.getFactory().getEnvironment().setAutoImports(false);

        // Note:
        // It is important that methods are instantiated here and not while transformations are running,
        // as maybe there are additional Methods created. This way, only ur-elements will be altered.
        classes = codeRoot.getElements(c -> c instanceof CtClass);
        methods = codeRoot.getElements(c -> c instanceof CtMethod);

        logger.info("Found " + classes.size() + " Classes and "
                + codeRoot.getElements(f -> f instanceof CtMethod).size() + " Methods");
        if(classes.size() == 0 || methods.size() == 0) {
            logger.error("Either found no classes or no methods - exiting early. " +
                    "Check your configuration, whether it points to actual files.");
            return;
        }

        // Step 2:
        // Apply the Transformations according to distribution
        List<TransformationResult> results = new ArrayList<>();
        // Step 2.1:
        // set the total number of transformations regarding the scope
        long totalTransformationsToDo = switch (scope) {
            case global -> numberOfTransformationsPerScope;
            case perMethod -> numberOfTransformationsPerScope * codeRoot.filterChildren(c -> c instanceof CtMethod).list().size();
            case perClass -> numberOfTransformationsPerScope * codeRoot.filterChildren(c -> c instanceof CtClass).list().size();
            case perMethodEach -> numberOfTransformationsPerScope * codeRoot.filterChildren(c -> c instanceof CtMethod).list().size();
            case perClassEach -> numberOfTransformationsPerScope * codeRoot.filterChildren(c -> c instanceof CtClass).list().size();
            default ->  0;
        };
        logger.info("Applying " + totalTransformationsToDo + " Transformations evenly distributed amongst all classes");
        // Step 2.2:
        // For picking transformers, a simple approach was taken to quantify them according to distribution
        // make a new list of transformers, where every transformer is added a number of times their distribution
        // then, pick a random number between 0 and list.size() and this is your transformer!
        List<Transformer> quantifiedTransformers = new ArrayList<>();
        for (var entry : distribution.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                quantifiedTransformers.add(entry.getKey());
            }
        }
        // Step 2.3:
        // For every to-be-applied transformation
        // Pick the next (random) element
        // Pick a random transformer
        // apply the transformer and add the result to the aggregation
        for (long a = 0; a < totalTransformationsToDo; a++) {
            CtElement toAlter = getNextCtElement();

            int index = random.nextInt(quantifiedTransformers.size());
            Transformer transformer = quantifiedTransformers.get(index);

            TransformationResult result = transformer.applyAtRandom(toAlter);
            results.add(result);
        }

        // Step 2.4:
        // Repair parent relationships which may have broken
        // classes.stream().forEach(c -> c.updateAllParentsBelow());

        Instant endOfTransformations = Instant.now();
        logger.info("Applying the Transformations took "
                + Duration.between(startOfEngine,endOfTransformations) + " seconds");
        logger.info("Of the " + results.size() + " Transformations applied, "
                + results.stream().filter(u -> u.equals(new EmptyTransformationResult())).count() + " where malformed");

        // Step 2.5:
        // If enabled, remove all comments (set them invisible)
        if (removeAllComments) {
            RemoveAllCommentsTransformer commentRemover = new RemoveAllCommentsTransformer();
            classes.forEach(c -> commentRemover.applyAtRandom(c));
        }
        classes.forEach(c -> c.updateAllParentsBelow());

        // Step 3:
        // Write Transformed Code
        if (writeJavaOutput) {
            logger.debug("Starting to pretty-print  altered files to " + outputDirectory);
            launcher.setSourceOutputDirectory(outputDirectory);
            launcher.prettyprint();
        } else {
            logger.info("Writing the java files has been disabled for this run.");
        }

        // Step 4:
        // Create Transformation Manifest
        if(writer.isPresent()){
            writer.get().writeManifest(results);
        } else {
            logger.debug("There was no ManifestWriter specified - skipping writing the Manifest.");
        }

        Instant endOfWriting = Instant.now();
        logger.info("Writing files and Manifest took " + Duration.between(endOfTransformations,endOfWriting).getSeconds() + " seconds");
        logger.info("Engine ran successfully");
    }

    /**
     * Looks in the initially found classes/methods for the next specified element according to specified scope.
     * Classes and Methods are returned in a order dependent from structure of the program,
     * but are always in the same order (for multiple iterations).
     *
     * Heavily relies on object-level index counters and methods. If there are issues, look there first.
     * The object-level indices are also handled in this method.
     *
     * @return the next element to alter, according to scope.
     */
    private CtElement getNextCtElement() {
        CtElement toAlter = null;
        switch (scope) {
            // For these, just pick random classes and methods
            case global,perMethod,perClass : toAlter = classes.get(random.nextInt(classes.size())); break;
            case perMethodEach: {
                // Pick next method
                toAlter = methods.get(methodIndex);
                // Move or reset methodIndex
                methodIndex = methodIndex < methods.size()-1 ? methodIndex + 1 : 0;
            } break;
            case perClassEach: {
                // Pick (specific) next class
                toAlter = classes.get(classIndex);
                // Move or reset classIndex
                classIndex = classIndex < classes.size()-1 ? classIndex + 1 : 0;
            } break;
            default: logger.error("Found unknown/unhandled Scope in Engine");
        }
        return toAlter;
    }

    /**
     * This method sets the distribution on how often to apply the Transformers
     * if every transformer has the same value, they are applied evenly often.
     * if e.g. a transformer has 2 and another one has 1, then the 2-transformer is applied twice as much.
     *
     * They are applied using a random algorithm and not in order.
     *
     * You can create certain distributions using factory methods of this class,
     * building distributions e.g. for a certain set of categories of all available transformations in the registry.
     *
     * @param distribution
     * @throws UnsupportedOperationException when the distribution contains items that are not in the registry.
     */
    public void setDistribution(Map<Transformer,Integer> distribution){
        // check whether all Transformers in the distribution are in the registry
        if(!distribution.keySet().stream().allMatch(
                d -> registry.getRegisteredTransformers().contains(d)
        )){
            throw new UnsupportedOperationException("The given distribution contains transformation outside of registry");
        }
        if(distribution.values().stream().anyMatch(v -> v < 0)) {
            throw new UnsupportedOperationException("The given distribution negative amounts for transformations");
        }

        this.distribution = distribution;
    }

    /**
     * This method sets the number of transformations and its scope.
     * They are multiplied with the item under alternation, example:
     * The system has 2 classes, scope classes and transformations 100 -> 200 Transformations in total.
     * The system has 10 methods, scope method, and transformations 10 -> 100 Transformations in total.
     *
     * The transformations are (currently) applied evenly among all items, so setting the scope to
     * "perMethod" does not mean that every method is altered 10 times,
     * but in average there will be 10 alternations per method.
     *
     * @param transformations
     * @param scope
     * @throws UnsupportedOperationException for negative number of transformations
     */
    public void setNumberOfTransformationsPerScope(long transformations, TransformationScope scope){
        if (transformations < 0) {
           throw new UnsupportedOperationException("Number of transformations cannot be negative");
        }
        this.scope = scope;
        this.numberOfTransformationsPerScope = transformations;
    }

    /**
     * Whether to write to the output folder or not.
     * All other logic is still applied as usual.
     *
     * This is mostly intended to make tests easier without the need for file cleanup.
     * @param val true if you want to write output, false otherwise.
     */
    public void setWriteJavaOutput(boolean val){
        writeJavaOutput = val;
    }

    /**
     * This method builds a distribution of the transformers in the
     * registry according to the given distribution of categories.
     * If the categories all have 1, they are applied evenly often.
     * If category A has 1, and category B has 2, then B will be used twice as much as A.
     *
     * The distribution will contain every transformer of the registry at least once if they have one category.
     *
     * @param distributionByCategory
     * @return A distribution of the registries transformers quantified by categories
     */
    public Map<Transformer,Integer> setDistributionByCategory(Map<TransformationCategory,Integer> distributionByCategory) {
        // TODO: Fill me
        // TODO: Decide whether the total amount of transformers is twice as much as the others
        return new HashMap<>();
    }

    /**
     * Sets a writer for this engine.
     * Overwrites existing writers if there are any.
     *
     * @param writer
     * @throws UnsupportedOperationException when an null-writer is entered
     */
    public void setManifestWriter(ManifestWriter writer){
        if(writer == null){
            throw new UnsupportedOperationException("Received null for writer");
        } else {
            this.writer = Optional.of(writer);
        }
    }

    /**
     * This method sets whether all comments are removed or not.
     * The comments are still entities in the AST, but are not in the toString() or prettyprinting.
     * @param val whether or not to remove all comments in all files - true for removal, false for keeping
     */
    public void setRemoveAllComments(boolean val) {
        this.removeAllComments = val;
    }

    /**
     * Sets the random number provider to using a certain seed.
     * Used for testing and repeatable experiments.
     * The default seed at initialization is the seed provided in App
     * @param seed
     */
    public void setRandomSeed(long seed){
        this.random = new Random(seed);
    }

}
