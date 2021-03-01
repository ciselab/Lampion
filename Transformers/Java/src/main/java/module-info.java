open module com.github.ciselab.lampion {
    // Export the App to run
    // Export the engine to be able to extend it
    exports com.github.ciselab.lampion.program;
    // Export the transformations so one has the interfaces to build new transformers
    exports com.github.ciselab.lampion.transformations;

    requires spoon.core;


    requires org.apache.logging.slf4j;
    requires org.slf4j;

    requires org.apache.logging.log4j;
    requires java.base;
    requires java.sql;
}