package com.github.ciselab.lampion.support;

import com.github.ciselab.lampion.program.App;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class provides a set of methods to create random strings of various length.
 * It will sooner or later also create random "real" words.
 *
 * To be better testable, the Factory methods receive the random supplier from the transformers.
 * As every transformer needs a random supplier anyway, this way round there is less need for seeding
 * and the unit tests are more stable/decoupled.
 */
public abstract class RandomNameFactory {

    /**
     * This method creates a random String between 1 and 5 random words.
     * The random words are separated using a space.
     * @return a string consisting of 1 to 4 random words, separated by strings. Does not contain numbers.
     */
    public static String getRandomComment(Random random){
        int numberofwords = 1 + random.nextInt(4);
        return getRandomComment(numberofwords,random);
    }

    /**
     * Creates a String with random words separated by spaces.
     * @param words number of random words in the string
     * @return a string consisting of "words" random words, separated by strings. Does not contain numbers.
     */
    public static String getRandomComment(int words,Random random){
        // To look a bit more human, there will be spaces added between random strings
        return IntStream.range(0,words)
                .mapToObj( t -> getRandomString(random))
                .collect(Collectors.joining(" "));
    }

    /**
     * @return a random, alphabetic string of length 3 to 10 that contains no numbers and no blanks
     */
    public static String getRandomString(Random random){
        int targetStringLength = random.nextInt(7)+3;
        return getRandomAlphabeticString(targetStringLength,random);
    }

    /**
     * Shamelessly copied from https://www.baeldung.com/java-random-string
     * @param length number of characters in the returned String
     * @return a random, alphabetic string of length that can contain numbers and no blanks
     */
    public static String getRandomAlphaNumericString(int length,Random random){
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
    public static String getRandomAlphabeticString(int length,Random random){
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

    /**
     * Returns a less-random, verbatim Animal String similar to docker container names.
     * It is camel cased.
     * Expected output range is ~ 25 x 25 x 25 possible strings.
     *
     * @param r a random number provider
     * @return a snake cased string such as setNaughtyBeaverAttorney or honorableKrakenZookeeper
     */
    public static String getCamelcasedAnimalString(Random r){
      return getCamelcasedAnimalString(false,r);
    }

    /**
     * Returns a less-random, verbatim Animal String similar to docker container names.
     * It is camel cased.
     * Expected output range is ~ 25 x 25 x 25 (x 10 if you have keywords on) possible strings.
     *
     * @param withKeyWord whether one of the keywords such as "get","set","compare"... should be added
     * @param r a random number provider
     * @return a snake cased string such as setNaughtyBeaverAttorney or honorableKrakenZookeeper
     */
    public static String getCamelcasedAnimalString(boolean withKeyWord,Random r){
        String keyword = withKeyWord ? keywords[r.nextInt(keywords.length)] : "";
        String adjective = adjectives[r.nextInt(adjectives.length)];
        if(withKeyWord){
            adjective = uppercaseFirstLetter(adjective);
        }
        String animal = uppercaseFirstLetter(animals[r.nextInt(animals.length)]);
        String job = uppercaseFirstLetter(jobs[r.nextInt(jobs.length)]);

        return keyword+adjective+animal+job;
    }

    /**
     * Returns a less-random, verbatim Animal String similar to docker container names.
     * It is snake cased.
     * Expected output range is ~ 25 x 25 x 25 possible strings.
     *
     * @param r a random number provider
     * @return a snake cased string such as happy_orangutan_landlord
     */
    public static String getSnakeCasedAnimalString(Random r){
        return getSnakeCasedAnimalString(false,r);
    }

    /**
     * Returns a less-random, verbatim Animal String similar to docker container names.
     * It is snake cased.
     * Expected output range is ~ 25 x 25 x 25 (x 10 if you have keywords on) possible strings.
     *
     * @param withKeyWord whether one of the keywords such as "get","set","compare"... should be added
     * @param r a random number provider
     * @return a snake cased string such as get_free_orca_lawyer or happy_orangutan_landlord
     */
    public static String getSnakeCasedAnimalString(boolean withKeyWord,Random r){
        String keyword = withKeyWord ? keywords[r.nextInt(keywords.length)]+"_" : "";
        String adjective = adjectives[r.nextInt(adjectives.length)];
        String animal = animals[r.nextInt(animals.length)];
        String job = jobs[r.nextInt(jobs.length)];

        return keyword+adjective+"_"+animal+"_"+job;
    }

    private static String uppercaseFirstLetter(String str){
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /*
    ===================================================================================================================
                                       String Arrays
                          Below this line are just dictionaries of names similar to docker
    ===================================================================================================================
     */


    /**
     * These keywords are commonly found as prefixes of methods
     */
    private static final String[] keywords = new String[]{
            "from",
            "is",
            "to",
            "get",
            "set",
            "equals",
            "swap",
            "generate",
            "compare",
            "delete",
            "write",
    };

    private static final String[] adjectives = new String[]{
            "aged",
            "biased",
            "complex",
            "destructive",
            "efficient",
            "frugal",
            "great",
            "honorable",
            "iterative",
            "joking",
            "kinky",
            "lazy",
            "mighty",
            "naughty",
            "obsolete",
            "perfect",
            "quick",
            "rural",
            "simple",
            "touching",
            "urban",
            "verbose",
            "wonderful",
            "xenophobe",
            "yummy",
            "zoomed"
    };

    private static final String[] animals = new String[]{
            "alpaca",
            "beaver",
            "cockroach",
            "dragon",
            "eagle",
            "fish",
            "goofer",
            "hippo",
            "ibex",
            "jellyfish",
            "kraken",
            "lux",
            "minks",
            "narwhal",
            "okapi",
            "python",
            "quetzal",
            "raccoon",
            "starfish",
            "tapir",
            "unicorn",
            "vulture",
            "wale",
            //"x", //There is no good animal with x :(
            "yak",
            "zebra"
    };

    private static final String[] jobs = new String[]{
            "attorney",
            "builder",
            "curator",
            "dean",
            "engineer",
            "firefighter",
            "gourmet",
            "hitchhiker",
            "influencer",
            "judge",
            "killer",
            "landlord", //Actually, Landlord is not a job, they are just filthy capitalists leeching from society
            "musician",
            "nurse",
            "operator",
            "professor",
            "quartermaster",
            "redactor",
            "sergeant",
            "teacher",
            "urologist",
            "veterinarian",
            "waiter",
            //"x", //There is no good job with x :(
            "youtuber",
            "zookeeper"
    };

}
