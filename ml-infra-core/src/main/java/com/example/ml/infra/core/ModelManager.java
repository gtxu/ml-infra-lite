package com.example.ml.infra.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.example.ml.infra.api.InferenceEngine;
import com.example.ml.infra.core.engine.DummyInferenceEngine;

/**
 * Manages the active InferenceEngine with thread-safe hot-swapping.
 */
public class ModelManager {
    private InferenceEngine currentEngine;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

    public ModelManager() {
        // Initial version starts with 1.0.0
        this.currentEngine = new DummyInferenceEngine("1.0.0");
    }

    public String predict(String input) {
        lock.readLock().lock();
        try {
            return currentEngine != null ? currentEngine.predict(input) : "No model loaded";
        } finally {
            lock.readLock().unlock();
        }
    }

    public void applyNewEngine(InferenceEngine nextEngine) {
        InferenceEngine oldEngine;
        lock.writeLock().lock();
        try {
            oldEngine = this.currentEngine;
            this.currentEngine = nextEngine;
        } finally {
            lock.writeLock().unlock();
        }

        // Don't close the old engine immediately because some read threads 
        // might still be finishing their predict() calls.
        if (oldEngine != null) {
            cleanupExecutor.schedule(() -> {
                try {
                    System.out.println("Cleaning up old engine: " + oldEngine.getVersion() + " in 30s");
                    oldEngine.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 30, TimeUnit.SECONDS); // Delay 30s to ensure all read threads are out
        }
    }
}