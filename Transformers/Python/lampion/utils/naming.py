import random
import string


def get_random_string(length: int) -> str:
    if length < 1:
        raise ValueError("Random Strings must have length 1 minimum.")
    # choose from all lowercase letter
    letters = string.ascii_letters + string.digits
    first_letter = random.choice(string.ascii_lowercase)
    result_str = ''.join(random.choice(letters) for i in range(length - 1))
    return first_letter + result_str

def get_pseudo_random_string(with_keyword: bool = True,with_adjective: bool = True, with_animal: bool = True, with_job: bool = True) -> str:
    # Helper Variable to keep track if we have a starting element and need "_" as glue
    has_already_elements: bool = False
    # Helper to store the final result
    result = ""

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

"""
===================================================================================================================
                                   String Arrays
      Below this line are just dictionaries of names similar to docker's container names
      Used for Pseudo Random String Generation, Which looks nicer than full random
===================================================================================================================
 """

adjectives = ["aged","biased","complex","destructive","efficient",
        "frugal","great","honorable","iterative",
        "joking","kinky","lazy","mighty",
        "naughty","obsolete","perfect","quick",
        "rural","simple","touching","urban","verbose",
        "wonderful","xenophobe","yummy","zoomed" ]

animals = [
        "alpaca","beaver","cockroach","dragon","eagle",
        "fish","goofer","hippo","ibex",
        "jellyfish","kraken","lux","minks",
        "narwhal","okapi","python","quetzal",
        "raccoon","starfish","tapir","unicorn",
        "vulture","wale","yak","zebra" ]

keywords = ["from","is","to","get","set",
        "equals","swap","generate","compare",
        "delete","write","save","load","store",
        "print","start","stop","test","run",
        "stream","catch","throw" ]

jobs = ["attorney","builder","curator","dean","engineer",
        "firefighter","gourmet","hitchhiker","influencer",
        "judge","killer","landlord","musician",
        "nurse","operator","professor","quartermaster",
        "redactor","sergeant","teacher","urologist",
        "veterinarian","waiter","youtuber","zookeeper" ]