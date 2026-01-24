package com.example.ml.infra.core.storage;

import java.io.File;
import java.nio.file.Files;
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

    /**
     * @param modelPath example: "v1.0.0/model.onnx" or "v1.0.0/model.bin"
     */
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

            throw new RuntimeException(String.format("[S3] Failed to download model: %s", modelPath), e);
        }
    }
}