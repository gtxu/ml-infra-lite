package com.example.ml.infra.server.health;

import com.codahale.metrics.health.HealthCheck;
import com.example.ml.infra.core.ModelCoordinator;
import com.example.ml.infra.core.ModelManager;

/**
 * K8s marks Readiness only when the model is ready and not in a failed state。
 */
public class ModelHealthCheck extends HealthCheck {
    private final ModelManager manager;
    private final ModelCoordinator coordinator;

    public ModelHealthCheck(ModelManager manager, ModelCoordinator coordinator) {
        this.manager = manager;
        this.coordinator = coordinator;
    }

    @Override
    protected Result check() throws Exception {
        String status = coordinator.getStatus();
        
        // If the status contains FAILED, the health check fails
        if (status.startsWith("FAILED")) {
            return Result.unhealthy("Model update failed: " + status);
        }

        // Ensure there is currently a live model instance
        if (manager.predict("health_check_input") != null) {
            return Result.healthy("Model is live and serving. Status: " + status);
        }

        return Result.unhealthy("Model is initializing...");
    }
}
