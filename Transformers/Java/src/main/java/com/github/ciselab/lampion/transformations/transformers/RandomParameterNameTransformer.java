package com.github.ciselab.lampion.transformations.transformers;

import com.github.ciselab.lampion.support.RandomNameFactory;
import com.github.ciselab.lampion.transformations.*;
import spoon.refactoring.CtRenameGenericVariableRefactoring;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This Transformer changes a parametername of a random method to be a random String.
 * TODO: check if local variables are changed??
 */
public class RandomParameterNameTransformer extends BaseTransformer {

    private static final String name = "RandomParameterName";

    // Whether this Transformer will produce pseudo-random names or full character-soup
    private boolean fullRandomStrings = false;

    // This Map holds all changed ParameterNames to not randomize ParameterNames twice.
    private Map<CtMethod,List<CtVariable>> alreadyAlteredParameterNames = new HashMap<>();

    public RandomParameterNameTransformer(){
        super();
        setConstraints();
    }

    public RandomParameterNameTransformer(long seed){
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

        Optional<CtMethod> oToAlter = pickRandomMethod(ast);
        // Check for existance of methods is done beforehand per constraints, so I just get the result right away
        CtMethod toAlter = oToAlter.get();

        Optional<CtVariable> oVarToAlter = pickRandomParameter(toAlter);
        // oVarToAlter always exists, as both check for params and check for non-changed params are done by constraints.
        // As the altered method is altered forever and in all instances, safe a clone for the transformation result.
        CtMethod savedElement = toAlter.clone();
        savedElement.setParent(toAlter.getParent());
        savedElement.getParent().updateAllParentsBelow();
        applyRandomParameterNameTransformation(toAlter, oVarToAlter.get());

        // If debug information is wished for, create a bigger Transformationresult
        // Else, just return a minimal Transformationresult
        if (debug) {
            return new SimpleTransformationResult(name, savedElement, this.getCategories(), beforeAfterOverview(savedElement, toAlter), ast.clone());
        } else {
            return new SimpleTransformationResult(name, savedElement, this.getCategories());
        }
    }

    /**
     * This method renames a random parameter of the toAlter method.
     * The toAlter CtMethod is altered in the process.
     *
     * if there is a return statement in the block, there is a trivial return null in the else block.
     * @param toAlter the CTMethod to change a random parameter from.
     * @param varToAlter the parameter to alter.
     */
    private void applyRandomParameterNameTransformation(CtMethod toAlter, CtVariable varToAlter) {
        CtRenameGenericVariableRefactoring refac = new CtRenameGenericVariableRefactoring();
        refac.setTarget(varToAlter);
        String name = fullRandomStrings ? RandomNameFactory.getRandomString(random) : RandomNameFactory.getCamelcasedAnimalString(false,random);
        refac.setNewName(name);
        refac.refactor();

        // Add the altered variable to the toplevel map to keep track that it was altered in constraints
        if(alreadyAlteredParameterNames.containsKey(toAlter)){
            alreadyAlteredParameterNames.get(toAlter).add(varToAlter);
        } else {
            List<CtVariable> l = new ArrayList<>();
            l.add(varToAlter);
            alreadyAlteredParameterNames.put(toAlter,l);
        }

        // Take the closest compilable unit (the class) and restore the ast according to transformers presettings
        CtClass containingclass = toAlter.getParent(p -> p instanceof CtClass);
        restoreAstAndImports(containingclass);
    }

    /**
     * Returns a random method of the ast.
     * Check whether ast is empty is done earlier using constraints.
     * It returns empty if there are either no methods with parameters,
     * or all parameters are already altered by this transformer.
     *
     * Note: Cannot easily be extracted, as there is an extra, unique check for methods with parameters.
     *
     * @param ast the toplevel element from which to pick a random method
     * @return a random method. Empty if there are no suited left. Reference is passed, so altering this element will alter the toplevel ast
     */
    private Optional<CtMethod> pickRandomMethod(CtElement ast) {
        // Get all Methods with Parameters
        List<CtMethod> allMethods = ast.filterChildren(
                c -> c instanceof CtMethod                                  // the child is a method
                        && !((CtMethod) c).getParameters().isEmpty()        // the method has parameters
                        && pickRandomParameter((CtMethod) c ).isPresent()   // there are free parameters left
        ).list();
        // Check for non-empty Methods is done beforehand per constraints

        // Pick a number between 0 and count(methods)
        int randomValidIndex = random.nextInt(allMethods.size());
        // return the method at the position
        return Optional.of(allMethods.get(randomValidIndex));
    }

