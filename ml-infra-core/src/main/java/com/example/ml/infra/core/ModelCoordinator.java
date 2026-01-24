package com.example.ml.infra.core;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import com.example.ml.infra.api.InferenceEngine;
import com.example.ml.infra.core.engine.DummyInferenceEngine;
import com.example.ml.infra.core.engine.OnnxInferenceEngine;
import com.example.ml.infra.core.storage.S3ModelDownloader;

import software.amazon.awssdk.services.s3.S3Client;

public class ModelCoordinator {
    private final ModelManager modelManager;
    private final S3ModelDownloader downloader;
    private final AtomicReference<String> status = new AtomicReference<>("IDLE");

    public ModelCoordinator(ModelManager modelManager, S3Client s3Client, String bucketName) {
        this.modelManager = modelManager;
        this.downloader = new S3ModelDownloader(s3Client, bucketName);
    }

    public void onModelUpdate(String version) {
        status.set("UPDATING");
        CompletableFuture.runAsync(() -> {
            try {

                String modelPath = downloader.resolvePath(version);

                System.out.println("[Coordinator] Resolved " + version + " to " + modelPath);

                File modelFile = downloader.downloadModel(modelPath);

                InferenceEngine next = createEngine(version, modelFile);
                
                // Warm-up the new model before swapping
                String warmUpInput = java.util.stream.IntStream.range(0, 784)
                                        .mapToObj(i -> "0")
                                        .collect(java.util.stream.Collectors.joining(","));

                next.predict(warmUpInput);

                modelManager.applyNewEngine(next);

                status.set("IDLE");
            } catch (Exception e) {
                status.set("FAILED: " + e.getMessage());
            }
        });
    }

    private InferenceEngine createEngine(String version, File file) throws Exception {
        String fileName = file.getName().toLowerCase();
        
        if (fileName.endsWith(".onnx")) {
            return new OnnxInferenceEngine(version, file.getAbsolutePath());
        } else {
            // Fallback to Dummy for .bin or unknown types
            return new DummyInferenceEngine(version, file.getAbsolutePath());
        }
    }

    public String getStatus() { return status.get(); }
}