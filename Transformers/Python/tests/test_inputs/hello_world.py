# Greet the World, it deserves it.

name: str = "World"


def greet(who: str) -> str:
    return f'Hello {who}'


if __name__ == '__main__':
    greeting = greet(name)
    print(greeting)
