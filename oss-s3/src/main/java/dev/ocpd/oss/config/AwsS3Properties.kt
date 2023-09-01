package dev.ocpd.oss.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI

@ConfigurationProperties("oss.s3")
data class AwsS3Properties(
    val accessKey: String?,
    val secretKey: String?,
    val region: String,
    val bucket: String,
    val endpoint: URI?
)
