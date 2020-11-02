package com.github.ciselab.lampion.transformations;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.TypeFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This Transformer wraps the block of a (random) Method into an "if(true){...}"
 *
 * See Tranformations.md for an example,
 * See Transformation.java for interface contract
 */
public class IfTrueTransformer implements Transformer {

    Random random;              // the random number provider used for picking random methods
    boolean debug = false;      // whether to add more information to the TransformationResults

    public IfTrueTransformer() {
        this.random = new Random();
    }

    /**
     * @param seed for the random number provider, used for testing and reproducible results
     */
    public IfTrueTransformer(long seed) {
        this.random = new Random(seed);
    }


    /**
     * This method applied the class-specific Transformation to a random, valid element of the given AST.
     * It should check itself for constraints given.
     * <p>
     * The Transformation returns
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

        CtMethod toAlter = pickRandomMethod(ast);
        // As the altered method is altered forever and in all instances, safe a clone for the transformation result.
        CtMethod safedElement = toAlter.clone();

        applyIfTrueTransformation(toAlter);

        // If debug information is wished for, create a bigger Transformationresult
        // Else, just return a minimal Transformationresult
        if (debug) {
            return new SimpleTransformationResult(this.getClass().getSimpleName(),safedElement,this.getCategories(),beforeAfterOverview(safedElement,toAlter),ast.clone());
        } else {
            return new SimpleTransformationResult(this.getClass().getSimpleName(),safedElement,this.getCategories());
        }
    }

    /**
     * This method wraps the full body of a method into if(true)
     * The toAlter CtMethod is altered in the process.
     *
     * if there is a return statement in the block, there is a trivial return null in the else block.
     * @param toAlter the CTMethod to wrap in an if(true){...}
     */
    private void applyIfTrueTransformation(CtMethod toAlter) {
        Factory factory = toAlter.getFactory();
        CtBlock methodBody = toAlter.getBody();

        var ifWrapper = factory.createIf();
        ifWrapper.setCondition(factory.createLiteral(true));
        ifWrapper.setThenStatement(methodBody);

        // First: Check if there is a return statement.
        // If yes, add the trivial return null statement in the else block
        if(! toAlter.filterChildren(c -> c instanceof CtReturn).list().isEmpty()){
            ifWrapper.setElseStatement(
                    factory.createBlock().addStatement(factory.createCodeSnippetStatement("return null"))
            );
        }

        toAlter.setBody(ifWrapper);
    }

    private String beforeAfterOverview(CtElement before, CtElement after) {
        String format = " // BEFORE \n %s \n // AFTER \n %s";
        return String.format(format,before.toString(),after.toString());
    }

    /**
     * Returns a random method of the ast.
     * Check whether ast is empty is done earlier using constraints.
     *
     * @param ast the toplevel element from which to pick a random method
     * @return a random element. Reference is passed, so altering this element will alter the toplevel ast.
     */
    private CtMethod pickRandomMethod(CtElement ast) {
        // Check for all methods
        List<CtMethod> allMethods = ast.filterChildren(c -> c instanceof CtMethod).list();
        // Pick a number between 0 and count(methods)
        int randomValidIndex = random.nextInt(allMethods.size()-1);
        // return the method at the position
        return allMethods.get(randomValidIndex);
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
     * To not break the code when applying Transformations,
     * each transformation can require a set of checks before they are applied.
     * Can be left empty on own risk.
     *
     * @return a set of predicates, which have to return "true" in order for the Transformation to be applicable.
     */
    @Override
    public Set<Predicate<CtElement>> getRequirements() {
        /*
         * There are some constraints which are more or less trivial:
         * 1. There are methods in the ast
         */
        Set<Predicate<CtElement>> constraints = new HashSet<>();

        return constraints;
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
        // With being so trivial, the compilers are very likely to throw out all useless code
        // Hence there will be no change in bytecode
        // As there is no forking (only true case) there is no controlflow change (the flow always goes one way)
        Set<TransformationCategory> categories = new HashSet<>();
        categories.add(TransformationCategory.STRUCTURE);
        categories.add(TransformationCategory.SMELL);
        return categories;
    }

    public void setDebug(boolean debug){
        this.debug = debug;
    }

    //TODO: Equals & HashCode?
}
