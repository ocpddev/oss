package dev.ocpd.oss.config

import com.google.cloud.storage.Storage
import dev.ocpd.oss.FileStore
import dev.ocpd.oss.GcsFileStore
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnClass(Storage::class)
@ConditionalOnProperty(prefix = "oss", name = ["provider"], havingValue = "gcs")
@EnableConfigurationProperties(GcsFileStoreProperties::class)
class GcsFileStoreAutoConfiguration(
    private val properties: GcsFileStoreProperties,
    private val storage: Storage
) {
    private val log = LoggerFactory.getLogger(GcsFileStoreAutoConfiguration::class.java)

    @Bean
    @ConditionalOnMissingBean
    fun gcs(): FileStore {
        log.info("Registering Google Cloud Storage File Store")
        return GcsFileStore(storage, properties.bucket)
    }
}
