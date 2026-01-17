package com.example.ml.infra.core.engine;

import com.example.ml.infra.api.InferenceEngine;

public class DummyInferenceEngine implements InferenceEngine {
    private final String version;

    public DummyInferenceEngine(String version) {
        this.version = version;
    }

    @Override
    public String predict(String input) {
        return "Prediction result [Model v" + version + "]: " + input.toUpperCase();
    }

    @Override
    public String getVersion() {
        return version;
    }
}