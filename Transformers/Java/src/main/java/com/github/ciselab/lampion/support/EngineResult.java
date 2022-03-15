package com.github.ciselab.lampion.support;

import com.github.ciselab.lampion.transformations.TransformationResult;
import com.github.ciselab.lampion.transformations.Transformer;
import com.github.ciselab.lampion.transformations.TransformerRegistry;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import spoon.reflect.CtModel;

/**
 * This is a support class for the run method in Engine.java.
 * This result includes all relevant fields that we need for writing to file in the App class.
 */
public class EngineResult {

    protected CtModel codeRoot;
    protected List<TransformationResult> transformationResults;
    protected String outputDirectory;
    protected Boolean writeJavaOutput;
    protected long transformationFailures;

    /**
     * Builder design pattern for the EngineResult.
     */
    public static class Builder {
        private CtModel codeRoot;
        private List<TransformationResult> transformationResults;
        private long totalTransformations;
        private long transformationFailures;
        private Random random;
        private String outputDirectory;
        private String codeDirectory;
        private TransformerRegistry transformerRegistry;
        private Map<Transformer,Integer> distribution;
        private Boolean writeJavaOutput;

        public Builder(CtModel codeRoot, String codeDirectory, String outputDirectory, TransformerRegistry transformerRegistry) throws UnsupportedOperationException {
            if(codeRoot == null)
                throw new UnsupportedOperationException("The CtModel cannot be null");
            if(transformerRegistry == null)
                throw new UnsupportedOperationException("The transformer registry cannot be null");
            this.codeRoot = codeRoot;
            this.codeDirectory = codeDirectory;
            this.outputDirectory = outputDirectory;
            this.transformerRegistry = transformerRegistry;
        }

        public Builder transformationResults(List<TransformationResult> val) {
            transformationResults = val;
            return this;
        }

        public Builder totalTransformations(long val) {
            totalTransformations = val;
            return this;
        }

        public Builder transformationFailures(long val) {
            transformationFailures = val;
            return this;
        }

        public Builder randomSeed(Random val) {
            random = val;
            return this;
        }

        public Builder distribution(Map<Transformer,Integer> val) {
            distribution = val;
            return this;
        }

        public Builder javaOutput(Boolean val) {
            writeJavaOutput = val;
            return this;
        }

        public EngineResult build() {
            return new EngineResult(this);
        }
    }

    private EngineResult(Builder builder) {
        this.codeRoot = builder.codeRoot;
        this.outputDirectory = builder.outputDirectory;
        this.transformationResults = builder.transformationResults;
        this.writeJavaOutput = builder.writeJavaOutput;
        this.transformationFailures = builder.transformationFailures;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public CtModel getCodeRoot() {
        return codeRoot;
    }

    public List<TransformationResult> getTransformationResults() {
        return transformationResults;
    }

    public Boolean getWriteJavaOutput() {
        return writeJavaOutput;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EngineResult that = (EngineResult) o;
        return Objects.equals(codeRoot, that.codeRoot) && Objects.equals(transformationResults, that.transformationResults);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codeRoot, transformationResults);
    }

    @Override
    public String toString() {
        return "EngineResult{" +
                "codeRoot=" + codeRoot +
                ", transformationResults=" + transformationResults +
                ", outputDirectory='" + outputDirectory + '\'' +
                '}';
    }
}
