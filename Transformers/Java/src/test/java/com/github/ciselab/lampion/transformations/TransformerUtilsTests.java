package com.github.ciselab.lampion.transformations;

import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtTypeReference;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TransformerUtilsTests {

    @Test
    public void testGetNeutralElement_forNull_shouldThrowError(){
        CtClass ast = Launcher.parseClass("package lampion.test; \n " +
                "class A {\n " +
                "public void some() { \n" +
                "var b = null; \n"+
                "}\n" +
                "}");

        CtLiteral unsupportedLiteral =
                (CtLiteral) ast
                        .filterChildren(c -> c instanceof CtLiteral)
                        .list()
                        .get(0);

        assertThrows(UnsupportedOperationException.class,
                () -> TransformerUtils.getNeutralElement(unsupportedLiteral));
    }

    @Test
    public void testGetNeutralElement_forNonPrimitiveType_shouldGiveNull(){
        CtClass ast = Launcher.parseClass("package lampion.test; \n " +
                "class A {\n " +
                "public A some() { \n" +
                "A a = new A(); \n" +
                "return a; \n" +
                "}\n" +
                "}");

        CtVariable nonPrimitiveElement =
                (CtVariable) ast
                        .filterChildren(c -> c instanceof CtVariable)
                        .list()
                        .get(0);

        CtTypeReference<?> nonPrimitiveType = nonPrimitiveElement.getType();

        String result = TransformerUtils.getNullElement(nonPrimitiveType);
        assertEquals("null",result);
    }

    @Test
    public void testPickRandomElementForType_forNonPrimitiveType_shouldThrowError(){
        CtClass ast = Launcher.parseClass("package lampion.test; \n " +
                "class A {\n " +
                "public A some() { \n" +
                "A a = new A(); \n" +
                "return a; \n" +
                "}\n" +
                "}");
        Random random = new Random(50);


        CtVariable nonPrimitiveElement =
                (CtVariable) ast
                        .filterChildren(c -> c instanceof CtVariable)
                        .list()
                        .get(0);

        CtTypeReference<?> nonPrimitiveType = nonPrimitiveElement.getType();

        assertThrows(UnsupportedOperationException.class,
                () -> TransformerUtils.pickRandomElementForType(nonPrimitiveType,false,random));
    }

    @Test
    public void testRandomElementForType_shouldBeDeterministicWithSeed(){
        CtClass ast = Launcher.parseClass("package lampion.test; \n " +
                "class A {\n " +
                "public int some() { \n" +
                "int a = 5; \n" +
                "return a; \n" +
                "}\n" +
                "}");
        Random randomA = new Random(50);
        Random randomB = new Random(50);


        CtVariable nonPrimitiveElement =
                (CtVariable) ast
                        .filterChildren(c -> c instanceof CtVariable)
                        .list()
                        .get(0);
        CtTypeReference<?> nonPrimitiveType = nonPrimitiveElement.getType();

        var resultA = TransformerUtils.pickRandomElementForType(nonPrimitiveType,false,randomA);
        var resultB = TransformerUtils.pickRandomElementForType(nonPrimitiveType,false,randomB);

        assertEquals(resultA.toString(),resultB.toString());
    }

    @Test
    public void testRandomElementForType_shouldGiveDifferentResultsForDifferentSeeds(){
        CtClass ast = Launcher.parseClass("package lampion.test; \n " +
                "class A {\n " +
                "public int some() { \n" +
                "int a = 5; \n" +
                "return a; \n" +
                "}\n" +
                "}");
        Random randomA = new Random(20);
        Random randomB = new Random(10);


        CtVariable nonPrimitiveElement =
                (CtVariable) ast
                        .filterChildren(c -> c instanceof CtVariable)
                        .list()
                        .get(0);
        CtTypeReference<?> nonPrimitiveType = nonPrimitiveElement.getType();

        var resultA = TransformerUtils.pickRandomElementForType(nonPrimitiveType,false,randomA);
        var resultB = TransformerUtils.pickRandomElementForType(nonPrimitiveType,false,randomB);

        assertNotEquals(resultA.toString(),resultB.toString());
    }

    @Test
    public void testRandomElementForType_shouldGiveDifferentResultsForSameRandom_WhenCalledTwice(){
        CtClass ast = Launcher.parseClass("package lampion.test; \n " +
                "class A {\n " +
                "public int some() { \n" +
                "int a = 5; \n" +
                "return a; \n" +
                "}\n" +
                "}");
        Random randomA = new Random(20);


        CtVariable nonPrimitiveElement =
                (CtVariable) ast
                        .filterChildren(c -> c instanceof CtVariable)
                        .list()
                        .get(0);
        CtTypeReference<?> nonPrimitiveType = nonPrimitiveElement.getType();

        var resultA = TransformerUtils.pickRandomElementForType(nonPrimitiveType,false,randomA);
        var resultB = TransformerUtils.pickRandomElementForType(nonPrimitiveType,false,randomA);

        assertNotEquals(resultA.toString(),resultB.toString());
    }
}
