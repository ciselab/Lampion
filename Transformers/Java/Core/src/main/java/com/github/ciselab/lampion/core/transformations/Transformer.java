package com.github.ciselab.lampion.core.transformations;

import spoon.reflect.declaration.CtElement;

import java.util.Set;
import java.util.function.Predicate;

public interface Transformer {
    /**
     * This class is the toplevel Interface for all Implementations of Metamorphic Transformers.
     * A metamorphic transformer can apply a metamorphic transformation to code, that is altering the look, syntax
     * or structure of the code while being effectively equals ("the code does the same").
     *
     * There have been multiple approaches considered all evolving around the principal issues mentioned in
     * DesignNotes.md "Registration of Transformations".
     * There are verbatim examples in the Lampion/Resources/Transformations.md
     * Despite the need for Java classes for logical separation, most Transformations are somewhat pure functions
     * Transformation :: AST -> (AST,Record)
     *
     * To implement the Transformations, each Transformer has a method "applyAtRandomPosition(AST)" which returns
     * and entry on what has been altered. The application is done as a side effect, so the AST put in is altered.
     * This behaviour using side-effects is unfortunate, but I think it will turn out the best code-wise.
     * To use the Transformations in a certain scope, simply just pass a certain scope into the transformation.
     * This "applyAtRandomPosition" enables the Engine to only have a list of functions stored to pick from,
     * without the ugly use of reflections. Extendability is given as this interface is public.
     *
     * I thought longer about implementing something with generics or reflections to enable more constrained approaches
     * but I think it's best to have the Transformer pretty "blind" and unrestricted in its surroundings, relying on the
     * care of the developer.
     * To Express this, the constraints where put in place whenever your Transformation cannot be applied.
     * These are e.g. that you cannot apply a Transformation renaming variables, if there are no variables.
     * Or the Transformer can collect constraints with each run,
     * if the transformation cannot be applied twice to an element.
     * Feel free to declare as many constraints as reasonable.
     *
     * There should be two globally managed and taken care of attributes of a Transformation:
     * - The debug level of the TransformationResult
     * - A random seed, to enable better testing
     *
     * To reduce crashes, the Predicates and Exclusive-Declarations where introduced.
     * Be Careful to properly test all transformations.
     * You find examples and helpers for tests in the "com.github.ciselabd.lampion.Transformation"-package src/test/java
     */

    /**
     * This method applied the class-specific Transformation to a random, valid element of the given AST.
     * It should check itself for constraints given.
     *
     * The Transformation returns a TransformationResult-Element, that holds all relevant information.
     * In case of a failing transformation or unmatched constraints, return an EmptyTransformationResult.
     *
     * The AST is altered in the process.
     *
     * @param ast The toplevel AST from which to pick a qualified children to transform.
     * @return The TransformationResult, containing all relevant information of the transformation
     */
    TransformationResult applyAtRandom(CtElement ast);

    /**
     * To enable a more correct approach in randomly picking next transformations,
     * there must be some kind of extra-information.
     * One important information is that some Transformations are diametric to each other, that is they cancel each other.
     *
     * @return a set of Transformation-Types that cannot be applied together with this Transformation.
     */
    Set<Class<Transformer>> isExclusiveWith();

    /**
     * To not break the code when applying Transformations,
     * each transformation can require a set of checks before they are applied.
     * Can be left empty on own risk.
     *
     * @return a set of predicates, which have to return "true" in order for the Transformation to be applicable.
     */
    Set<Predicate<CtElement>> getRequirements();

    /**
     * This method gives information on what kind of categories a transformation fits in.
     * It is used for later visualisation and storing the records apropiatly.
     * Optionally, this could be implemented to be a Set of Strings, but this way it's easier to match across classes.
     *
     * @return A set of categories that match for this Transformation
     */
    Set<TransformationCategory> getCategories();

    /**
     * Re-Initializes the random machine of the Transformer, if there is any.
     * For non-random transformers, this can be defaulted to doing nothing.
     * All others should reset their `random`.
     *
     * @param seed used for setting the random machine.
     */
    void setSeed(long seed);
}
