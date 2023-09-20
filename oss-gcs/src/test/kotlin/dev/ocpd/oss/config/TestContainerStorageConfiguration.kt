package dev.ocpd.oss.config

import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * This configuration class is used to configure the GCS storage client for TestContainer.
 */
@Configuration
@EnableConfigurationProperties(TestContainerGcsFileStoreProperties::class)
class TestContainerStorageConfiguration(
    private val properties: TestContainerGcsFileStoreProperties
) {

    @Bean
    fun storage(): Storage {
        return StorageOptions.newBuilder()
            .setHost(properties.host)
            .setProjectId(properties.projectId)
            .build()
            .service
    }
}
