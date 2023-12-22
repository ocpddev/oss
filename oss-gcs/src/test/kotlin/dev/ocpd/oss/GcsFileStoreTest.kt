package dev.ocpd.oss

import dev.ocpd.oss.config.GcsFileStoreAutoConfiguration
import dev.ocpd.oss.config.GcsTestContainerInitializer
import dev.ocpd.oss.config.TestContainerStorageConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest(
    classes = [GcsFileStoreAutoConfiguration::class, TestContainerStorageConfiguration::class],
    properties = ["oss.provider=gcs"]
)
@ContextConfiguration(initializers = [GcsTestContainerInitializer::class])
class GcsFileStoreTest(
    override val fileStore: GcsFileStore
) : FileStoreContract<GcsFileStore> {

    override fun generateDownloadUrl() {
        //Disable this test because it is not supported by fake-gcs-server
    }

    override fun generateUploadUrl() {
        //Disable this test because it is not supported by fake-gcs-server
    }
}
