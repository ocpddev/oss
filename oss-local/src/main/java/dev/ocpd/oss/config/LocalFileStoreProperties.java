package dev.ocpd.oss.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("oss.local")
public record LocalFileStoreProperties(String rootPath) {

}
