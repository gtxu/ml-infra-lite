package com.example.ml.infra.core.engine;

import java.io.File;

import com.example.ml.infra.api.InferenceEngine;

public class DummyInferenceEngine implements InferenceEngine {
    private final String version;
    private final String modelPath;

    public DummyInferenceEngine(String version, String modelPath) {
        this.version = version;
        this.modelPath = modelPath;
        if (!validateFilePath(modelPath)) {
            throw new IllegalArgumentException(
                String.format("[Dummy] [Critical] Model file missing at: %s for version %s", modelPath, version)
            );
        } else {
            System.out.println("[Dummy] Engine initialized using path: " + modelPath);
        }
    }

    private boolean validateFilePath(String path) {
        File file = new File(path);
        return file.exists()
            && file.isFile()
            && file.canRead();
    }

    @Override
    public String predict(String input) {
        if (!validateFilePath(this.modelPath)) {
            throw new IllegalStateException(
                String.format("[Dummy] Inference failed: Model file for version %s is missing", this.version)
            );
        }

        // String type = input.contains(",") ? "CSV" : "Base64";

        String preview = (input.length() > 10) ? input.substring(0, 10) + "..." : input;

        return String.format("[Type: DUMMY] [Source: %s] [Model: v%s] Received input (len: %d): %s. ", 
                         new File(this.modelPath).getName(),version, input.length(), preview);
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void close() {
        System.out.println("[Dummy] Resources released for engine version: " + version);
    }
}