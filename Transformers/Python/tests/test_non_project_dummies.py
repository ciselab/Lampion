# A very simple file just to see that I can run tests and setup a Pypeline (haha)
# Everything taken from https://docs.pytest.org/en/6.2.x/getting-started.html#getstarted
# Everything with test_ or Test as Prefix is run. tests_ does not work!!
import pytest


def inc(x):
    return x + 1


def test_answer():
    assert inc(3) != 5


def test_answer2():
    assert inc(-1) == 0


def f():
    raise SystemExit(1)


def test_mytest():
    with pytest.raises(SystemExit):
        f()

class TestClass:
    def test_one(self):
        x = "this"
        assert "h" in x
