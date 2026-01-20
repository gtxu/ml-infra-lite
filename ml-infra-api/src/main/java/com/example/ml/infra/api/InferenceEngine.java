package com.example.ml.infra.api;

/**
 * The abstraction for all inference implementations.
 * Decouples the HTTP layer from specific ML frameworks.
 * Using an interface allows us to swap DummyEngine with real ONNX/DL4J engines.
 */
public interface InferenceEngine extends AutoCloseable{
    String predict(String input);
    String getVersion();
    
    @Override
    default void close() throws Exception {
        // No-op by default
    }
}