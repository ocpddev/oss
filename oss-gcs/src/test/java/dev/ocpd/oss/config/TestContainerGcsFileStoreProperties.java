package dev.ocpd.oss.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("oss.gcs.testcontainers")
public record TestContainerGcsFileStoreProperties(String host, String projectId) {
}
