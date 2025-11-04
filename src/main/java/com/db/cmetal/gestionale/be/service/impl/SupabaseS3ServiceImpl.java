package com.db.cmetal.gestionale.be.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.db.cmetal.gestionale.be.service.SupabaseS3Service;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
public class SupabaseS3ServiceImpl implements SupabaseS3Service {

    private final S3Client s3Client;
    private final String bucket;

    public SupabaseS3ServiceImpl(S3Client s3Client, String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        log.info("SupabaseS3ServiceImpl initialized. Bucket: {}", bucket);
    }

    @Override
    public String uploadFile(MultipartFile file, String path) throws Exception {
        log.info("Uploading file '{}' to path '{}'", file.getOriginalFilename(), path);
        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .contentType(file.getContentType())
                .build();

        try {
            s3Client.putObject(putReq, RequestBody.fromBytes(file.getBytes()));
            log.info("File '{}' uploaded successfully", file.getOriginalFilename());
        } catch (Exception e) {
            log.error("Error uploading file '{}': {}", file.getOriginalFilename(), e.getMessage(), e);
            throw e;
        }
        return path;
    }

    @Override
    public void deleteFile(String path) {
        log.info("Deleting file at path '{}'", path);
        try {
            DeleteObjectRequest deleteReq = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(path)
                    .build();
            s3Client.deleteObject(deleteReq);
            log.info("File '{}' deleted successfully", path);
        } catch (Exception e) {
            log.error("Error deleting file '{}': {}", path, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public String getPublicUrl(String path) {
        String url = String.format("%s/%s/%s", s3Client.serviceClientConfiguration().endpointOverride().toString(), bucket, path);
        log.info("Generated public URL: {}", url);
        return url;
    }

    @Override
    public byte[] downloadFile(String path) throws Exception {
        try {
            return s3Client.getObjectAsBytes(b -> b.bucket(bucket).key(path)).asByteArray();
        } catch (Exception e) {
            log.error("Error downloading file '{}': {}", path, e.getMessage(), e);
            throw e;
        }
    }
}
