package com.github.ciselab.lampion.core.transformations;

import com.github.ciselab.lampion.core.support.RandomNameFactory;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * This class holds some static functions used within the transformers.
 * At the moment, they are not strongly re-used, but I extracted them from their transformers as they were too
 * generic from where they were previously.
 *
 * All methods that take a random-element change the random in-place,
 * hence calling methods with the same random twice will give different results.
 * See the corresponding Unit-Tests for the behaviour.
 */
public class TransformerUtils {

    /**
     * This method returns a random value for a given type.
     * The supported types (and their ranges) are:
     *
     * - boolean [true,false]
     * - float [0,1]
     * - int [0,10000]
     * - long [Long.MinValue,Long.MaxValue]
     * - String [see "fullRandomStrings"]
     *
     * @param t the type for which to pick a random element.
     * @param fullRandomStrings whether to use fully random or pseudo random strings
     * @param random the random number provider of the transformer, should be seeded. Will be altered here.
     * @return a CtLiteral of the type t with a random value
     */
    public static CtLiteral pickRandomElementForType(CtTypeReference t, boolean fullRandomStrings, Random random){
        Factory factory = t.getFactory();

        switch (t.getSimpleName()) {
            case "boolean","Boolean": {
                var coin = random.nextInt(1);
                return factory.createLiteral((coin==0));
            }
            case "float","Float": {
                return factory.createLiteral(random.nextFloat());
            }
            case "int","Integer": {
                return factory.createLiteral(random.nextInt(10000));
            }
            case "Long","long": {
                return factory.createLiteral(random.nextLong());
            }
            case "String": {
                if(fullRandomStrings){
                    return factory.createLiteral(RandomNameFactory.getRandomComment(random));
                } else {
                    return factory.createLiteral(RandomNameFactory.getAnimalComment(3,random));
                }
            }
            default: {
                throw new UnsupportedOperationException("Received an Unkown Type for picking random values");
            }
        }
    }


    /**
     * This method helps with building the else block of an if-true transformations.
     * The reason for this is, that the "return null" is not sufficient for primitive datatypes.
     * "return null" is an ok statement for "Integer" but not for "int".
     *
     * This method returns
     * 0 for numeric types (.0d for double, .0f for float)
     * '\u0000' for Char
     * false for boolean
     * null for anything else
     *
     * @param type
     * @return
     */
    public static String getNullElement(CtTypeReference type){
        switch(type.getSimpleName()){
            case "byte": return "0";
            case "short": return "0";
            case "int": return "0";
            case "long": return "0L";
            case "char": return "Character.MIN_VALUE";
            case "float": return "0.0f";
            case "double": return "0.0d";
            default: return "null";
        }
    }


    /**
     * This method helps returns the neutral element for a set of supported types.
     * Some items are not that easily added in java, e.g. if one adds two chars, they become an integer (same goes for bytes).
     *
     * Here is the article on Java primitive type promotion
     * https://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.6.2
     *
     * Supported types are
     *  - String [""]
     *  - Int [0]
     *  - Float [0.0f]
     *  - Long [0.0L]
     *  - Double [0.0d]
     *
     * @param lit the literal to which to get a neutral element
     * @return
     */
    public static CtLiteral<?> getNeutralElement(CtTypedElement<?> lit){
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

    /**
     * Returns a random literal of the ast.
     * Check whether ast is empty must be done earlier using constraints / additional logic.
     *
     * @param ast the toplevel element from which to pick a random method
     * @param random the random number provider of the transformer, should be seeded. Will be altered in-place.
     * @return a random element. Reference is passed, so altering this element will alter the toplevel ast.
     */
    public static Optional<CtLiteral> pickRandomLiteral(CtElement ast, Random random) {
        // Check for all literals
        List<CtLiteral> allLiterals = ast.filterChildren(c -> c instanceof CtLiteral).list();
        if(allLiterals.size()==0)
            return Optional.empty();
        // Pick a number between 0 and count(literals)
        int randomValidIndex = random.nextInt(allLiterals.size());
        // return the literal at the position
        return Optional.of(allLiterals.get(randomValidIndex));
    }


}
