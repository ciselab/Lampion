package com.github.ciselab.lampion.transformations.transformers;

import com.github.ciselab.lampion.support.RandomNameFactory;
import com.github.ciselab.lampion.transformations.EmptyTransformationResult;
import com.github.ciselab.lampion.transformations.SimpleTransformationResult;
import com.github.ciselab.lampion.transformations.TransformationCategory;
import com.github.ciselab.lampion.transformations.TransformationResult;
import com.github.ciselab.lampion.transformations.Transformer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import spoon.refactoring.CtRenameLocalVariableRefactoring;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;

public class RenameVariableTransformer extends BaseTransformer {

    // Whether this Transformer will produce pseudo-random names or full character-soup
    private boolean fullRandomStrings = false;
    protected String name = "RenameVariableTransformer";

    // This Map holds all changed VariableNames to not randomize VariableNames twice.
    private Map<CtMethod,List<CtLocalVariable>> alreadyAlteredVariableNames = new HashMap<>();


    public RenameVariableTransformer() {
        super();
        setConstraints();
    }

    public RenameVariableTransformer(long seed) {
        super(seed);
        setConstraints();
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
    //TODO: Remove debug code
    @Override
    public TransformationResult applyAtRandom(CtElement ast) {
        // Sanity check, if there are blockers in the constraints return empty TransformationResult
        if (!getRequirements().stream().allMatch(r -> r.test(ast))) {
//            for(int i = 0; i < getRequirements().size(); i++) {
//                Predicate<CtElement> t = (Predicate<CtElement>) getRequirements().toArray()[i];
//                if(!t.test(ast))
//                    System.out.println(i);
//            }
            return new EmptyTransformationResult();
        }
        Optional<CtMethod> oToAlter = pickRandomMethod(ast);
        // Check for existance of methods is done beforehand per constraints, so I just get the result right away
        CtMethod toAlter = oToAlter.get();

        Optional<CtLocalVariable> oVarToAlter = pickRandomVariable(toAlter);
        // As the altered method is altered forever and in all instances, safe a clone for the transformation result.
        CtMethod savedElement = toAlter.clone();
        savedElement.setParent(toAlter.getParent());
        savedElement.getParent().updateAllParentsBelow();

        applyRenameVariableTransformer(toAlter, oVarToAlter.get());

        // If debug information is wished for, create a bigger Transformationresult
        // Else, just return a minimal Transformationresult
        if (debug) {
            return new SimpleTransformationResult(name,savedElement,this.getCategories(),beforeAfterOverview(savedElement,toAlter),ast.clone());
        } else {
            return new SimpleTransformationResult(name,savedElement,this.getCategories());
        }
    }

    private void applyRenameVariableTransformer(CtMethod toAlter, CtLocalVariable varToAlter) {
        CtRenameLocalVariableRefactoring refac = new CtRenameLocalVariableRefactoring();
        //CtRenameGenericVariableRefactoring refac = new CtRenameGenericVariableRefactoring();
        refac.setTarget(varToAlter);
        String name = fullRandomStrings ? RandomNameFactory.getRandomString(random) : RandomNameFactory.getCamelcasedAnimalString(false,random);
        refac.setNewName(name);
        refac.refactor();

        // Add the altered variable to the toplevel map to keep track that it was altered in constraints
        if(alreadyAlteredVariableNames.containsKey(toAlter)){
            alreadyAlteredVariableNames.get(toAlter).add(varToAlter);
        } else {
            List<CtLocalVariable> l = new ArrayList<>();
            l.add(varToAlter);
            alreadyAlteredVariableNames.put(toAlter,l);
        }

        // Take the closest compilable unit (the class) and restore the ast according to transformers presettings
        CtClass containingclass = toAlter.getParent(p -> p instanceof CtClass);
        restoreAstAndImports(containingclass);
    }

    /**
     * This method picks a random variable out of the method.
     * @param method the ast of the method.
     * @return the randomly picked variable.
     */
    private Optional<CtLocalVariable> pickRandomVariable(CtMethod method) {
        List<CtLocalVariable> existingVariables = method
                .filterChildren(u -> u instanceof CtLocalVariable).list()
                .stream()
                .map(m -> (CtLocalVariable) m)
                .collect(Collectors.toList());
        // If there are already altered variable names for this method,
        // remove all altered variables from the pool of possible chosen element
        if(alreadyAlteredVariableNames.containsKey(method)){
            List<CtLocalVariable> alteredVariables = new ArrayList<>(alreadyAlteredVariableNames.get(method));

            existingVariables.removeAll(alteredVariables);
        }
        List<CtLocalVariable> varsToPickFrom = existingVariables;

        if(existingVariables.size()==0){
            return Optional.empty();
        } else {
            // Pick a number between 0 and count(methods)
            int randomValidIndex = random.nextInt(varsToPickFrom.size());
            // return the variable at the position
            return Optional.of(varsToPickFrom.get(randomValidIndex));
        }
    }

    /**
     * Returns a random method of the ast.
     * Check whether ast is empty is done earlier using constraints.
     * It returns empty if there are either no methods with variables,
     * or all variable are already altered by this transformer.
     *
     * Note: Cannot easily be extracted, as there is an extra, unique check for methods with variables.
     *
     * @param ast the toplevel element from which to pick a random method
     * @return a random method. Empty if there are no suited left. Reference is passed, so altering this element will alter the toplevel ast
     */
    private Optional<CtMethod> pickRandomMethod(CtElement ast) {
        // Get all Methods with Variables
        List<CtMethod> allMethods = ast.filterChildren(
                c -> c instanceof CtMethod                                  // the child is a method
                        && !c.filterChildren(p -> p instanceof CtLocalVariable).list().isEmpty()       // the method has parameters
                        && pickRandomVariable((CtMethod) c ).isPresent()   // there are free parameters left
        ).list();
        // Check for non-empty Methods is done beforehand per constraints
        // Pick a number between 0 and count(methods)
        int randomValidIndex = random.nextInt(allMethods.size());
        // return the method at the position
        return Optional.of(allMethods.get(randomValidIndex));
    }

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
        // With being so trivial, the compilers are very likely to throw out all useless code
        // Hence there will be no change in bytecode
        // As there is no forking (only true case) there is no controlflow change (the flow always goes one way)
        Set<TransformationCategory> categories = new HashSet<>();
        categories.add(TransformationCategory.NAMING);
        categories.add(TransformationCategory.SMELL);
        categories.add(TransformationCategory.NLP);
        return categories;
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
     * Adds the required base-line constraints for this class to the constraints.
     * For this Transformer, the constraints are:
     * 1. There are methods in the Ast
     * 2. The methods have variables
     * 3. There are methods that have not-randomized / not altered names (altering them twice would be useless)
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

        Predicate<CtElement> methodsHaveVariables = ct -> {
            return  ct.filterChildren(c -> c instanceof CtVariable)
                    .list()
                    .stream()
                    .map(c -> (CtVariable) c)
                    .anyMatch( m -> !m.getSimpleName().isEmpty());
        };

        // Whether there are any variables un-altered left available
        Predicate<CtElement> methodsHaveFreeVariables = ct -> {
            return  ct.filterChildren(c -> c instanceof CtMethod)
                    .list()
                    .stream()
                    .map(c -> (CtMethod) c)
                    .filter(m -> !m.filterChildren(c -> c instanceof CtLocalVariable).list().isEmpty())
                    .anyMatch( m -> pickRandomVariable(m).isPresent());
        };

        constraints.add(hasMethods);
        constraints.add(methodsHaveVariables);
        constraints.add(methodsHaveFreeVariables);
    }
}
