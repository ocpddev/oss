package dev.ocpd.oss.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("oss.gcs.testcontainers")
data class TestContainerGcsFileStoreProperties(val host: String, val projectId: String)
