package dev.ocpd.oss

import cloud.localstack.Localstack
import cloud.localstack.docker.annotation.LocalstackDockerConfiguration
import dev.ocpd.oss.config.AwsPropertiesEnvInitializer
import dev.ocpd.oss.config.AwsS3FileStoreAutoConfiguration
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest(classes = [AwsS3FileStoreAutoConfiguration::class])
@ContextConfiguration(initializers = [AwsPropertiesEnvInitializer::class])
object AwsS3FileStoreTest : FileStoreTest() {

    @JvmStatic
    @BeforeAll
    fun setUp() {
        Localstack.INSTANCE.startup(LocalstackDockerConfiguration.DEFAULT)
    }

    @JvmStatic
    @AfterAll
    fun teardown() {
        Localstack.INSTANCE.stop()
    }
}
