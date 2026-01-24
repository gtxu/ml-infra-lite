package com.example.ml.infra.core.storage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
public class S3ModelDownloader {
    private final S3Client s3Client;
    private final String bucketName;
    private final Path localBaseDir;
    private final ObjectMapper mapper = new ObjectMapper();

    public S3ModelDownloader(S3Client s3Client, String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.localBaseDir = Paths.get(System.getProperty("java.io.tmpdir"), "models");
    }

    public String resolvePath(String version) {
        try {
            ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key("models/manifest.json")
                        .build()
            );
            
            JsonNode root = mapper.readTree(s3Stream);
            JsonNode pathNode = root.path("mappings").path(version);
            
            if (pathNode.isMissingNode()) {
                throw new IllegalArgumentException("Version not found in manifest: " + version);
            }
            
            return pathNode.asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve version through manifest.json", e);
        }
    }

    public File downloadModel(String modelPath) {
        
        String s3Key = "models/" + modelPath;

        Path localPath = localBaseDir.resolve(modelPath);

        try {
            // Synchronous fetch for the worker thread
            Files.createDirectories(localPath.getParent());

            System.out.println("[S3] Starting download: s3://" + this.bucketName + "/" + s3Key + " to " + localPath.toString());

            s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(this.bucketName)
                        .key(s3Key)
                        .build(), 
                    ResponseTransformer.toFile(localPath));
            
            return localPath.toFile();
        } catch (Exception e) {

            throw new RuntimeException(String.format("[S3] Failed to download model: %s, %s", modelPath, e.getMessage()), e);
        }
    }
}