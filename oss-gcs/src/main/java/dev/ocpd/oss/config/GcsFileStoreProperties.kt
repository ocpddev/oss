package dev.ocpd.oss.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("oss.gcs")
data class GcsFileStoreProperties(val bucket: String)
