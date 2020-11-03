package com.github.ciselab.lampion.transformations.transformers;

import com.github.ciselab.lampion.program.App;
import com.github.ciselab.lampion.transformations.*;
import spoon.refactoring.CtRenameGenericVariableRefactoring;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.factory.Factory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This Transformer adds a random String comment into a program
 */
public class RandomInlineCommentTransformer extends BaseTransformer {

    private static final String name = "RandomInlineComment";

    // This transformer is build here and registered in the global registry of app
    private static final RandomInlineCommentTransformer delegate = buildAndRegisterDefaultDelegate();

    public RandomInlineCommentTransformer(){
        super();
        setConstraints();
    }

    public RandomInlineCommentTransformer(long seed){
        super(seed);
        setConstraints();
    }

    /**
     * This method applied the class-specific Transformation to a random, valid element of the given AST.
     * It should check itself for constraints given.
     *
     * The Transformation returns a TransformationResult-Element, that holds all relevant information.
     * In case of a failing transformation or unmatched constraints, return an EmptyTransformationResult.
     *
     * @param ast The toplevel AST from which to pick a qualified children to transform.
     * @return The TransformationResult, containing all relevant information of the transformation
     */
    @Override
    public TransformationResult applyAtRandom(CtElement ast) {
        // Sanity check, if there are blockers in the constraints return empty TransformationResult
        if (!getRequirements().stream().allMatch(r -> r.test(ast))) {
            return new EmptyTransformationResult();
        }

        Optional<CtMethod> oToAlter = pickRandomMethod(ast);
        if(oToAlter.isEmpty()){
            return new EmptyTransformationResult();
        }
        CtMethod toAlter = oToAlter.get();

        // As the altered method is altered forever and in all instances, safe a clone for the transformation result.
        CtMethod savedElement = toAlter.clone();
        applyRandomParameterNameTransformation(toAlter);

        // If debug information is wished for, create a bigger Transformationresult
        // Else, just return a minimal Transformationresult
        if (debug) {
            return new SimpleTransformationResult(name, savedElement, this.getCategories(), beforeAfterOverview(savedElement, toAlter), ast.clone());
        } else {
            return new SimpleTransformationResult(name, savedElement, this.getCategories());
        }
    }

    /**
     * This method adds a random //inlineComment to a random position in the method
     * The toAlter CtMethod is altered in the process.
     *
     * @param toAlter the CTMethod to add a random comment to
     */
    private void applyRandomParameterNameTransformation(CtMethod toAlter) {
        Factory factory  = toAlter.getFactory();
        var comment =  factory.createInlineComment(getRandomComment());

        var existingStatements = toAlter.getBody().getStatements().size();

        // Exception Case: The method was empty
        if(existingStatements==0){
            toAlter.getBody().addStatement(comment);
        } else {
            // Add it to a random position
            toAlter.getBody().addStatement(random.nextInt(existingStatements),comment);
        }
    }

    /**
     * Returns a random method of the ast.
     * Check whether ast is empty is done earlier using constraints.
     *
     * @param ast the toplevel element from which to pick a random method
     * @return a random method. Empty if there are none. Reference is passed, so altering this element will alter the toplevel ast
     */
    private Optional<CtMethod> pickRandomMethod(CtElement ast) {
        // Get all Methods with Parameters
        List<CtMethod> allMethods = ast.filterChildren(
                c -> c instanceof CtMethod                                  // the child is a method
        ).list();

        if(allMethods.isEmpty()){
            return Optional.empty();
        }

        // Pick a number between 0 and count(methods)
        int randomValidIndex = random.nextInt(allMethods.size());
        // return the method at the position
        return Optional.of(allMethods.get(randomValidIndex));
    }

    /**
     * To enable a more correct approach in randomly picking next transformations,
     * there must be some kind of extra-information.
     * One important information is that some Transformations are diametric to each other, that is they cancel each other.
     *
     * @return a set of Transformation-Types that cannot be applied together with this Transformation.
     */
    @Override
    public Set<Class<Transformer>> isExclusiveWith() {
        return new HashSet<>();
    }

    /**
     * This method gives information on what kind of categories a transformation fits in.
     * It is used for later visualisation and storing the records apropiatly.
     * Optionally, this could be implemented to be a Set of Strings, but this way it's easier to match across classes.
     *
     * @return A set of categories that match for this Transformation
     */
    @Override
    public Set<TransformationCategory> getCategories() {
        Set<TransformationCategory> categories = new HashSet<>();

        categories.add(TransformationCategory.COMMENT);
        categories.add(TransformationCategory.NLP);

        return categories;
    }

    private String getRandomComment(){
        // To look a bit more human, there will be spaces added between random strings
        int numberofwords = 1 + random.nextInt(4);
        return IntStream.range(0,numberofwords)
                .mapToObj( t -> getRandomString())
                .map(t -> (String) t)
                .collect(Collectors.joining(" "));
    }

    /**
     * Shamelessly copied from https://www.baeldung.com/java-random-string
     * @return a random, alphabetic string that contains no numbers
     */
    private String getRandomString(){
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = random.nextInt(7)+3;
        //TODO: Add spaces? Are there Spaces?

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }

    /**
     * This methods builds a transformer using the apps global seed for it's randomness
     * and registers it in the global default registry.
     * The return value is the build transformer, set to the toplevel delegate entry,
     * this behavior helps to build it at startup exploiting the static startup.
     * @return the RandomInlineCommentTransformer that is registered in Apps default registry
     */
    private static RandomInlineCommentTransformer buildAndRegisterDefaultDelegate(){
        RandomInlineCommentTransformer delegate =  new RandomInlineCommentTransformer(App.globalRandomSeed);
        App.globalRegistry.registerTransformer(delegate);
        return delegate;
    }

    /**
     * Adds the required base-line constraints for this class to the constraints.
     * For this Transformer, the constraints are:
     * 1. there are methods in the Ast
     */
    private void setConstraints() {
        Predicate<CtElement> hasMethods = ct -> {
            return ! ct.filterChildren(c -> c instanceof CtMethod).list().isEmpty();
        };
        constraints.add(hasMethods);
    }
}