    /**
     * Short explanation:
     * The CtParameters "form" a CtVariable as far as I can tell.
     * The CtParameter is just the VariableDeclaration,
     * hence the Variable must be altered to alter it in every position.
     *
     * @return a CtVariable that has not been randomized by this transformer, empty if there are none available
     */
    private Optional<CtVariable> pickRandomParameter(CtMethod method) {
        /**
         * There was an issue with "disappearing" parameters, this was due to the "removeIf" on
         * method.getParameters()
         * Because the removal removed them from the actual parameters.
         * There are some regression-tests in place now.
         */
        List<CtVariable> allParams = (List<CtVariable>) method
                .getParameters()
                .stream()
                .map(p -> (CtVariable) p)
                // This is a short check to not alter the main(String[] args)
                // While it should be ok to change it, rather not touch that hot potato
                .filter(p -> ! ((CtVariable<?>) p).getSimpleName().equalsIgnoreCase("args"))
                .collect(Collectors.toList());

        // If there are already altered parameter names for this method,
        // remove all altered parameters from the pool of possible chosen element
        if(alreadyAlteredParameterNames.containsKey(method)){
            List<String> alteredParameters = alreadyAlteredParameterNames.get(method).stream()
                    .map(p -> p.toString()).collect(Collectors.toList());

            allParams.removeIf(p -> alteredParameters.contains(p.toString()));
        }
        List<CtVariable> paramsToPickFrom = allParams;

        if(allParams.size()==0){
            return Optional.empty();
        } else {
            // Pick a number between 0 and count(parans)
            int randomValidIndex = random.nextInt(paramsToPickFrom.size());
            // return the method at the position
            return Optional.of(paramsToPickFrom.get(randomValidIndex));
        }
    }

    /**
     * Sets the value of being full random or semi random.
     * If set to true, you get full random strings such as zhüojqyjjke
     * If set to false, you get pseudo random string such as getSlyElefantLawyer
     * @param value whether to use pseudo random strings (false) or full random strings (true)
     */
    public void setFullRandomStrings(boolean value){
        this.fullRandomStrings=value;
    }

    public boolean isFullRandomStrings(){
        return fullRandomStrings;
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

        categories.add(TransformationCategory.SMELL);
        categories.add(TransformationCategory.NAMING);
        categories.add(TransformationCategory.NLP);

        return categories;
    }

    /**
     * Adds the required base-line constraints for this class to the constraints.
     * For this Transformer, the constraints are:
     * 1. There are methods in the Ast.
     * 2. The methods have parameters.
     * 3. There are methods that have not-randomized / not altered names (altering them twice would be useless).
     */
    private void setConstraints() {
        /*
        These Constraints are self-inclusive to some extend,
        but they are kept as 3 to make them more expressive.
        Shrink them once there are performance issues
         */

        Predicate<CtElement> hasMethods = ct -> {
            return ! ct.filterChildren(c -> c instanceof CtMethod).list().isEmpty();
        };

        Predicate<CtElement> methodsHaveParameters = ct -> {
            return  ct.filterChildren(c -> c instanceof CtMethod)
                    .list()
                    .stream()
                    .map(c -> (CtMethod) c)
                    .anyMatch( m -> !m.getParameters().isEmpty());
        };

        // Whether there are any parameters un-altered left available
        Predicate<CtElement> methodsHaveFreeParameters = ct -> {
            return  ct.filterChildren(c -> c instanceof CtMethod)
                    .list()
                    .stream()
                    .map(c -> (CtMethod) c)
                    .filter(m -> !m.getParameters().isEmpty())
                    .anyMatch( m -> pickRandomParameter(m).isPresent());
        };

        constraints.add(hasMethods);
        constraints.add(methodsHaveParameters);
        constraints.add(methodsHaveFreeParameters);
    }
}
