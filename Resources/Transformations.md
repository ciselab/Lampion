# Transformations

This Markdown-File holds a set of metamorphic transformations of code on the example of java code.
Many of these Examples can be considered *obfuscations* or *smells*.

The intent of this document is to provide an overview of the implemented (or to be implemented) metamorphic Transformations,
their context, restrictions and an example to ease writing tests.

They are categorized by their primary category.

## Structure Related

### IfWrapper

Wrap the whole body of a method into a `if(true){body}`

Example:

```Java
// before
public void some (String text) {
    System.out.println(text);
}
// after
public void some (String text) {
    if (true) {
        System.out.println(text);
    }
}
```

If the method has a return statement, to produce valid code it is necessary to have a return statement in the other path. 
Yes, this is trivial and likely your IDE will detect / change / warn about this, but some compilers will be harsh on you.
Another issue is, that *primitive datatypes* cannot return null, so "int" returning methods return 0, while "char" returning methods return `Char.empty()`. Every object-returning method returns `null`.

```Java
// before
public int sum(int a, int b) {
    return a + b;
}
// after
public int sum(int a, int b) {
    if (true) {
        return a + b;
    } else {
        return 0;
    }
}
```

Excludes: None

Variations: Add tautologic expressions instead of `true`, e.g. `1==1` or `"a".equals("a")`, invoke a method that returns true

Alters Bytecode: No

Is a Smell: Yes

Categories: Structure, Control-Flow, Layout, Complexity

## Unused Items Related

### UnusedParameter

Add a unused parameter to a method.

Risks: Collision with existing parameters / variables, breaking interfaces

Example:

```Java
// before
public void sum (int a, int b) {
    return a + b;
}
// after
public void sum (int a, int b, int c) {
    return a + b;
}
```

Excludes: None

Variations: Add parameters with names similar to existing parameters/variables

Alters Bytecode: Yes

Is a Smell: Yes

Categories: Complexity, NLP

### Empty Method Invocation

Add a call to a useless / unimportant method

Risks: Collision with method names, Risk of alternating the newly introduced method instead of the actual system under test.

Example:

```Java
// before
public void sum (int a, int b) {
    return a + b;
}

// after
public void sum (int a, int b) {
    someOtherMethod();
    return a + b;
}

private static void someOtherMethod() {
    // Do nothing
}
```

Excludes: None

Variations: let the method use existing parameters / variables

Alters Bytecode: Yes

Is a Smell: Yes

Categories: Complexity, NLP, ControlFlow

## Comment Related

### NewInlineComment

Add a Inline Comment before a (random) Statement

Risks: None

Example:

```Java
// before
public void sum (int a, int b) {
    return a + b;
}
// after
public void sum (int a, int b) {
    // Lorem Ipsum
    return a + b;
}
```

Excludes: None

Variations: Use a String that looks like a password, looks like Code, looks like english language

Alters Bytecode: No

Is a Smell: No

Categories: Layout, NLP, Comment

## Naming Related

### RenameParameter

Alters the name of a parameter in all occurrences of a single method.

Risks: Collisions with other variable names

Example:

```Java
// before
public void sum (int a, int b) {
    return a + b;
}
// after
public void sum (int alteredA, int b) {
    return alteredA + b;
}
```

Excludes: Itself

Variations: Hash the name, switch the variable names, use meaningful but random english words (e.g. change node to user, user to list etc.)

Alters Bytecode: No

Is a Smell: No

Categories: NLP, Variables

## Lambda Related

### LiteralIdentityWrapper

Wrap a literal into a lambda-function + it's invocation.

Risks: None

```Java
// before
public void plusOne (int a) {
    return a + 1;
}
// (intended) after
public void plusOne (int a) {
    return a + (() -> 1).supply();
}
```

Atleast, this was the idea. However, due to Java-Compilation Problems, 
it is a bit uglier: 

```Java
// actual after
public void plusOne (int a) {
    return a = ((int)(java.util.function.Supplier<?>)(()->1).get());
}
```

Note: This transformation requires Java to be version 8 or higher.

Excludes: None

Variations: Wrap function invocations too

Alters Bytecode: Likely

Is a Smell: Somewhat

Categories: NLP, Structure, Complexity
