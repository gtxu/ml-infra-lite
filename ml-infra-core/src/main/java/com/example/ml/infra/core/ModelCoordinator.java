package com.example.ml.infra.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import com.example.ml.infra.api.InferenceEngine;
import com.example.ml.infra.core.engine.DummyInferenceEngine;
import com.example.ml.infra.core.storage.S3ModelDownloader;

public class ModelCoordinator {
    private final ModelManager modelManager;
    private final S3ModelDownloader downloader;
    private final AtomicReference<String> status = new AtomicReference<>("IDLE");

    public ModelCoordinator(ModelManager modelManager, S3ModelDownloader downloader) {
        this.modelManager = modelManager;
        this.downloader = downloader;
    }

    public void onModelUpdate(String version) {
        status.set("UPDATING");
        CompletableFuture.runAsync(() -> {
            try {
                downloader.downloadModel(version); // simulate S3 download
                InferenceEngine next = new DummyInferenceEngine(version);
                next.predict("warmup_data"); // warm-up
                modelManager.applyNewEngine(next);
                status.set("IDLE");
            } catch (Exception e) {
                status.set("FAILED: " + e.getMessage());
            }
        });
    }

    public String getStatus() { return status.get(); }
}