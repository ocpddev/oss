package dev.ocpd.oss.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("oss.gcs")
public record GcsFileStoreProperties(String bucket) {
}
