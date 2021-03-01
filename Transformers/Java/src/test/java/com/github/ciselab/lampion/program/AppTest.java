package com.github.ciselab.lampion.program;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AppTest {
    // TODO: Fill me again !

    /*
    @Test
    public void runApp_noArgs_shouldNotBreak(){
        App.main(new String[]{});

        assertTrue(true);
    }
    */

    @Tag("Regression")
    @Test
    void testDefaultRegistry_ShouldNotBeEmpty(){
        /*
        This issue occurred due to an issue with the Java runtime.
        The transformers register their delegate at "static"-time, that is when the class is loaded.
        However, the class is only loaded when needed.
        Without further measures taken, this leads to an empty registry at runtime.

        For this issue, this simple test is added and should always succeed while relying on the default registry!
         */
        assertFalse(App.globalRegistry.getRegisteredTransformers().isEmpty());
    }

}
