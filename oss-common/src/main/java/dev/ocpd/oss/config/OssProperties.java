package dev.ocpd.oss.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("oss")
public record OssProperties(Provider provider) {
}
