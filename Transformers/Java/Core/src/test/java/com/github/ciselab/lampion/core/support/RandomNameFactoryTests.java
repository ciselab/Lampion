package com.github.ciselab.lampion.core.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class RandomNameFactoryTests {

    @Test
    void getAnimalNameCamelCased_withKeyword_has3uppercasedLetters(){
        Random r = new Random(1);

        String animalName = RandomNameFactory.getCamelcasedAnimalString(true,r);

        long uppercases = animalName.codePoints().filter(c-> c>='A' && c<='Z').count();

        assertEquals(3,uppercases);
    }

    @Test
    void getAnimalNameCamelCased_withoutKeyword_has2uppercasedLetters(){
        Random r = new Random(1);

        String animalName = RandomNameFactory.getCamelcasedAnimalString(r);

        long uppercases = animalName.codePoints().filter(c-> c>='A' && c<='Z').count();

        assertEquals(2,uppercases);
    }

    @Test
    void getAnimalNameSnakeCased_withKeyword_has3LowerscoreLetters(){
        Random r = new Random(1);

        String animalName = RandomNameFactory.getSnakeCasedAnimalString(true,r);

        long uppercases = animalName.codePoints().filter(c-> c=='_').count();

        assertEquals(3,uppercases);
    }

    @Test
    void getAnimalNameSnakeCased_withoutKeyword_has2LowerscoreLetters(){
        Random r = new Random(1);

        String animalName = RandomNameFactory.getSnakeCasedAnimalString(r);

        long uppercases = animalName.codePoints().filter(c-> c=='_').count();

        assertEquals(2,uppercases);
    }


    @Test
    void getVeryLongAlphaNumeric_shouldHaveNumber(){
        Random r = new Random(1);

        String alphanumeric = RandomNameFactory.getRandomAlphaNumericString(500,r);

        char[] chars = alphanumeric.toCharArray();
        boolean seenDigit = false;
        for (char c : chars){
            seenDigit = seenDigit || Character.isDigit(c);
        }

        assertTrue(seenDigit);
    }

    @Test
    void getVeryLongAlphaNumeric_shouldHaveLowerCaseLetter(){
        Random r = new Random(1);

        String alphanumeric = RandomNameFactory.getRandomAlphaNumericString(500,r);

        char[] chars = alphanumeric.toCharArray();
        boolean seenLowercase = false;
        for (char c : chars){
            seenLowercase = seenLowercase || Character.isLowerCase(c);
        }

        assertTrue(seenLowercase);
    }


    @Test
    void getVeryLongAlphaNumeric_shouldHaveUpperCaseLetter(){
        Random r = new Random(1);

        String alphanumeric = RandomNameFactory.getRandomAlphaNumericString(500,r);

        char[] chars = alphanumeric.toCharArray();
        boolean seenUppercase = false;
        for (char c : chars){
            seenUppercase = seenUppercase || Character.isUpperCase(c);
        }

        assertTrue(seenUppercase);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 5, 15, 400}) // six numbers
    void getVeryLongAlphaNumeric_shouldHaveSpecifiedLength(int l){
        Random r = new Random(1);

        String alphanumeric = RandomNameFactory.getRandomAlphaNumericString(l,r);

        assertEquals(l,alphanumeric.length());
    }
}
