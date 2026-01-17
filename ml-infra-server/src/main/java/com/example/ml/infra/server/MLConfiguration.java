package com.example.ml.infra.server;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.core.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class MLConfiguration extends Configuration {
    @Valid
    @NotNull
    @JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;

    @Valid
    @NotNull
    @JsonProperty("s3")
    public S3Config s3Config;

    public static class S3Config {
        @NotEmpty public String endpoint;
        @NotEmpty public String region;
        @NotEmpty public String accessKey;
        @NotEmpty public String secretKey;
        @NotEmpty public String bucketName;
    }
}