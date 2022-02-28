# Farewell the World, it was a nice journey.

name: str = "World"


def bye(who: str) -> str:
    return f'Goodbye {who}'


if __name__ == '__main__':
    farewell = bye(name)
    print(farewell)
