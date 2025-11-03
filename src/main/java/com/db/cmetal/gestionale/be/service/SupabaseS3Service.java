package com.db.cmetal.gestionale.be.service;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class SupabaseS3Service {

    private final S3Client s3Client;
    private final String bucket;
    private final String endpoint;

    public SupabaseS3Service(
            @Value("${supabase.s3.bucket}") String bucket,
            @Value("${supabase.s3.endpoint}") String endpoint,
            @Value("${supabase.s3.access-key}") String accessKey,
            @Value("${supabase.s3.secret-key}") String secretKey
    ) {
        this.bucket = bucket;
        this.endpoint = endpoint;
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.EU_WEST_1)
                .build();
    }

    public String uploadFile(MultipartFile file, String path) throws Exception {
        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putReq, software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));
        return path;
    }

    public void deleteFile(String path) {
        DeleteObjectRequest deleteReq = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .build();
        s3Client.deleteObject(deleteReq);
    }

    public String getPublicUrl(String path) {
        return String.format("%s/%s/%s", endpoint.replace("/s3", ""), bucket, path);
    }
}
