package com.github.ciselab.lampion.core.transformations.transformers;

import com.github.ciselab.lampion.core.transformations.EmptyTransformationResult;
import com.github.ciselab.lampion.core.transformations.SimpleTransformationResult;
import com.github.ciselab.lampion.core.transformations.TransformationCategory;
import com.github.ciselab.lampion.core.transformations.TransformationResult;
import com.github.ciselab.lampion.core.transformations.Transformer;
import com.github.ciselab.lampion.core.transformations.TransformerUtils;
import com.github.ciselab.lampion.core.support.RandomNameFactory;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
This Transformer adds an unused Variable with a random value at the beginning of a block statement.

Example Before:

```
public int getOne(){
    return 1;
}
```

After:

```
public int getOne(){
    Integer bbX61U = 2;
    return 1;
}
```

 or:

 ```
 public boolean isEven(int a){
 if( (a%2) == 0) {
    return true;
 } else {
    return false;
 }
 ```

 After:

 ```
 public boolean isEven(int a){
 if( (a%2) == 0) {
 return true;
 } else {
 String noohenpxv = "ipqegmüoy";
 return false;
 }
 ```
 ```

There are some limitations to this approach, first of all not all Types are supported. The supported types are
 - int / Integer
 - String
 - float / Float
 - long / Long
 - boolean / Boolean

 Second is, to avoid compilation errors, the variable are always added at the beginning of a block statement.
 Otherwise, the statement might be added after a return statement, being "unreachable code" and failing compilation.

 Third limitation is that due to name generation, when using pseudo names, there is a (high, but) limited amount of
 possible combinations.
 To not alter the code, it must be checked that only unused variable names are introduced, leading to a natural upper
 bound of transformations possible for each method.
 */
public class AddUnusedVariableTransformer extends BaseTransformer {

    // Whether this Transformer will produce pseudo-random names or full character-soup
    private boolean fullRandomStrings = false;
    protected String name = "AddUnusedVariable";

    public AddUnusedVariableTransformer(){
        super();

        Predicate<CtElement> hasMethods = (CtElement elem ) -> {
            return !elem.filterChildren(u -> u instanceof CtMethod).list().isEmpty();
        };
        constraints.add(hasMethods);
    }

    public AddUnusedVariableTransformer(long seed){
        super(seed);

        Predicate<CtElement> hasMethods = (CtElement elem ) -> {
            return !elem.filterChildren(u -> u instanceof CtMethod).list().isEmpty();
        };
        constraints.add(hasMethods);
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

        Optional<CtMethod> oToAlter = pickRandomMethod(ast);
        if(oToAlter.isEmpty())
            return new EmptyTransformationResult();
        CtMethod toAlter = oToAlter.get();
        // As the altered method is altered forever and in all instances, safe a clone for the transformation result.
        CtMethod savedElement = toAlter.clone();
        savedElement.setParent(toAlter.getParent());
        savedElement.getParent().updateAllParentsBelow();

        applyAddUnusedVariableTransformer(toAlter);

        // If debug information is wished for, create a bigger Transformationresult
        // Else, just return a minimal Transformationresult
        if (debug) {
            return new SimpleTransformationResult(name,savedElement,this.getCategories(),beforeAfterOverview(savedElement,toAlter),ast.clone());
        } else {
            return new SimpleTransformationResult(name,savedElement,this.getCategories());
        }
    }

    private void applyAddUnusedVariableTransformer(CtMethod toAlter) {
        Factory factory = toAlter.getFactory();
        // Step 1: Pick a type, variable name and random value
        String nameOfVarToAdd = RandomNameFactory.getCamelcasedAnimalString(random);
        // Step 1.1: Check, if variable name is already taken, if so, redo Step 1
        List<String> existingNames = toAlter
                .filterChildren(u -> u instanceof CtVariableReference).list()
                .stream()
                .map(m -> (CtVariableReference)m)
                .map(v -> v.getSimpleName())
                .collect(Collectors.toList());
        while(existingNames.contains(nameOfVarToAdd)){
            nameOfVarToAdd = RandomNameFactory.getCamelcasedAnimalString(random);
        }
        CtTypeReference typeofVarToAdd = pickRandomSupportedType(factory);
        var valueOfVarToAdd = TransformerUtils.pickRandomElementForType(typeofVarToAdd,fullRandomStrings,random);

        // Step 2: Pick a random block of the method or the whole body otherwise
        List<CtBlock> blocks = toAlter.filterChildren(u -> u instanceof CtBlock).list()
                .stream()
                .map(b -> (CtBlock) b)
                .collect(Collectors.toList());
        int indexOfBlockToPick = random.nextInt(blocks.size());
        CtBlock blockToAddTo = blocks.get(indexOfBlockToPick);

        // Step 3: Create the local variable and add it as the first statement
        CtLocalVariable newlyCreatedVariable =
                factory.createLocalVariable(typeofVarToAdd, nameOfVarToAdd, valueOfVarToAdd);
        blockToAddTo.getStatements().add(newlyCreatedVariable);
    }

    private CtTypeReference pickRandomSupportedType(Factory factory){
        List<CtTypeReference> possibleTypes = new ArrayList<>();
        // Supported Types
        possibleTypes.add(factory.Type().INTEGER_PRIMITIVE);
        possibleTypes.add(factory.Type().INTEGER);
        possibleTypes.add(factory.Type().FLOAT_PRIMITIVE);
        possibleTypes.add(factory.Type().FLOAT);
        possibleTypes.add(factory.Type().LONG_PRIMITIVE);
        possibleTypes.add(factory.Type().LONG);
        possibleTypes.add(factory.Type().BOOLEAN_PRIMITIVE);
        possibleTypes.add(factory.Type().BOOLEAN);
        possibleTypes.add(factory.Type().STRING);
        // Pick one
        int toPick = random.nextInt(possibleTypes.size());
        return possibleTypes.get(toPick);
    }

    /**
     * Returns a random (non-empty) method of the ast.
     * Check whether ast is empty is done earlier using constraints.
     *
     * @param ast the toplevel element from which to pick a random method
     * @return a random element. Reference is passed, so altering this element will alter the toplevel ast.
     */
    private Optional<CtMethod> pickRandomMethod(CtElement ast) {
        // Check for all methods
        List<CtMethod> allMethods = ast
                .filterChildren(c -> c instanceof CtMethod)
                .list()
                .stream()
                .map(o -> (CtMethod) o)
                .collect(Collectors.toList());
        if(allMethods.size()==0)
            return Optional.empty();
        // Pick a number between 0 and count(methods)
        int randomValidIndex = random.nextInt(allMethods.size());
        // return the method at the position
        return Optional.of(allMethods.get(randomValidIndex));
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
        // With being so trivial, the compilers are very likely to throw out all useless code
        // Hence there will be no change in bytecode
        // As there is no forking (only true case) there is no controlflow change (the flow always goes one way)
        Set<TransformationCategory> categories = new HashSet<>();
        categories.add(TransformationCategory.NAMING);
        categories.add(TransformationCategory.SMELL);
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
            result = 31 * result + Boolean.hashCode(this.fullRandomStrings);
            result = 31 * result + Long.hashCode(this.seedOnCreation);
            hashCode = result;
        }
        return result;
    }

    @Override
    public boolean equals(Object o){
        if (o == this)
            return true;
        if (o instanceof AddUnusedVariableTransformer other){
            return other.triesToCompile == this.triesToCompile
                    && other.setsAutoImports == this.setsAutoImports
                    && other.fullRandomStrings == this.fullRandomStrings
                    && other.seedOnCreation == this.seedOnCreation;
        }
        return false;
    }
}
