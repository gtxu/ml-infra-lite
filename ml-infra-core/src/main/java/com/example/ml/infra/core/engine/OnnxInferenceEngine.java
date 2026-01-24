package com.example.ml.infra.core.engine;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Collections;

import javax.imageio.ImageIO;

import com.example.ml.infra.api.InferenceEngine;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

// ONNX Inference Engine for MNIST digit recognition
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

            float[][][][] imageData = preprocessBase64(input);
            
            try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, imageData)) {
                OrtSession.Result results = session.run(Collections.singletonMap("input", inputTensor));
                
                // Post-processing: Get the predicted digit (argmax)
                float[][] outputLabels = (float[][]) results.get(0).getValue();
                int predictedDigit = argmax(outputLabels[0]);
                
                return String.format("[ONNX] [Model v%s] Predicted Digit: %d", version, predictedDigit);
            }
        } catch (Exception e) {
            return "[ONNX] [Error] Inference failed: " + e.getMessage();
        }
    }

    // private float[][][][] preprocessCsv(String input) {
    //     // Simple mock: assumes input is a comma-separated string of 784 pixels
    //     float[] pixels = new float[784];
    //     String[] parts = input.split(",");
    //     for (int i = 0; i < 784; i++) {
    //         pixels[i] = Float.parseFloat(parts[i]) / 255.0f; // Normalize
    //     }
        
    //     float[][][][] tensorData = new float[1][1][28][28];
    //     for (int i = 0; i < 28; i++) {
    //         for (int j = 0; j < 28; j++) {
    //             tensorData[0][0][i][j] = pixels[i * 28 + j];
    //         }
    //     }
    //     return tensorData;
    // }

    // private float[][][][] preprocess(String input) throws Exception {
    //     if (!input.contains(",")) {
    //         return preprocessBase64(input);
    //     }
    //     return preprocessCsv(input);
    // }

    private float[][][][] preprocessBase64(String base64Str) throws Exception {
        // 1. Decode Base64 string
        byte[] imageBytes = Base64.getDecoder().decode(base64Str);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));

        // 2. Resize to 28x28 and convert to Gray
        BufferedImage resized = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = resized.createGraphics();
        g.drawImage(img, 0, 0, 28, 28, null);
        g.dispose();

        // 3. Extract pixels and normalize to [0, 1], Convert to 4D Tensor [1][1][28][28]
        float[][][][] tensorData = new float[1][1][28][28];
        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                int gray = resized.getRaster().getSample(x, y, 0);
                tensorData[0][0][y][x] = gray / 255.0f;
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