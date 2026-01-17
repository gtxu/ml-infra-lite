package com.example.ml.infra.core;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.example.ml.infra.api.InferenceEngine;
import com.example.ml.infra.core.engine.DummyInferenceEngine;

/**
 * Manages the active InferenceEngine with thread-safe hot-swapping.
 */
public class ModelManager {
    private InferenceEngine currentEngine;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public ModelManager() {
        // Initial version starts with 1.0.0
        this.currentEngine = new DummyInferenceEngine("1.0.0");
    }

    public String predict(String input) {
        lock.readLock().lock();
        try {
            return currentEngine.predict(input);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void applyNewEngine(InferenceEngine nextEngine) {
        lock.writeLock().lock();
        try {
            this.currentEngine = nextEngine;
        } finally {
            lock.writeLock().unlock();
        }
    }
}