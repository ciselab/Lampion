package com.github.ciselab.lampion.core.transformations.transformers;

import com.github.ciselab.lampion.core.transformations.EmptyTransformationResult;
import com.github.ciselab.lampion.core.transformations.SimpleTransformationResult;
import com.github.ciselab.lampion.core.transformations.TransformationCategory;
import com.github.ciselab.lampion.core.transformations.TransformationResult;
import com.github.ciselab.lampion.core.transformations.Transformer;
import com.github.ciselab.lampion.core.support.RandomNameFactory;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import java.util.*;
import java.util.function.Predicate;

/**
 * This Transformer adds a random String comment into a program
 */
public class RandomInlineCommentTransformer extends BaseTransformer {

    private static final String name = "RandomInlineComment";

    // Whether this Transformer will produce pseudo-random names or full character-soup
    private boolean fullRandomStrings = false;

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
        // Check for emptyness is done earlier as constraint, but a sanity check just in case
        if(oToAlter.isEmpty())
            return new EmptyTransformationResult();
        CtMethod toAlter = oToAlter.get();

        // As the altered method is altered forever and in all instances, safe a clone for the transformation result.
        CtMethod savedElement = toAlter.clone();
        savedElement.setParent(toAlter.getParent());
        savedElement.getParent().updateAllParentsBelow();

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
        var comment = factory.createInlineComment(
                fullRandomStrings ? RandomNameFactory.getRandomComment(random) : RandomNameFactory.getAnimalComment(3,random));

        var existingStatements = toAlter.getBody().getStatements().size();

        // Exception Case: The method was empty
        if(existingStatements==0){
            toAlter.getBody().addStatement(comment);
        } else {
            // Add it to a random position
            toAlter.getBody().addStatement(random.nextInt(existingStatements),comment);
        }

        // Take the closest compilable unit (the class) and restore the ast according to transformers presettings
        CtClass containingclass = toAlter.getParent(p -> p instanceof CtClass);
        restoreAstAndImports(containingclass);
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
                c -> c instanceof CtMethod // the child is a method
        ).list();

        // The check for empty-ness is done as constraint beforehand.
        if(allMethods.size() == 0)
            return Optional.empty();
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

    /**
     * Sets the value of being full random or semi random.
     * If set to true, you get full random strings such as zhüojqyjjke
     * If set to false, you get pseudo random string such as getSlyElefantLawyer
     * @param value whether to use pseudo random strings (false) or full random strings (true)
     */
    public void setFullRandomStrings(boolean value){
        this.fullRandomStrings=value;
    }

    public boolean isFullRandomStrings(){
        return fullRandomStrings;
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

    /*
    =========================================================
                       Hashcode & Equals
    =========================================================
    The Implementation of HashCode and Equals are a bit of a philosophical question.
    Without an override, two Transformers are always different.
    We aim to have an implementation that two transformers are equal if they
    are from the same Type (E.g. IfTrueTransformer) and have a similar configuration.
    This includes seeds.

    The tests for identity are in the test-file for this Transformer,
    but there is a separate Testfile for comparing different transformers.

    The provided implementation is taken from Joshua Bloch
    "Effective Java - third Edition".
     */
    // HashCode method with lazily initialized cached hash code
    private int hashCode = 0;

    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = name.hashCode();
            result = 31 * result + Boolean.hashCode(this.triesToCompile);
            result = 31 * result + Boolean.hashCode(this.setsAutoImports);
            result = 31 * result + Boolean.hashCode(this.fullRandomStrings);
            result = 31 * result + Long.hashCode(this.seedOnCreation);
            hashCode = result;
        }
        return result;
    }

    @Override
    public boolean equals(Object o){
        if (o == this)
            return true;
        if (o instanceof RandomInlineCommentTransformer other){
            return other.triesToCompile == this.triesToCompile
                    && other.setsAutoImports == this.setsAutoImports
                    && other.fullRandomStrings == this.fullRandomStrings
                    && other.seedOnCreation == this.seedOnCreation;
        }
        return false;
    }
}
