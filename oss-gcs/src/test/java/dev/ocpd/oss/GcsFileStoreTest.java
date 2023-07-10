package dev.ocpd.oss;

import dev.ocpd.oss.config.GcsFileStoreAutoConfiguration;
import dev.ocpd.oss.config.GcsTestContainerInitializer;
import dev.ocpd.oss.config.TestContainerStorageConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    classes = {GcsFileStoreAutoConfiguration.class, TestContainerStorageConfiguration.class},
    properties = "oss.provider=gcs"
)
@ContextConfiguration(initializers = GcsTestContainerInitializer.class)
public class GcsFileStoreTest extends FileStoreTest {

    @Override
    public void givenFile_whenUploadAndAccessSignedUrl_thenSuccess() {
        //Disable this test because it is not supported by fake-gcs-server
    }
}
