package com.github.ciselab.lampion.transformations.transformers;

import com.github.ciselab.lampion.support.RandomNameFactory;
import com.github.ciselab.lampion.transformations.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This transformer adds a random method into a class, and invokes it at a random place in a method.
 *
 * See Tranformations.md for an example,
 * See Transformation.java for interface contract
 */
public class EmptyMethodTransformer extends BaseTransformer {
    public final String name = "EmptyMethod";

    // This List holds all by this Transformer created methods, and is used to not pick (artificial) methods created by this
    private List<CtMethod> createdMethods = new ArrayList<>();

    public EmptyMethodTransformer(){
        super();
        this.setConstraints();
    }

    public EmptyMethodTransformer(long seed){
        super(seed);
        this.setConstraints();
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

        var oToAlter = pickRandomMethod(ast);
        if(oToAlter.isEmpty()) {
            return new EmptyTransformationResult();
        }
        CtMethod toAlter = oToAlter.get();
        // As the altered method is altered forever and in all instances, safe a clone for the transformation result.
        CtMethod savedElement = toAlter.clone();
        savedElement.setParent(toAlter.getParent());
        savedElement.getParent().updateAllParentsBelow();

        applyEmptyMethodTransformer(toAlter);

        // If debug information is wished for, create a bigger Transformationresult
        // Else, just return a minimal Transformationresult
        if (debug) {
            return new SimpleTransformationResult(name,savedElement,this.getCategories(),beforeAfterOverview(savedElement,toAlter),ast.clone());
        } else {
            return new SimpleTransformationResult(name,savedElement,this.getCategories());
        }
    }

    /**
     * Creates a private method with a random name,
     * adds it to the class
     * adds a method invocation somewhere in the original method to alter
     * @param methodToAlter
     */
    private void applyEmptyMethodTransformer(CtMethod methodToAlter){
        CtClass containingClass = methodToAlter.getParent(p -> p instanceof CtClass);

        String methodName = RandomNameFactory.getRandomString();
        //Short check, redo if there is already such a method
        String finalMethodName = methodName;
        while(containingClass.getMethods().stream()
                .filter(c -> c instanceof CtMethod)
                .map(c -> (CtMethod) c)
                .anyMatch(m -> ((CtMethod<?>) m).getSimpleName().equalsIgnoreCase(finalMethodName)))
        {
            methodName = RandomNameFactory.getRandomString();
        }

        CtMethod emptyMethod = containingClass.getFactory().createMethod();
        emptyMethod.setSimpleName(methodName);
        emptyMethod.setParent(containingClass);
        // Set the same modifiers as method to alter, that helps to catch static signatures
        //emptyMethod.setModifiers(methodToAlter.getModifiers());

        emptyMethod.setType(emptyMethod.getFactory().Type().VOID_PRIMITIVE);
        emptyMethod.addModifier(ModifierKind.STATIC);
        emptyMethod.addModifier(ModifierKind.PRIVATE);
        emptyMethod.addModifier(ModifierKind.FINAL);
        emptyMethod.setBody(emptyMethod.getFactory().createBlock());


        containingClass.addMethod(emptyMethod);

        int statementsInInitialMethod = methodToAlter.getBody().getStatements().size();
        int invocationIndex = random.nextInt(statementsInInitialMethod);
        methodToAlter.getBody().addStatement(invocationIndex,
                containingClass.getFactory().createCodeSnippetStatement(methodName+"()"));

        // Sanity Check for compilation as well as restoring items
        containingClass.getFactory().getEnvironment().setAutoImports(false);
        containingClass.compileAndReplaceSnippets();
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
        categories.add(TransformationCategory.STRUCTURE);
        categories.add(TransformationCategory.NLP);
        categories.add(TransformationCategory.CONTROLFLOW);
        categories.add(TransformationCategory.NAMING);
        return categories;
    }


    /**
     * Returns a random method of the ast.
     * Check whether ast is empty is done earlier using constraints.
     *
     * @param ast the toplevel element from which to pick a random method
     * @return a random nonempty Method that was not created by this transformer. Reference is passed, so altering this element will alter the toplevel ast.
     */
    private Optional<CtMethod> pickRandomMethod(CtElement ast) {
        // Check for all methods that are not created by this transformer
        List<CtMethod> allMethods = ast
                .filterChildren(c -> c instanceof CtMethod)
                .list()
                .stream()
                .map(c -> (CtMethod) c)
                .filter(c -> ! createdMethods.contains(c))
                .filter(c -> ! c.getBody().getStatements().isEmpty())
                .collect(Collectors.toList());

        if(allMethods.isEmpty()){
            return Optional.empty();
        }

        // Pick a number between 0 and count(methods)
        int randomValidIndex = random.nextInt(allMethods.size());
        // return the method at the position
        return Optional.of(allMethods.get(randomValidIndex));
    }


    private void setConstraints(){
        Predicate<CtElement> hasMethods = c -> {
            return !c.filterChildren(ch -> ch instanceof CtMethod).list().isEmpty();
        };
        Predicate<CtElement> hasNonArtificalMethods = c -> {
            return pickRandomMethod(c) != null;
        };

        constraints.add(hasMethods);
        constraints.add(hasNonArtificalMethods);
    }


}
