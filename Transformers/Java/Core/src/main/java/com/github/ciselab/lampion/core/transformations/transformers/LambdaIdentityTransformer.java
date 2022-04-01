package com.github.ciselab.lampion.core.transformations.transformers;

import com.github.ciselab.lampion.core.transformations.EmptyTransformationResult;
import com.github.ciselab.lampion.core.transformations.SimpleTransformationResult;
import com.github.ciselab.lampion.core.transformations.TransformationCategory;
import com.github.ciselab.lampion.core.transformations.TransformationResult;
import com.github.ciselab.lampion.core.transformations.Transformer;
import com.github.ciselab.lampion.core.transformations.TransformerUtils;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This Transformer wraps a literal into an identity lambda.
 * Example
 * Before
     * int addOne(int a){return a + 1;}
 * After
     * int addOne(int a){return a + (()->1).get()}
 *
 * At least, that was the intention. However, a small adjustment had to me made:
 * To be compilable, casts and fully qualified imports were necessary.
 * Hence, the after looks like:
     * int addOne(int a) {
     *  return a + ((int) (((java.util.function.Supplier<\?>) (() -> 1)).get()));
     * }
 *
 * In general, this Transformer will require java to be of at least version 8 and will break older projects,
 * which is something to look out for in selecting experiments.
 *
 * ========================================= IMPORTANT ==============================================================
 *
 * Unlike other Transformers, this Transformer ONLY works on Classes generated from files.
 * This is due to the need for playing with imports, which only work for compilation-units, which need (except under  very
 * special treatment) to be created from files.
 *
 * ===================================================================================================================
 */
public class LambdaIdentityTransformer extends BaseTransformer {

    String name = "LambdaIdentity";

    public LambdaIdentityTransformer(){
        super();
        setConstraints();
    }

    public LambdaIdentityTransformer(long seed){
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

        CtLiteral toAlter = TransformerUtils.pickRandomLiteral(ast,random);
        // As the altered method is altered forever and in all instances, safe a clone for the transformation result.
        CtLiteral savedElement = toAlter.clone();
        savedElement.setParent(toAlter.getParent());
        savedElement.getParent().updateAllParentsBelow();

        applyWrapInIdentityLambdaTransformation(toAlter);

        // If debug information is wished for, create a bigger Transformationresult
        // Else, just return a minimal Transformationresult
        if (debug) {
            return new SimpleTransformationResult(name,savedElement,this.getCategories(),beforeAfterOverview(savedElement,toAlter),ast.clone());
        } else {
            return new SimpleTransformationResult(name,savedElement,this.getCategories());
        }
    }

    /**
     * This method wraps the literal in an identity supplier lambda.
     * The toAlter CtLiteral is altered in the process.
     *
     * @param toAlter the CtLiteral i to wrap in an (()->i).get()
     */
    private void applyWrapInIdentityLambdaTransformation(CtLiteral toAlter) {
        // Important: Make a clone ! Otherwise it's overwriting the initial items attributes
        Factory factory = toAlter.getFactory();
        CtLambda lambda = factory.createLambda();
        lambda.setExpression(toAlter.clone());
        // This is a bit noisy, but required to make it compile and restore types
        CtExpression wrapped = factory.createCodeSnippetExpression(
            "(("+toAlter.getType().getSimpleName()+")((java.util.function.Supplier<?>)("+lambda.toString()+")).get())"
        );
        wrapped.setType(toAlter.getType());
        wrapped.setPosition(toAlter.getPosition());
        wrapped.setParent(toAlter.getParent());

        toAlter.replace(wrapped);

        CtClass containingclass = toAlter.getParent(p -> p instanceof CtClass);

        // Add the import if it does not exist already
        var unit = containingclass.getFactory().CompilationUnit().getOrCreate(containingclass);
        var existingImports = unit.getImports();
        var supplierReference = factory.createImport(factory.createReference("java.util.function.Supplier"));
        if(!existingImports.contains(supplierReference)){
            existingImports.add(supplierReference);
        }

        restoreAstAndImports(containingclass);
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
        HashSet<TransformationCategory> categories = new HashSet<>();
        categories.add(TransformationCategory.STRUCTURE);
        categories.add(TransformationCategory.LAMBDA);
        // Lambdas are LIKELY to alter the ByteCode. Not necessarily.
        categories.add(TransformationCategory.BYTECODE);
        return categories;
    }

    private void setConstraints(){
        Predicate<CtElement> hasLiterals = c -> {
            return !c.filterChildren(ch -> ch instanceof CtLiteral).list().isEmpty();
        };

        constraints.add(hasLiterals);
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
            result = 31 * result + Long.hashCode(this.seedOnCreation);
            hashCode = result;
        }
        return result;
    }

    @Override
    public boolean equals(Object o){
        if (o == this)
            return true;
        if (o instanceof LambdaIdentityTransformer other){
            return other.triesToCompile == this.triesToCompile
                    && other.setsAutoImports == this.setsAutoImports
                    && other.seedOnCreation == this.seedOnCreation;
        }
        return false;
    }
}
