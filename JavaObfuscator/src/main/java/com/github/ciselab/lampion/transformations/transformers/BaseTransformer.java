package com.github.ciselab.lampion.transformations.transformers;

import com.github.ciselab.lampion.program.App;
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

    protected boolean triesToCompile = true;          // Whether after applying the change, the snippets try to be compiled

    Set<Predicate<CtElement>> constraints = new HashSet<Predicate<CtElement>>();

    public BaseTransformer() {
        this.random = new Random(App.globalRandomSeed);
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

    public void setSeed(long seed){
        this.random = new Random(seed);
    }

    /**
     * This method decides whether the Transformer will try to compile the code, restoring the AST after
     * working with snippets, and verifying that the resulting code after transformation is still valid java.
     * The default for this behaviour is true, and should not be changed.
     *
     * There are two potential drawbacks, when the compiling is disabled:
     * - Sometimes you get invalid java code (especially when throwing around to broad comments and changing names)
     * - The snippets have reduced capability of being used
     *
     * Depending on your Domain / UseCase, it might be necessary to disable compiling. Do so very carefully, and only
     * if your program failed on the compiling code.
     * Also, be aware to not use some transformers compiling and others non-compiling. The compiling transformers
     * will try to compile the code left by the non-compilings, failing in an unexpected place.
     * @param value whether or not to compile the code, true for compilation, false otherwise
     */
    public void setTryingToCompile(boolean value) {this.triesToCompile = value;}
}
