open module com.github.ciselab.lampion.core {
    // Export the App to run
    // Export the engine to be able to extend it
    exports com.github.ciselab.lampion.core.program;
    // Export the transformations so one has the interfaces to build new transformers
    exports com.github.ciselab.lampion.core.transformations;
    exports com.github.ciselab.lampion.core.transformations.transformers;

    requires spoon.core;

    requires org.slf4j;

    requires java.base;
    requires java.sql;
}