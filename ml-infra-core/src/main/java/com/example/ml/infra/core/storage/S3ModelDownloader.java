package com.example.ml.infra.core.storage;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
public class S3ModelDownloader {
    private final S3Client s3Client;
    private final String bucketName;
    private final Path localBaseDir;

    public S3ModelDownloader(S3Client s3Client, String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.localBaseDir = Paths.get(System.getProperty("java.io.tmpdir"), "models");
    }

    public File downloadModel(String version) {
        
        String s3Key = String.format("models/model-v%s.onnx", version);
        Path localPath = localBaseDir.resolve(String.format("model-v%s.onnx", version));

        System.out.println("[S3] Starting download: s3://" + this.bucketName + "/" + s3Key);

        try {
            // Synchronous fetch for the worker thread
            s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(this.bucketName)
                        .key(s3Key)
                        .build(), 
                    ResponseTransformer.toFile(localPath));
            return localPath.toFile();
        } catch (Exception e) {

            throw new RuntimeException(String.format("[S3] Failed to download model v%s: %s", version, e.toString()), e);
        }
    }
}