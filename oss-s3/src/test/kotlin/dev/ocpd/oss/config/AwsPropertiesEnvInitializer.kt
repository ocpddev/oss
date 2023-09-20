package dev.ocpd.oss.config

import cloud.localstack.Localstack
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext

class AwsPropertiesEnvInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        TestPropertyValues.of(ENVS).applyTo(applicationContext.environment)
    }

    companion object {
        private const val TEST_BUCKET_NAME = "test-bucket"
        private const val TEST_ACCESS_KEY = "test"
        private const val TEST_SECRET_KEY = "test"

        private val ENVS = mapOf(
            "oss.provider" to "s3",
            "oss.s3.access-key" to TEST_ACCESS_KEY,
            "oss.s3.secret-key" to TEST_SECRET_KEY,
            "oss.s3.bucket" to TEST_BUCKET_NAME,
            "oss.s3.region" to Localstack.getDefaultRegion(),
            "oss.s3.endpoint" to Localstack.INSTANCE.endpointS3
        )
    }
}
