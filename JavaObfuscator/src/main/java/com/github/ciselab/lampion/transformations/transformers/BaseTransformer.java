package com.github.ciselab.lampion.transformations.transformers;

import com.github.ciselab.lampion.transformations.Transformer;
import spoon.reflect.declaration.CtElement;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This abstract class provides some shared utilities that occurred multiple times in Transformers.
 */
public abstract class BaseTransformer implements Transformer {
    protected Random random;                          // the random number provider used for picking random methods
    protected boolean debug = false;                  // whether to add more information to the TransformationResults

    Set<Predicate<CtElement>> constraints = new HashSet<Predicate<CtElement>>();

    public BaseTransformer() {
        this.random = new Random();
    }

    /**
     * @param seed for the random number provider, used for testing and reproducible results
     */
    public BaseTransformer(long seed) {
        this.random = new Random(seed);
    }

    public void setDebug(boolean debug){
        this.debug = debug;
    }

    protected String beforeAfterOverview(CtElement before, CtElement after) {
        String format = " // BEFORE \n %s \n // AFTER \n %s";
        return String.format(format,before.toString(),after.toString());
    }

    /**
     * To not break the code when applying Transformations,
     * each transformation can require a set of checks before they are applied.
     * Can be left empty on own risk.
     *
     * @return a set of predicates, which have to return "true" in order for the Transformation to be applicable.
     */
    @Override
    public Set<Predicate<CtElement>> getRequirements() {
        return constraints;
    }

}
