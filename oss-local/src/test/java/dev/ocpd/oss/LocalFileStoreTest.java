package dev.ocpd.oss;

import dev.ocpd.oss.config.LocalFileStoreAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    value = {
        "oss.provider=local",
        "oss.local.root-path=/tmp/file-store-api-test",
    },
    classes = LocalFileStoreAutoConfiguration.class
)
public class LocalFileStoreTest extends FileStoreTest {

    @Override
    public void givenFile_whenUploadAndAccessSignedUrl_thenSuccess() {
        //Disable this test because it is not supported by local file system
    }
}
