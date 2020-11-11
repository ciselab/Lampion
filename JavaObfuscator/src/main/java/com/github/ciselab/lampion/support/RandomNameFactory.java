package com.github.ciselab.lampion.support;

import com.github.ciselab.lampion.program.App;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class provides a set of methods to create random strings of various length.
 * It will sooner or later also create random "real" words.
 */
public abstract class RandomNameFactory {

    public static long randomSeed = App.globalRandomSeed;
    public static Random random = new Random(randomSeed);

    /**
     * Sets the Seed for Random Creating to the given Value.
     * Initial value is the App's global random seed.
     *
     * @param seed
     */
    public static void setSeed(long seed) {
        RandomNameFactory.randomSeed=seed;
        RandomNameFactory.random = new Random(seed);
    }

    /**
     * This method creates a random String between 1 and 5 random words.
     * The random words are separated using a space.
     * @return a string consisting of 1 to 4 random words, separated by strings. Does not contain numbers.
     */
    public static String getRandomComment(){
        int numberofwords = 1 + random.nextInt(4);
        return getRandomComment(numberofwords);
    }

    /**
     * Creates a String with random words separated by spaces.
     * @param words number of random words in the string
     * @return a string consisting of "words" random words, separated by strings. Does not contain numbers.
     */
    public static String getRandomComment(int words ){
        // To look a bit more human, there will be spaces added between random strings
        return IntStream.range(0,words)
                .mapToObj( t -> getRandomString())
                .map(t -> (String) t)
                .collect(Collectors.joining(" "));
    }

    /**
     * @return a random, alphabetic string of length 3 to 10 that contains no numbers and no blanks
     */
    public static String getRandomString(){
        int targetStringLength = random.nextInt(7)+3;
        return getRandomAlphabeticString(targetStringLength);
    }

    /**
     * Shamelessly copied from https://www.baeldung.com/java-random-string
     * @param length number of characters in the returned String
     * @return a random, alphabetic string of length that can contain numbers and no blanks
     */
    public static String getRandomAlphaNumericString(int length){
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = random.nextInt(7)+3;

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }
    /**
     * Shamelessly copied from https://www.baeldung.com/java-random-string
     * @param length number of characters in the returned String
     * @return a random, alphabetic string of length that contains no numbers and no blanks
     */
    public static String getRandomAlphabeticString(int length){
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = random.nextInt(7)+3;

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }
}
