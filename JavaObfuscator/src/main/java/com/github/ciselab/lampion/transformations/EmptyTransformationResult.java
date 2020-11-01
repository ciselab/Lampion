package com.github.ciselab.lampion.transformations;

import spoon.reflect.declaration.CtElement;

import java.util.HashSet;
import java.util.Set;

/**
 * Used for representing failed TransformationResults.
 */
public class EmptyTransformationResult implements TransformationResult{

    @Override
    public String getTransformationName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public CtElement getTransformedElement() {
        return null;
    }

    @Override
    public Set<TransformationCategory> getCategories() {
        return new HashSet<>();
    }

    @Override
    public boolean equals(Object o){
        // All Empty Results are the same
        return o instanceof EmptyTransformationResult;
    }

    @Override
    public int hashCode(){
        // Return a static number for every Empty Transformation, but I did not want to pick 42 or some magic number.
        return this.getClass().getSimpleName().hashCode();
    }
}
