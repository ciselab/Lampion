package com.github.ciselab.lampion.core.transformations;

import com.github.ciselab.lampion.core.transformations.transformers.IfTrueTransformer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TransformerRegistryTests {

    @Test
    void testConstructor_validName_isBuildWithEmptyRegisteredItemsAndName(){
        TransformerRegistry registry = new TransformerRegistry("Test");

        assertEquals("Test", registry.name);
        assertTrue(registry.getRegisteredTransformers().isEmpty());
    }

    @Test
    void testConstructor_nullName_ThrowsException(){
        assertThrows(UnsupportedOperationException.class,
                () -> new TransformerRegistry(null)
        );
    }

    @Test
    void testConstructor_emptyName_ThrowsException(){
        assertThrows(UnsupportedOperationException.class,
                () -> new TransformerRegistry("")
        );
    }

    @Test
    void testConstructor_BlankName_ThrowsException(){
        assertThrows(UnsupportedOperationException.class,
                () -> new TransformerRegistry("  ")
        );
    }

    @Test
    void testGetRegistry_RegisterItem_askForItems_isNotEmpty(){
        IfTrueTransformer testTransformer = new IfTrueTransformer();

        TransformerRegistry registry = new TransformerRegistry("Test");

        registry.registerTransformer(testTransformer);

        assertFalse(registry.getRegisteredTransformers().isEmpty());
    }

    @Test
    void testGetRegistry_RegisterItem_askForItemsWithExistingCategory_isNotEmpty(){
        IfTrueTransformer testTransformer = new IfTrueTransformer();

        TransformerRegistry registry = new TransformerRegistry("Test");

        registry.registerTransformer(testTransformer);

        var results = registry.getRegisteredTransformersWithCategory(TransformationCategory.SMELL);

        assertFalse(results.isEmpty());
    }

    @Test
    void testGetRegistry_RegisterItem_askForItemsWithNonRegisteredCategory_isNotEmpty(){
        IfTrueTransformer testTransformer = new IfTrueTransformer();

        TransformerRegistry registry = new TransformerRegistry("Test");

        registry.registerTransformer(testTransformer);

        var results = registry.getRegisteredTransformersWithCategory(TransformationCategory.NLP);

        assertTrue(results.isEmpty());
    }
}
