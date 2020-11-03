package com.github.ciselab.lampion.transformations.transformers;

import com.github.ciselab.lampion.program.App;
import com.github.ciselab.lampion.transformations.*;
import spoon.refactoring.CtRenameGenericVariableRefactoring;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.factory.Factory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This Transformer changes a parametername of a random method to be a random String.
 */
public class RandomParameterNameTransformer extends BaseTransformer {

    private static final String name = "RandomParameterName";

    // This Map holds all changed Parameternames to not randomize ParameterNames twice.
    private Map<CtMethod,List<CtVariable>> alreadyAlteredParameterNames = new HashMap<>();

    // This transformer is build here and registered in the global registry of app
    private static final RandomParameterNameTransformer delegate = buildAndRegisterDefaultDelegate();

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
        if(oToAlter.isEmpty()){
            return new EmptyTransformationResult();
        }
        CtMethod toAlter = oToAlter.get();

        Optional<CtVariable> oVarToAlter = pickRandomParameter(toAlter);

        if(oVarToAlter.isEmpty()) {
            return new EmptyTransformationResult();
        }

        // As the altered method is altered forever and in all instances, safe a clone for the transformation result.
        CtMethod savedElement = toAlter.clone();
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
     * This method wraps the full body of a method into if(true)
     * The toAlter CtMethod is altered in the process.
     *
     * if there is a return statement in the block, there is a trivial return null in the else block.
     * @param toAlter the CTMethod to wrap in an if(true){...}
     */
    private void applyRandomParameterNameTransformation(CtMethod toAlter, CtVariable varToAlter) {
        Factory factory = toAlter.getFactory();

        CtRenameGenericVariableRefactoring refac = new CtRenameGenericVariableRefactoring();
        refac.setTarget(varToAlter);
        refac.setNewName(getRandomString());
        refac.refactor();

        // Add the altered variable to the toplevel map to keep track that it was altered in constraints
        if(alreadyAlteredParameterNames.containsKey(toAlter)){
            alreadyAlteredParameterNames.get(toAlter).add(varToAlter);
        } else {
            List<CtVariable> l = new ArrayList<>();
            l.add(varToAlter);
            alreadyAlteredParameterNames.put(toAlter,l);
        }
    }

    /**
     * Returns a random method of the ast.
     * Check whether ast is empty is done earlier using constraints.
     * It returns empty if there are either no methods with parameters,
     * or all parameters are already altered by this transformer.
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

        if(allMethods.isEmpty()){
            return Optional.empty();
        }

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
        List<CtVariable> allParams = method.getParameters();

        // If there are already altered parameternames for this method,
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
     * Shamelessly copied from https://www.baeldung.com/java-random-string
     * @return a random, alphabetic string that contains no numbers
     */
    private String getRandomString(){
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 12;

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }

    /**
     * This methods builds a transformer using the apps global seed for it's randomness
     * and registers it in the global default registry.
     * The return value is the build transformer, set to the toplevel delegate entry,
     * this behavior helps to build it at startup exploiting the static startup.
     * @return the RandomParameterNameTransformer that is registered in Apps default registry
     */
    private static RandomParameterNameTransformer buildAndRegisterDefaultDelegate(){
        RandomParameterNameTransformer delegate =  new RandomParameterNameTransformer(App.globalRandomSeed);
        App.globalRegistry.registerTransformer(delegate);
        return delegate;
    }

    /**
     * Adds the required base-line constraints for this class to the constraints.
     * For this Transformer, the constraints are:
     * 1. there are methods in the Ast
     * 2. the methods have parameters
     * 3. there are methods that have not-randomized / not altered names (altering them twice would be useless)
     *
     * The third constraint is added for every new parametername on transformer run and not at constructor time.
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
