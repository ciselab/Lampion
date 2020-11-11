package com.github.ciselab.lampion.regression;
import com.github.ciselab.lampion.transformations.Transformer;
import com.github.ciselab.lampion.transformations.transformers.*;
import org.junit.jupiter.api.*;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This Set of Tests shall help to keep found bugs under regression.
 * They are too vague to be put into a single other testclass at the moment
 *
 * Also, the bug itself is reported making these tests rather noisy and long.
 */
public class RegressionTests {

    @Tag("Regression")
    @RepeatedTest(10)
    void testEmptyMethodTransformerAndIfTrueTransformer_MethodBodyShouldNotBeDuplicated_UsingRandomizedTransformers(){
        // There was an issue when running the App with both EmptyMethod and IfTrue Transformer
        // For no apparent reason, the method body was duplicated
        // With the duplicated code, the second if-block was "Unreachable Code" which did not compile
        // Throwing Errors

        /*
        Example of Bad Code:
         package lampion.test.examples;class Example2 {
            public int mult(int bjwyc, int vumyzolxx) {
                if (true) {
                    lampion.test.examples.Example2.cerv();
                    // oob
                    // mswyi lhrqsz favzoywm
                    if (true) {
                        if (true) {
                            if (true) {
                                lampion.test.examples.Example2.wfrfdqy();
                                lampion.test.examples.Example2.eytuvj();
                                return bjwyc * vumyzolxx;
                            } else {
                                return 0;
                            }
                        } else {
                            return 0;
                        }
                    } else {
                        return 0;
                    }
                } else {
                    return 0;
                }
            }
            private void wfrfdgy(){}
            private void eytuvj(){}
         */

        /*
        Issue Diagnosis:

        Just using IfTrue and EmptyMethod did not cause errors on any repeated runs.
        It turned out to be "EmptyMethod" + "RandomInlineComment". See
            testEmptyMethodTransformerAndIfTrueTransformer_MethodBodyShouldNotBeDuplicated
        For minimal precise non random reproduction.
         */

        String initialClass = "package lampion.test.examples; class A { public int mult(int a, int b){return a * b;}";

        CtClass<?> ast = Launcher.parseClass(initialClass);

        EmptyMethodTransformer methodTrans = new EmptyMethodTransformer();
        IfTrueTransformer ifTrueTrans = new IfTrueTransformer();
        // RandomInlineCommentTransformer commentTrans = new RandomInlineCommentTransformer();
        RandomParameterNameTransformer paramTrans = new RandomParameterNameTransformer();
        LambdaIdentityTransformer lambdaTrans = new LambdaIdentityTransformer();

        Transformer[] transformers = new Transformer[]{
                 methodTrans
                ,ifTrueTrans
                //,commentTrans
                ,paramTrans
                ,lambdaTrans
        };

        Random r = new Random();
        for(int i=0;i<15;i++) {
            int random_index = r.nextInt(transformers.length);
            transformers[random_index].applyAtRandom(ast);
        }
        //fail();
        return;
    }

    //TODO: this must be fixed in the Library
    /*
    @Ignore
    @Tag("Exploration")
    @Tag("Regression")
    @RepeatedTest(5)
    void testEmptyMethodTransformerAndIfTrueTransformer_MethodBodyShouldNotBeDuplicated(){
        // This is the minimal reproduction of the above

        String initialClass = "package lampion.test.examples; class A { public int mult(int a, int b){return a * b;}";

        CtClass<?> ast = Launcher.parseClass(initialClass);

        EmptyMethodTransformer methodTrans = new EmptyMethodTransformer();
        RandomInlineCommentTransformer commentTrans = new RandomInlineCommentTransformer();

        commentTrans.applyAtRandom(ast);
        methodTrans.applyAtRandom(ast);
        commentTrans.applyAtRandom(ast);
        //fail();
        return;
    }

    //TODO: this must be fixed in the Library
    @Ignore
    @Tag("Exploration")
    @Tag("Regression")
    @Test
    void testSnippetReplacement_BlockHasIssuesWithNextAfterComment(){
        String initialClass = "package some; class A { public int mult(int a, int b){return a * b;}";

        CtClass<?> containingClass = Launcher.parseClass(initialClass);
        String methodName = "someMethod";

        var methodToAlter= containingClass.getMethodsByName("mult").get(0);

        // Build an empty method, return void
        CtMethod emptyMethod = containingClass.getFactory().createMethod();
        emptyMethod.setSimpleName(methodName);
        emptyMethod.setParent(containingClass);
        emptyMethod.setType(emptyMethod.getFactory().Type().VOID_PRIMITIVE);
        emptyMethod.addModifier(ModifierKind.PRIVATE);
        emptyMethod.setBody(emptyMethod.getFactory().createBlock());

        containingClass.addMethod(emptyMethod);

        methodToAlter.getBody().addStatement(0,
                containingClass.getFactory().createBlock().addStatement( containingClass.getFactory().createCodeSnippetStatement(methodName+"()"))
               );

        methodToAlter.getBody().addStatement(0,containingClass.getFactory().createComment("I seem to break the test", CtComment.CommentType.BLOCK));

        // This fails the test
        containingClass.compileAndReplaceSnippets();

        return;
    }
    */
}
