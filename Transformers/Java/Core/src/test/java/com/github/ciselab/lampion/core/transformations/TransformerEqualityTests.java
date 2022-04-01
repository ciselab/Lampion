package com.github.ciselab.lampion.core.transformations;

import com.github.ciselab.lampion.core.transformations.transformers.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransformerEqualityTests {
    /**
     * This class contains tests that compare different Transformers with each other.
     * I did not want to introduce Transformer B into the Transformer A Tests etc.
     * In case we remove Transformer B or change its interface, we would have to adjust Transformer A's tests.
     */

    @Test
    public void testEquality_VariousDifferentTransformers_shouldNotBeEqual(){
        Transformer t1 = new IfTrueTransformer(1);
        Transformer t2 = new IfFalseElseTransformer(1);
        Transformer t3 = new EmptyMethodTransformer(1);
        Transformer t4 = new AddUnusedVariableTransformer(1);
        Transformer t5 = new AddNeutralElementTransformer(1);
        Transformer t6 = new RandomInlineCommentTransformer(1);
        Transformer t7 = new RandomParameterNameTransformer(1);
        Transformer t8 = new RenameVariableTransformer(1);
        Transformer t9 = new LambdaIdentityTransformer(1);

        List<Transformer> transformers = new ArrayList<>();
        transformers.add(t1);
        transformers.add(t2);
        transformers.add(t3);
        transformers.add(t4);
        transformers.add(t5);
        transformers.add(t6);
        transformers.add(t7);
        transformers.add(t8);
        transformers.add(t9);

        // We expect for every transformer to have exactly one other match, which is if it sees itself
        for (Transformer tx : transformers){
            int equals = 0;
            for (Transformer ty : transformers) {
                if (tx.equals(ty)) equals++;
            }
            assertEquals(1,equals);
        }
    }



    @Test
    public void testEquality_VariousDifferentTransformers_WithDuplicates_shouldNotBeEqual(){

        List<Transformer> transformers = new ArrayList<>();
        transformers.add(new IfTrueTransformer(1));
        transformers.add(new IfFalseElseTransformer(1));
        transformers.add(new EmptyMethodTransformer(1));
        transformers.add(new AddUnusedVariableTransformer(1));
        transformers.add(new AddNeutralElementTransformer(1));
        transformers.add(new RandomInlineCommentTransformer(1));
        transformers.add(new RandomParameterNameTransformer(1));
        transformers.add(new RenameVariableTransformer(1));
        transformers.add(new LambdaIdentityTransformer(1));
        transformers.add(new IfTrueTransformer(1));
        transformers.add(new IfFalseElseTransformer(1));
        transformers.add(new EmptyMethodTransformer(1));
        transformers.add(new AddUnusedVariableTransformer(1));
        transformers.add(new AddNeutralElementTransformer(1));
        transformers.add(new RandomInlineCommentTransformer(1));
        transformers.add(new RandomParameterNameTransformer(1));
        transformers.add(new RenameVariableTransformer(1));
        transformers.add(new LambdaIdentityTransformer(1));

        // We expect for every transformer to have exactly two matches, which is if it sees itself and the copy
        for (Transformer tx : transformers){
            int equals = 0;
            for (Transformer ty : transformers) {
                if (tx.equals(ty)) equals++;
            }
            assertEquals(2,equals);
        }
    }

    @Test
    public void testEquality_VariousDifferentTransformers_WithDifferentSeeds_shouldNotBeEqual(){

        List<Transformer> transformers = new ArrayList<>();
        transformers.add(new IfTrueTransformer(1));
        transformers.add(new IfFalseElseTransformer(1));
        transformers.add(new EmptyMethodTransformer(1));
        transformers.add(new AddUnusedVariableTransformer(1));
        transformers.add(new AddNeutralElementTransformer(1));
        transformers.add(new RandomInlineCommentTransformer(1));
        transformers.add(new RandomParameterNameTransformer(1));
        transformers.add(new RenameVariableTransformer(1));
        transformers.add(new LambdaIdentityTransformer(1));

        transformers.add(new IfTrueTransformer(2));
        transformers.add(new IfFalseElseTransformer(2));
        transformers.add(new EmptyMethodTransformer(2));
        transformers.add(new AddUnusedVariableTransformer(2));
        transformers.add(new AddNeutralElementTransformer(2));
        transformers.add(new RandomInlineCommentTransformer(2));
        transformers.add(new RandomParameterNameTransformer(2));
        transformers.add(new RenameVariableTransformer(2));
        transformers.add(new LambdaIdentityTransformer(2));

        // We expect for every transformer to have exactly one matches, which is if it sees itself
        // The other transformer of the same type is not equal due to different seed
        for (Transformer tx : transformers){
            int equals = 0;
            for (Transformer ty : transformers) {
                if (tx.equals(ty)) equals++;
            }
            assertEquals(1,equals);
        }
    }
}
