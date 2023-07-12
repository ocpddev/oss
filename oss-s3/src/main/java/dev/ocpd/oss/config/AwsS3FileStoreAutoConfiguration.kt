package dev.ocpd.oss.config

import dev.ocpd.oss.AwsS3FileStore
import dev.ocpd.oss.FileStore
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner

@AutoConfiguration
@EnableConfigurationProperties(AwsS3Properties::class)
@ConditionalOnClass(S3Client::class)
@ConditionalOnProperty(prefix = "oss", name = ["provider"], havingValue = "s3")
class AwsS3FileStoreAutoConfiguration(
    private val awsS3Properties: AwsS3Properties
) {
    private val log = LoggerFactory.getLogger(AwsS3FileStoreAutoConfiguration::class.java)
    private val awsCredentialsProvider: AwsCredentialsProvider = createCredentialsProvider()

    private fun createCredentialsProvider(): AwsCredentialsProvider {
        return StaticCredentialsProvider.create(
            AwsBasicCredentials.create(
                awsS3Properties.accessKey,
                awsS3Properties.secretKey
            )
        )
    }

    private fun buildS3Client(): S3Client {
        val region = Region.of(awsS3Properties.region)
        val s3ClientBuilder = S3Client.builder()
            .credentialsProvider(awsCredentialsProvider)
            .region(region)
        if (awsS3Properties.endpoint != null) {
            s3ClientBuilder.endpointOverride(awsS3Properties.endpoint)
        }
        return s3ClientBuilder.build()
    }

    private fun buildS3Presigner(): S3Presigner {
        val region = Region.of(awsS3Properties.region)
        val s3PresignerBuilder = S3Presigner.builder()
            .credentialsProvider(awsCredentialsProvider)
            .region(region)
        if (awsS3Properties.endpoint != null) {
            s3PresignerBuilder.endpointOverride(awsS3Properties.endpoint)
        }
        return s3PresignerBuilder.build()
    }

    @Bean
    @ConditionalOnMissingBean
    fun awsS3(): FileStore {
        log.info("Registering AWS S3 File Store")

        return AwsS3FileStore(buildS3Client(), buildS3Presigner(), awsS3Properties.bucket)
    }
}
