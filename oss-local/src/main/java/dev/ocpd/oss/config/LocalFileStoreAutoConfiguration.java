package dev.ocpd.oss.config;

import dev.ocpd.oss.FileStore;
import dev.ocpd.oss.LocalFileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(LocalFileStoreProperties.class)
@ConditionalOnClass(LocalFileStore.class)
@ConditionalOnProperty(prefix = "oss", name = "provider", havingValue = "local")
public class LocalFileStoreAutoConfiguration {

    private final Logger log = LoggerFactory.getLogger(LocalFileStoreAutoConfiguration.class);

    private final LocalFileStoreProperties localFileStoreProperties;

    public LocalFileStoreAutoConfiguration(LocalFileStoreProperties localFileStoreProperties) {
        this.localFileStoreProperties = localFileStoreProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public FileStore local() {
        log.info("Registering Local File System File Store");

        return new LocalFileStore(this.localFileStoreProperties.rootPath());
    }
}
