package com.example.ml.infra.api;

/**
 * The abstraction for all inference implementations.
 * Decouples the HTTP layer from specific ML frameworks.
 * Using an interface allows us to swap DummyEngine with real ONNX/DL4J engines.
 */
public interface InferenceEngine {
    String predict(String input);
    String getVersion();
    // TODO: Add a close() method for native memory cleanup
}