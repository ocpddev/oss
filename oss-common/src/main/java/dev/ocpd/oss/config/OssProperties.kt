package dev.ocpd.oss.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("oss")
data class OssProperties(val provider: Provider)
