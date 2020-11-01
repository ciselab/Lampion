package com.github.ciselab.lampion.transformations;

import spoon.reflect.declaration.CtElement;

import java.util.Optional;
import java.util.Set;

/**
 * Minimal Implementation of Transformation result providing simple getters and a set of constructors.
 *
 * Be careful when working with the CTElement, as further alternations will alter the TransformationResult
 * if only a reference is stored. Hence, store a copy of the element!
 *
 * For the equality, the optional attributes are ignored purposefully.
 * These are only determined by debug-degree and should not be used for logical attributes.
 */
public class SimpleTransformationResult implements TransformationResult {

    private Set<TransformationCategory> categories;
    private String transformationName;
    private CtElement element;
    private Optional<String> beforeAfter;
    private Optional<CtElement> initialScope;

    // Lazily initalized hashCode
    private int hashCode = 0;

    public SimpleTransformationResult(String name, CtElement element, Set<TransformationCategory> categories){
        transformationName = name;
        this.element = element.clone();
        this.categories = categories;
        beforeAfter = Optional.empty();
        initialScope = Optional.empty();
    }

    public SimpleTransformationResult(String name, CtElement element, Set<TransformationCategory> categories, String beforeAfter){
        transformationName = name;
        this.element = element.clone();
        this.categories = categories;
        this.beforeAfter = Optional.of(beforeAfter);
        initialScope = Optional.empty();
    }

    public SimpleTransformationResult(String name, CtElement element, Set<TransformationCategory> categories, CtElement initialScope){
        transformationName = name;
        this.element = element.clone();
        this.categories = categories;
        this.beforeAfter = Optional.empty();
        this.initialScope = Optional.of(initialScope.clone());
    }
    public SimpleTransformationResult(String name, CtElement element, Set<TransformationCategory> categories, String beforeAfter, CtElement initialScope){
        transformationName = name;
        this.element = element.clone();
        this.categories = categories;
        this.beforeAfter = Optional.of(beforeAfter);
        this.initialScope = Optional.of(initialScope.clone());
    }

    /**
     * @return The name of the Transformation, usually the class name of the Transformation
     */
    @Override
    public String getTransformationName() {
        return transformationName;
    }

    /**
     * Holds information on the actually changed element.
     * When a transformation is applied randomly to a full project AST,
     * and a single method is altered, (e.g. the rename-method alternation) this method should return the method node.
     *
     * @return the node that has been changed
     */
    @Override
    public CtElement getTransformedElement() {
        return element;
    }

    /**
     * The categories that fit this transformation, usually the getCategories() of the Transformation class.
     *
     * @return the categories matching for this Transformation Step
     */
    @Override
    public Set<TransformationCategory> getCategories() {
        return categories;
    }

    /**
     * Stores the before and after state of the transformed element, used for debugging and maybe tests.
     * In a production environment, this should usually be only optional empties.
     *
     * @return a String of the before and after state of the changed element, or empty() if this is disabled
     */
    @Override
    public Optional<String> getBeforeAfterComparison() {
        return beforeAfter;
    }

    /**
     * Stores the node that has been used for toplevel application, that is the highest scoped AST Node.
     * This is a feature that should be turned of for production.
     *
     * @return the toplevel scope from which the TransformedElement was picked, empty if this is disabled.
     */
    @Override
    public Optional<CtElement> getInitialScopeOfTransformation() {
        return initialScope;
    }

    @Override
    public boolean equals(Object o){
        if (!(o instanceof SimpleTransformationResult)){
            return false;
        }
        SimpleTransformationResult otherCasted = (SimpleTransformationResult) o;
        return this.transformationName.equals(otherCasted.getTransformationName())
                && this.element.equals(otherCasted.getCategories())
                && this.categories.equals(otherCasted.getCategories());
    }

    @Override
    public int hashCode(){
        if (this.hashCode == 0) {
           int result = transformationName.hashCode();
           result = result * 31 + categories.hashCode();
           result = result * 31 + element.hashCode();
           this.hashCode = result;
        }

        return hashCode;
    }

}
