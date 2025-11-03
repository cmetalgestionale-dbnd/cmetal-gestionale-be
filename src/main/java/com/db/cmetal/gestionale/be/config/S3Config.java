package com.db.cmetal.gestionale.be.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${supabase.s3.endpoint}")
    private String endpoint;

    @Value("${supabase.s3.access-key}")
    private String accessKey;

    @Value("${supabase.s3.secret-key}")
    private String secretKey;

    @Value("${supabase.s3.bucket}")
    private String bucket;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials creds = AwsBasicCredentials.create(accessKey, secretKey);
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))    // Endpoint Supabase
                .credentialsProvider(StaticCredentialsProvider.create(creds))
                .region(Region.EU_WEST_1)                  // Regione dummy
                .forcePathStyle(true)                      // IMPORTANTE per Supabase
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build();
    }

    @Bean
    public String bucketName() {
        return bucket;
    }
}
