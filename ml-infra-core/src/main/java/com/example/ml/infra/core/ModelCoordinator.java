package com.example.ml.infra.core;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import com.example.ml.infra.api.InferenceEngine;
import com.example.ml.infra.core.engine.DummyInferenceEngine;
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
                File modelFile = downloader.downloadModel(version);
                InferenceEngine next = new DummyInferenceEngine(version, modelFile.getAbsolutePath());
                
                // Warm-up the new model before swapping
                next.predict("warmup_data");

                modelManager.applyNewEngine(next);

                status.set("IDLE");
            } catch (Exception e) {
                status.set("FAILED: " + e.getMessage());
            }
        });
    }

    public String getStatus() { return status.get(); }
}