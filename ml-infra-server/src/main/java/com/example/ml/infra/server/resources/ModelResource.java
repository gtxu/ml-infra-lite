package com.example.ml.infra.server.resources;

import java.util.Map;

import com.example.ml.infra.core.ModelCoordinator;
import com.example.ml.infra.core.ModelManager;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/models")
@Produces(MediaType.APPLICATION_JSON)
public class ModelResource {
    private final ModelManager manager;
    private final ModelCoordinator coordinator;

    public ModelResource(ModelManager manager, ModelCoordinator coordinator) {
        this.manager = manager;
        this.coordinator = coordinator;
    }

    /**
     * Data Plane: High-concurrency inference endpoint.
     */
    @GET
    @Path("/predict")
    public String predict(@NotEmpty @NotNull @QueryParam("input") String input) {
        return manager.predict(input);
    }

    /**
     * Control Plane: Triggers an asynchronous model update.
     */
    @POST
    @Path("/update")
    public Response update(@QueryParam("version") String version) {
        if (version == null || version.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(Map.of("error", "Version is required"))
                           .build();
        }

        // Trigger the async update pipeline
        coordinator.onModelUpdate(version);

        return Response.accepted()
                       .entity(Map.of(
                           "message", "Update triggered successfully",
                           "target_version", version,
                           "status_url", "/v1/models/status"
                       ))
                       .build();
    }

    /**
     * Observability: Check the current status of the update pipeline.
     */
    @GET
    @Path("/status")
    public Response getStatus() {
        return Response.ok(Map.of("status", coordinator.getStatus())).build();
    }
}