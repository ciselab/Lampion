package com.github.ciselab.lampion.core.transformations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The TransformerRegistry is an element to store and categorize transformers.
 *
 * This Registry can either be instantiated and filled "manually" on demand, such to build specific scenarios or tests,
 * or there is a singleton instance in "App.java" on which every Transformer registers a single instance on startup.
 *
 * For further discussion on the topic, see: DesignNotes.md "Registration of Transformations"
 */
public class TransformerRegistry {

    private List<Transformer> registeredTransformers;   // All existing and registered, fully functioning transformers
    public final String name;                           // A (unique) name for the registry, should reflect the transformers stored

    /**
     * Simple constructor which initializes a registry with no transformers.
     *
     * @param name a name for this registry, should reflect the contents of the transformers.
     *
     * @throws UnsupportedOperationException when an empty, blank or null name is entered
     */
    public TransformerRegistry(String name) {
        if (name == null || name.isEmpty() || name.isBlank()) {
            throw new UnsupportedOperationException("Name for Registry cannot be null, empty or blank.");
        }
        this.name = name;
        registeredTransformers = new ArrayList<Transformer>();
    }

    /**
     * Stores a Transformer in the registry.
     *
     * @param toRegister the Transformer to be registered
     * @return true if the transformer was successfully registered, false otherwise
     */
    public boolean registerTransformer(Transformer toRegister) {
        return registeredTransformers.add(toRegister);
    }

    /**
     * Getter for the hidden Transformers.
     *
     * @return the registered Transformers. List can be empty.
     */
    public List<Transformer> getRegisteredTransformers() {
        return registeredTransformers;
    }

    /**
     * Searches the registered Transformers for any that have the requested category.
     *
     * @param category the category to look for
     * @return any registered transformer with the requested category. List can be empty.
     */
    public List<Transformer> getRegisteredTransformersWithCategory(TransformationCategory category) {
        return this.getRegisteredTransformers()
                .stream()
                .filter(t -> t.getCategories().contains(category))
                .collect(Collectors.toList());
    }

}
