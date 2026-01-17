package com.example.ml.infra.server;

import com.example.ml.infra.core.ModelManager;
import com.example.ml.infra.server.resources.ModelResource;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

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
        final ModelManager modelManager = new ModelManager();
        environment.jersey().register(new ModelResource(modelManager));
    }
}