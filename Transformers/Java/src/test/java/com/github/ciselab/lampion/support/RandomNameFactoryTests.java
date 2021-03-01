package com.github.ciselab.lampion.support;

import org.junit.jupiter.api.Test;

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


}
