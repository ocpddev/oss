package dev.ocpd.oss.config

import com.google.cloud.storage.BucketInfo
import com.google.cloud.storage.StorageOptions
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.event.ContextClosedEvent
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

class GcsTestContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    companion object {
        const val FAKE_GCS_SERVER_PORT = 4443
        private const val FAKE_GCS_SERVER_IMAGE = "fsouza/fake-gcs-server"
        private const val TEST_BUCKET_NAME = "test-bucket"
        private const val TEST_PROJECT_ID = "test-project"
    }

    private lateinit var container: GenericContainer<*>

    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
        container = createContainer()
        container.start()
        createBucket()
        applyConfiguration(configurableApplicationContext)
        configurableApplicationContext.addApplicationListener(ContainerShutdownListener())
    }

    private inner class ContainerShutdownListener : ApplicationListener<ContextClosedEvent> {

        override fun onApplicationEvent(contextClosedEvent: ContextClosedEvent) {
            if (container.isRunning) {
                container.stop()
            }
        }
    }

    private fun createContainer(): GenericContainer<*> {
        return GenericContainer(DockerImageName.parse(FAKE_GCS_SERVER_IMAGE))
            .withExposedPorts(FAKE_GCS_SERVER_PORT)
            .withCreateContainerCmdModifier {
                it.withEntrypoint("/bin/fake-gcs-server", "-scheme", "http")
            }
    }

    private fun applyConfiguration(configurableApplicationContext: ConfigurableApplicationContext) {
        TestPropertyValues.of(
            "oss.gcs.testcontainers.project-id=$TEST_PROJECT_ID",
            "oss.gcs.testcontainers.host=http://localhost:${container.firstMappedPort}",
            "oss.gcs.bucket=$TEST_BUCKET_NAME"
        )
            .applyTo(configurableApplicationContext.environment)
    }

    private fun createBucket() {
        StorageOptions.newBuilder()
            .setHost("http://localhost:${container.firstMappedPort}")
            .setProjectId(TEST_PROJECT_ID)
            .build()
            .service
            .create(BucketInfo.of(TEST_BUCKET_NAME))
    }
}
