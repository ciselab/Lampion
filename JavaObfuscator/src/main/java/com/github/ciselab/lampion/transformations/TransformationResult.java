package com.github.ciselab.lampion.transformations;

import spoon.reflect.declaration.CtElement;

import java.util.Optional;
import java.util.Set;

/**
 * Easy interface for TransformationResults.
 *
 * Used for unifying the results of Transformations to gather them in one place for saving.
 *
 * Additionally, there is a implementation of "EmptyTransformationResult" to have a typesafe fail-state.
 * It is used wherever a failing Transformation is required/reasonable.
 *
 * There might be an issue with storing the CTElement directly, always store a CtElement copy!
 */
public interface TransformationResult {

    /**
     * @return The name of the Transformation, usually the class name of the Transformation
     */
    String getTransformationName();

    /**
     * Holds information on the actually changed element.
     * When a transformation is applied randomly to a full project AST,
     * and a single method is altered, (e.g. the rename-method alternation) this method should return the method node.
     * @return the node that has been changed
     */
    CtElement getTransformedElement();

    /**
     * The categories that fit this transformation, usually the getCategories() of the Transformation class.
     * @return the categories matching for this Transformation Step
     */
    Set<TransformationCategory> getCategories();

    /**
     * Stores the before and after state of the transformed element, used for debugging and maybe tests.
     * In a production environment, this should usually be only optional empties.
     * @return a String of the before and after state of the changed element, or empty() if this is disabled
     */
    default Optional<String> getBeforeAfterComparison(){
        return Optional.empty();
    }

    /**
     * Stores the node that has been used for toplevel application, that is the highest scoped AST Node.
     * This is a feature that should be turned of for production.
     * @return the toplevel scope from which the TransformedElement was picked, empty if this is disabled.
     */
    default Optional<CtElement> getInitialScopeOfTransformation(){
        return Optional.empty();
    }
}
