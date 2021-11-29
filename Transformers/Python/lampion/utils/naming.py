"""
Contains shared utils for naming,
mostly about creating random and pseudo random strings.

Also contains the dictionaries for creating the pseudo random strings
(matching the ones used for the Java Transformer).
"""
import random
import string


def get_random_string(length: int) -> str:
    """
    Returns a random string starting with a lower-case letter.
    Later parts can contain numbers, lower- and uppercase letters.

    Note: Random Seed should be set somewhere in the program!
    :param length: How long the required string must be. length > 0 required.
    :return: a randomly created string
    :raises: ValueError for zero and negative length
    """
    if length < 1:
        raise ValueError("Random Strings must have length 1 minimum.")
    # choose from all lowercase letter
    letters = string.ascii_letters + string.digits
    first_letter = random.choice(string.ascii_lowercase)
    result_str = ''.join(random.choice(letters) for i in range(length - 1))
    return first_letter + result_str


def get_pseudo_random_string(
        with_keyword: bool = True, with_adjective: bool = True, with_animal: bool = True, with_job: bool = True) -> str:
    """
    Returns a pseudo random string containing keywords, animal-names, adjectives and job-names.
    Results look for example like:

    >>> get_pseudo_random_string()
    >>> get_important_dolphin_lawyer

    The key words are separated by underscores ("_") and there cannot be leading underscore.
    There is one word in the dictionary for every letter, except for "x" (because that is a hard letter).

    :param with_keyword: whether or not to add a keyword like "get","store","save", etc.
    :param with_adjective: whether or not to add adjectives like "intelligent","sneaky", etc.
    :param with_animal: whether or not to add a animal name, like "beaver","octopus", etc.
    :param with_job: whether or not to add a job-name, like "lawyer", "doctor", "programmer", etc.
    :return: a pseudo random string based of randomly drawing keywords. Joined by underscore.
    :raises: ValueError if all options were turned off.
    """
    # Helper Variable to keep track if we have a starting element and need "_" as glue
    has_already_elements: bool = False
    # Helper to store the final result
    result = ""

    if not (with_keyword or with_animal or with_job or with_adjective):
        raise ValueError("All Options for get_pseudo_random_string have been turned off!")

    if with_keyword:
        result = result + random.choice(keywords)
        has_already_elements = has_already_elements or True

    if with_adjective:
        if has_already_elements:
            result = result + "_"
        result = result + random.choice(adjectives)
        has_already_elements = has_already_elements or True

    if with_animal:
        if has_already_elements:
            result = result + "_"
        result = result + random.choice(animals)
        has_already_elements = has_already_elements or True

    if with_job:
        if has_already_elements:
            result = result + "_"
        result = result + random.choice(jobs)

    return result


#===================================================================================================================
#                                   String Arrays
#      Below this line are just dictionaries of names similar to docker's container names
#      Used for Pseudo Random String Generation, Which looks nicer than full random
#===================================================================================================================

adjectives = ["aged", "biased", "complex", "destructive", "efficient",
              "frugal", "great", "honorable", "iterative",
              "joking", "kinky", "lazy", "mighty",
              "naughty", "obsolete", "perfect", "quick",
              "rural", "simple", "touching", "urban", "verbose",
              "wonderful", "xenophobe", "yummy", "zoomed"]

animals = [
    "alpaca", "beaver", "cockroach", "dragon", "eagle",
    "fish", "goofer", "hippo", "ibex",
    "jellyfish", "kraken", "lux", "minks",
    "narwhal", "okapi", "python", "quetzal",
    "raccoon", "starfish", "tapir", "unicorn",
    "vulture", "wale", "yak", "zebra"]

keywords = ["from", "is", "to", "get", "set",
            "equals", "swap", "generate", "compare",
            "delete", "write", "save", "load", "store",
            "print", "start", "stop", "test", "run",
            "stream", "catch", "throw"]

jobs = ["attorney", "builder", "curator", "dean", "engineer",
        "firefighter", "gourmet", "hitchhiker", "influencer",
        "judge", "killer", "landlord", "musician",
        "nurse", "operator", "professor", "quartermaster",
        "redactor", "sergeant", "teacher", "urologist",
        "veterinarian", "waiter", "youtuber", "zookeeper"]
