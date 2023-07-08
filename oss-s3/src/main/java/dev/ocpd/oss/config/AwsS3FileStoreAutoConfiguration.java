package dev.ocpd.oss.config;

import dev.ocpd.oss.AwsS3FileStore;
import dev.ocpd.oss.FileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@AutoConfiguration
@EnableConfigurationProperties({AwsS3Properties.class})
@ConditionalOnClass(S3Client.class)
@ConditionalOnProperty(prefix = "oss", name = "provider", havingValue = "s3")
public class AwsS3FileStoreAutoConfiguration {

    private final Logger log = LoggerFactory.getLogger(AwsS3FileStoreAutoConfiguration.class);

    private final AwsS3Properties awsS3Properties;

    private final AwsCredentialsProvider awsCredentialsProvider;

    public AwsS3FileStoreAutoConfiguration(AwsS3Properties awsS3Properties) {
        this.awsS3Properties = awsS3Properties;
        this.awsCredentialsProvider = this.createCredentialsProvider();
    }

    private AwsCredentialsProvider createCredentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                        this.awsS3Properties.accessKey(),
                        this.awsS3Properties.secretKey()
                )
        );
    }

    private S3Client buildS3Client() {
        var region = Region.of(this.awsS3Properties.region());
        var s3ClientBuilder = S3Client.builder()
                .credentialsProvider(this.awsCredentialsProvider)
                .region(region);
        if (this.awsS3Properties.endpoint() != null) {
            s3ClientBuilder.endpointOverride(this.awsS3Properties.endpoint());
        }
        return s3ClientBuilder.build();
    }

    private S3Presigner buildS3Presigner() {
        var region = Region.of(this.awsS3Properties.region());
        var s3PresignerBuilder = S3Presigner.builder()
                .credentialsProvider(this.awsCredentialsProvider)
                .region(region);
        if (this.awsS3Properties.endpoint() != null) {
            s3PresignerBuilder.endpointOverride(this.awsS3Properties.endpoint());
        }
        return s3PresignerBuilder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public FileStore awsS3() {
        log.info("Registering AWS S3 File Store");

        return new AwsS3FileStore(this.buildS3Client(), this.buildS3Presigner(), this.awsS3Properties.bucket());
    }
}
