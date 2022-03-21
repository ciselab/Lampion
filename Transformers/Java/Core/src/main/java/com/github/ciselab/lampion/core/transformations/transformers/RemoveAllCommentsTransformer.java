package com.github.ciselab.lampion.core.transformations.transformers;

import com.github.ciselab.lampion.core.transformations.EmptyTransformationResult;
import com.github.ciselab.lampion.core.transformations.SimpleTransformationResult;
import com.github.ciselab.lampion.core.transformations.TransformationCategory;
import com.github.ciselab.lampion.core.transformations.TransformationResult;
import com.github.ciselab.lampion.core.transformations.Transformer;
import spoon.reflect.code.CtComment;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This Transformer removes all comments (inline, block, javadoc, etc.) of a given element and all it's children.
 *
 * It can be either used as a normal transformer,
 * or it can be used to clean a program if the requiring experiment does not work on commented code.
 */
public class RemoveAllCommentsTransformer extends BaseTransformer {

    public final String name = "RemoveAllComments";    // The name used for TransformationResults

    public RemoveAllCommentsTransformer() {
        super();

        Predicate<CtElement> hasComments = ct -> {
            return ! ct.filterChildren(c -> c instanceof CtComment).list().isEmpty();
        };

        constraints.add(hasComments);
    }

    /**
     * @param seed for the random number provider, used for testing and reproducible results
     */
    public RemoveAllCommentsTransformer(long seed) {
        super(seed);

        Predicate<CtElement> hasComments = ct -> {
            return ! ct.filterChildren(c -> c instanceof CtComment).list().isEmpty();
        };

        constraints.add(hasComments);
    }

    /**
     * This method applied the class-specific Transformation to the given AST.
     * It should check itself for constraints given.
     *
     * The Transformation returns a TransformationResult-Element, that holds all relevant information.
     * In case of a failing transformation or unmatched constraints, return an EmptyTransformationResult.
     *
     * As this is the "removeAll" Comment Transformer, it does not need to pick random elements
     * it just removes all comments of the given ast
     *
     * @param ast the element of which all comments will be removed.
     * @return The TransformationResult, containing all relevant information of the transformation
     */
    @Override
    public TransformationResult applyAtRandom(CtElement ast) {
        // Sanity check, if there are blockers in the constraints return empty TransformationResult
        if (!getRequirements().stream().allMatch(r -> r.test(ast))) {
            return new EmptyTransformationResult();
        }

        // As the altered method is altered forever and in all instances, safe a clone for the transformation result.
        CtElement savedElement = ast.clone();
        savedElement.setParent(ast.getParent());
        savedElement.getParent().updateAllParentsBelow();


        // Take the closest compilable unit (the class) and restore the ast according to transformers presettings
        CtClass containingClass =
                ast instanceof CtClass ? (CtClass)ast : ast.getParent(p -> p instanceof CtClass);

        containingClass.getFactory().getEnvironment().setCommentEnabled(false);

        restoreAstAndImports(containingClass);


        // If debug information is wished for, create a bigger Transformationresult
        // Else, just return a minimal Transformationresult
        if (debug) {
            return new SimpleTransformationResult(name,savedElement,this.getCategories(),beforeAfterOverview(savedElement,ast),ast.clone());
        } else {
            return new SimpleTransformationResult(name,savedElement,this.getCategories());
        }
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
        return categories;
    }

    //TODO: Equals & HashCode?
}
