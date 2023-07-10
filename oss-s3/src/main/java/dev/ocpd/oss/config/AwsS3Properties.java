package dev.ocpd.oss.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;

import java.net.URI;

@ConfigurationProperties("oss.s3")
public record AwsS3Properties(
    String accessKey,
    String secretKey,
    String region,
    String bucket,
    @Nullable
    URI endpoint) {

}
