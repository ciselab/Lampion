package com.github.ciselab.lampion.core.transformations.transformers;

import com.github.ciselab.lampion.core.program.Engine;
import com.github.ciselab.lampion.core.transformations.Transformer;
import spoon.reflect.declaration.CtClass;
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
    protected boolean setsAutoImports = true;         // Whether foreign references will be resolved to their fully qualified name

    protected long seedOnCreation;                    // The seed used to create this transformer, later used for equality and hashcode

    Set<Predicate<CtElement>> constraints = new HashSet<Predicate<CtElement>>();

    public BaseTransformer() {
        this.seedOnCreation = Engine.globalRandomSeed;
        this.random = new Random(Engine.globalRandomSeed);
    }

    /**
     * @param seed for the random number provider, used for testing and reproducible results
     */
    public BaseTransformer(long seed) {
        this.seedOnCreation = seed;
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
        this.seedOnCreation = seed;
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

    /**
     * This method decides whether the transformer will try to resolve the references to their fully qualified name.
     * This might can fail compilations, if the references are unknown or there are multiple possible alternatives.
     *
     * Be careful to set this value globally for all transformers, as if only one transformer has it on it will set it for
     * all elements it touches.
     * @param value whether or not to resolve foreign references to their fully qualified name, such as com.apache.org.math.sin()
     */
    public void setSetsAutoImports(boolean value) {this.setsAutoImports = value;}

    /**
     * This methods performs some housekeeping actions on the given ast element.
     * It is intended to be used after transformations.
     *
     * The actions are:
     * - resolving references to their qualified name, such as from ArrayList to java.utils.ArrayList
     * - compiling snippets inside the object, to restore the ast
     *
     * As a rule of thumb, the bigger the changes are, and the higher they are in the ast,
     * the more important are compilation and import resolves.
     * With higher in the ast meaning class > method > block > statement.
     *
     * See "setTryingToCompile" and "setSetsAutoImports" for more information.
     * @param containingClass the element that can be compiled after change, usually the class containing the changed method/element
     */
    protected void restoreAstAndImports(CtClass containingClass){
        if(!setsAutoImports) {
            // Sanity Check for compilation as well as restoring items
            containingClass.getFactory().getEnvironment().setAutoImports(setsAutoImports);
            // This enables missing entries in references to be "fine"
            containingClass.getFactory().getEnvironment().setNoClasspath(setsAutoImports);
        }
        if(triesToCompile) {
            containingClass.compileAndReplaceSnippets();
        }
    }

    /**
     * Gives the Seed used to create the transformer.
     *
     * Note: The "Running Seed" is not accessible - it is hidden in the random number provider.
     * @return the Seed used on creation.
     */
    public long getSeed() {
        return seedOnCreation;
    }
}
