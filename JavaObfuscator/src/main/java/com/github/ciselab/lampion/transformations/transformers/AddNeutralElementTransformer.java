package com.github.ciselab.lampion.transformations.transformers;

import com.github.ciselab.lampion.transformations.*;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This Transformer takes Literals or variables (with supported types), and adds the corresponding neutral element.
 * Examples:
 *
 * From
 *      return a + 1
 * To
 *      return a + (1 + 0)
 *
 * Or From
 *      return "Hey"
 * To
 *      return ("Hey"+"")
 *
 * Or from
 *      return a + 1
 * To
 *      return (a+0)+1
 *
 * Due to Java Binary Type promotion, adding a char to a char will result in an integer, which makes char addition
 * only doable with casting (which is a bit ugly).
 * Hence, the currently supported types are:
 *  - String
 *  - Int
 *  - Float
 *  - Long
 *
 */
public class AddNeutralElementTransformer extends BaseTransformer {

    public final String name = "AddNeutralElement";    // The name used for TransformationResults

    public AddNeutralElementTransformer() {
        super();

        Predicate<CtElement> hasLiterals = ct -> {
            return ! ct.filterChildren(c ->
                        c instanceof CtLiteral
                        && isSupportedType(((CtLiteral<?>) c).getType())
                    )
                    .list()
                    .isEmpty();
        };

        Predicate<CtElement> hasVariables = ct -> {
            return ! ct.filterChildren(c ->
                    c instanceof CtVariableRead
                            && isSupportedType(((CtVariableRead<?>) c).getType())
                    )
                    .list()
                    .isEmpty();
        };

        Predicate<CtElement> hasAnyValidElements = hasLiterals.or(hasVariables);

        constraints.add(hasAnyValidElements);
    }

    /**
     * @param seed for the random number provider, used for testing and reproducible results
     */
    public AddNeutralElementTransformer(long seed) {
        super(seed);

        Predicate<CtElement> hasLiterals = ct -> {
            return ! ct.filterChildren(c ->
                    c instanceof CtLiteral
                            && isSupportedType(((CtLiteral<?>) c).getType())
                    )
                    .list()
                    .isEmpty();
        };

        Predicate<CtElement> hasVariables = ct -> {
            return ! ct.filterChildren(c ->
                    c instanceof CtVariableRead
                            && isSupportedType(((CtVariableRead<?>) c).getType())
                    )
                    .list()
                    .isEmpty();
        };

        Predicate<CtElement> hasAnyValidElements = hasLiterals.or(hasVariables);

        constraints.add(hasAnyValidElements);
    }

    /**
     * This method applied the class-specific Transformation to a random, valid element of the given AST.
     * It should check itself for constraints given.
     * <p>
     * The Transformation returns a TransformationResult-Element, that holds all relevant information.
     * In case of a failing transformation or unmatched constraints, return an EmptyTransformationResult.
     * <p>
     * The AST is altered in the process.
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

        CtTypedElement toAlter = pickRandomValidElement(ast);
        // As the altered method is altered forever and in all instances, safe a clone for the transformation result.
        CtElement savedElement = toAlter.clone();
        savedElement.setParent(toAlter.getParent());
        savedElement.getParent().updateAllParentsBelow();

        applyAddNeutralElementTransformation(toAlter);

        // If debug information is wished for, create a bigger Transformationresult
        // Else, just return a minimal Transformationresult
        if (debug) {
            return new SimpleTransformationResult(name,savedElement,this.getCategories(),beforeAfterOverview(savedElement,toAlter),ast.clone());
        } else {
            return new SimpleTransformationResult(name,savedElement,this.getCategories());
        }
    }

    private void applyAddNeutralElementTransformation(CtTypedElement toAlter){
        /*
        Due to not every CtTypedElement being an expression,
        it needs to do a little extra steps.
        With casting to expressions it would be even Worse IMO,
        so I just checked for both supported types and throw an exception otherwise.
         */
        Factory factory = toAlter.getFactory();
        CtLiteral neutralElement = getNeutralElement(toAlter);

        if(toAlter instanceof CtLiteral) {
            CtLiteral copy = (CtLiteral) toAlter.clone();

            var binaryOp = factory.createBinaryOperator();
            binaryOp.setKind(BinaryOperatorKind.PLUS);
            binaryOp.setType(copy.getType());
            binaryOp.setLeftHandOperand(copy);
            binaryOp.setRightHandOperand(neutralElement);

            toAlter.replace(binaryOp);
        } else if (toAlter instanceof CtVariableRead) {
            CtVariableRead copy = (CtVariableRead) toAlter.clone();


            var binaryOp = factory.createBinaryOperator();
            binaryOp.setKind(BinaryOperatorKind.PLUS);
            binaryOp.setType(copy.getType());
            binaryOp.setLeftHandOperand(copy);
            binaryOp.setRightHandOperand(neutralElement);

            toAlter.replace(binaryOp);
        } else {
            throw new UnsupportedOperationException("Received an unsupported type of CtTypedElement to add Neutral Elements to");
        }
        toAlter.getParent().updateAllParentsBelow();
    }

    /**
     * This method helps returns the neutral element for a set of supported types.
     * Some items are not that easily added in java, e.g. if one adds two chars, they become an integer (same goes for bytes).
     *
     * Here is the article on Java primitive type promotion
     * https://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.6.2
     *
     * Supported types are
     *  - String
     *  - Int
     *  - Float
     *  - Long
     *
     * @param lit the literal to which to get a neutral element
     * @return
     */
    private CtLiteral<?> getNeutralElement(CtTypedElement<?> lit){
        Factory factory = lit.getFactory();
        switch(lit.getType().getSimpleName()){
            case "int": return factory.createLiteral(0);
            case "long": return factory.createLiteral(0L);
            case "float": return factory.createLiteral(0.0f);
            case "double": return factory.createLiteral(0.0d);
            case "String": return factory.createLiteral("");
            default: throw new UnsupportedOperationException("Received unsupported type for neutral elements");
        }
    }

    private boolean isSupportedType(CtTypeReference type) {
        return isSupportedType(type.getSimpleName());
    }

    private boolean isSupportedType(String typename) {
        switch(typename){
            case "int": return true;
            case "long": return true;
            case "float": return true;
            case "double": return true;
            case "String": return true;
            default: return false;
        }
    }


    /**
     * Returns a random literal of the ast.
     * Check whether ast is empty is done earlier using constraints.
     *
     * @param ast the toplevel element from which to pick a random method
     * @return a random supported literal. Reference is passed, so altering this element will alter the toplevel ast.
     */
    private CtTypedElement pickRandomValidElement(CtElement ast) {
        // Check for all Literals that are supported
        List<CtTypedElement> validElements = ast
                .filterChildren(c -> c instanceof CtLiteral || c instanceof CtVariableRead)
                .list()
                .stream()
                .map(p -> (CtTypedElement)p)
                .filter(u -> isSupportedType(u.getType()))
                .collect(Collectors.toList());
        // Pick a number between 0 and count(literals)
        int randomValidIndex = random.nextInt(validElements.size());
        // return the method at the position
        return validElements.get(randomValidIndex);
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
     * It is used for later visualisation and storing the records appropriately.
     * Optionally, this could be implemented to be a Set of Strings, but this way it's easier to match across classes.
     *
     * @return A set of categories that match for this Transformation
     */
    @Override
    public Set<TransformationCategory> getCategories() {
        Set<TransformationCategory> categories = new HashSet<>();
        categories.add(TransformationCategory.STRUCTURE);
        categories.add(TransformationCategory.SMELL);
        return categories;
    }
}
