package com.example.ml.infra.core.engine;

import ai.onnxruntime.*;
import com.example.ml.infra.api.InferenceEngine;
import java.util.Collections;

public class OnnxInferenceEngine implements InferenceEngine {
    private final String version;
    private final OrtEnvironment env;
    private final OrtSession session;

    public OnnxInferenceEngine(String version, String modelPath) throws OrtException {
        this.version = version;
        this.env = OrtEnvironment.getEnvironment();
        // Create a session with default options
        this.session = env.createSession(modelPath, new OrtSession.SessionOptions());
        System.out.println("[ONNX] Engine initialized for version " + version + " from " + modelPath);
    }

    @Override
    public String predict(String input) {
        try {
            // MNIST Preprocessing: String (JSON/CSV) -> Float Array (1x1x28x28)
            float[][][][] imageData = preprocess(input);
            
            try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, imageData)) {
                OrtSession.Result results = session.run(Collections.singletonMap("input", inputTensor));
                
                // Post-processing: Get the predicted digit (argmax)
                float[][] outputLabels = (float[][]) results.get(0).getValue();
                int predictedDigit = argmax(outputLabels[0]);
                
                return String.format("[Model v%s] Predicted Digit: %d", version, predictedDigit);
            }
        } catch (Exception e) {
            return "Inference failed: " + e.getMessage();
        }
    }

    private float[][][][] preprocess(String input) {
        // Simple mock: assumes input is a comma-separated string of 784 pixels
        float[] pixels = new float[784];
        String[] parts = input.split(",");
        for (int i = 0; i < 784; i++) {
            pixels[i] = Float.parseFloat(parts[i]) / 255.0f; // Normalize
        }
        
        float[][][][] tensorData = new float[1][1][28][28];
        for (int i = 0; i < 28; i++) {
            for (int j = 0; j < 28; j++) {
                tensorData[0][0][i][j] = pixels[i * 28 + j];
            }
        }
        return tensorData;
    }

    private int argmax(float[] probabilities) {
        int maxIdx = 0;
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > probabilities[maxIdx]) {
                maxIdx = i;
            }
        }
        return maxIdx;
    }

    @Override
    public String getVersion() { return version; }

    @Override
    public void close() throws Exception {
        if (session != null) session.close();
        if (env != null) env.close();
        System.out.println("[ONNX] Resources released for version: " + version);
    }
}