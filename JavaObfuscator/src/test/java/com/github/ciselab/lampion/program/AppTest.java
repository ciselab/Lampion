package com.github.ciselab.lampion.program;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AppTest {
    /*
    Simply run the App to see whether the JUnit is properly configured
     */

    @Test
    public void runApp_noArgs_shouldNotBreak(){
        App.main(new String[]{});

        assertTrue(true);
    }
}
