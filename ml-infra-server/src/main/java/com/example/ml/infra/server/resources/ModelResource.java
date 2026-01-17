package com.example.ml.infra.server.resources;

import com.example.ml.infra.core.ModelManager;
import com.example.ml.infra.core.engine.DummyInferenceEngine;

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

    public ModelResource(ModelManager manager) {
        this.manager = manager;
    }

    @GET
    @Path("/predict")
    public String predict(@QueryParam("input") String input) {
        return manager.predict(input);
    }

    @POST
    @Path("/update")
    public Response update(@QueryParam("version") String version) {
        if (version == null || version.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Version is required").build();
        }
        // Synchronous swap for initial validation
        manager.applyNewEngine(new DummyInferenceEngine(version));
        return Response.ok("Successfully swapped to version: " + version).build();
    }
}