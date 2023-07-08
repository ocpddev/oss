package dev.ocpd.oss.config;

import com.google.cloud.storage.Storage;
import dev.ocpd.oss.FileStore;
import dev.ocpd.oss.GcsFileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(Storage.class)
@ConditionalOnProperty(prefix = "oss", name = "provider", havingValue = "gcs")
@EnableConfigurationProperties(GcsFileStoreProperties.class)
public class GcsFileStoreAutoConfiguration {

    private final Logger log = LoggerFactory.getLogger(GcsFileStoreAutoConfiguration.class);

    private final GcsFileStoreProperties properties;

    private final Storage storage;

    public GcsFileStoreAutoConfiguration(GcsFileStoreProperties properties,
                                         Storage storage) {
        this.properties = properties;
        this.storage = storage;
    }

    @Bean
    @ConditionalOnMissingBean
    public FileStore gcs() {
        log.info("Registering Google Cloud Storage File Store");

        return new GcsFileStore(this.storage, this.properties.bucket());
    }
}
