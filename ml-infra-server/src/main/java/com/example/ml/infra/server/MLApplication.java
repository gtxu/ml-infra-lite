package com.example.ml.infra.server;

import java.net.URI;

import com.example.ml.infra.core.ModelCoordinator;
import com.example.ml.infra.core.ModelManager;
import com.example.ml.infra.server.health.ModelHealthCheck;
import com.example.ml.infra.server.resources.ModelResource;

import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.lifecycle.Managed;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class MLApplication extends Application<MLConfiguration> {

    public static void main(String[] args) throws Exception {
        new MLApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<MLConfiguration> bootstrap) {
        bootstrap.addBundle(new SwaggerBundle<>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(MLConfiguration configuration) {
                return configuration.swaggerBundleConfiguration;
            }
        });
    }

    @Override
    public void run(MLConfiguration configuration, Environment environment) {

        S3Client s3Client = S3Client.builder()
            .endpointOverride(URI.create(configuration.s3Config.endpoint))
            .region(Region.of(configuration.s3Config.region))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(configuration.s3Config.accessKey, configuration.s3Config.secretKey)))
            .forcePathStyle(true)
            .build();
        
        // Ensure S3 client is closed on shutdown
        environment.lifecycle().manage(new Managed() {
            @Override
            public void stop() { s3Client.close(); }
        });

        final ModelManager modelManager = new ModelManager();
        final ModelCoordinator coordinator = new ModelCoordinator(modelManager, s3Client, configuration.s3Config.bucketName);

        environment.jersey().register(new ModelResource(modelManager, coordinator));

        environment.healthChecks().register("model-status", new ModelHealthCheck(modelManager, coordinator));

        coordinator.onModelUpdate("1.0.0-dummy");
    }
}