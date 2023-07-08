package dev.ocpd.oss;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class FileStoreTest {

    private static final URL TEST_FILE_URL;

    static {
        try {
            TEST_FILE_URL = new URL("https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid test file URL", e);
        }
    }

    private static final String TEST_FILE_KEY_PREFIX = "oss-test-images";
    private static final String TEST_FILE_KEY = TEST_FILE_KEY_PREFIX + "/googlelogo.png";

    @Autowired
    private FileStore fileStore;

    @AfterEach
    public void clear() {
        fileStore.delete(TEST_FILE_KEY);
    }

    @Test
    public void givenFile_whenUploadFileByPathAndList_thenSuccess() throws IOException {
        var tempFile = Files.createTempFile("googlelogo", ".png");

        try (var inputStream = TEST_FILE_URL.openStream()) {
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            fileStore.upload(TEST_FILE_KEY, tempFile);
            var files = fileStore.list(TEST_FILE_KEY_PREFIX).toList();

            assertEquals(1, files.size());
            assertEquals(TEST_FILE_KEY, files.get(0));
        } finally {
            Files.delete(tempFile);
        }
    }

    @Test
    public void givenFile_whenUploadFileAndDownloadToOutputStream_thenSuccess() throws IOException {
        var tempFile = Files.createTempFile("googlelogo", ".png");

        try (var inputStream = TEST_FILE_URL.openStream()) {
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            fileStore.upload(TEST_FILE_KEY, tempFile);

            var os = new ByteArrayOutputStream((int) tempFile.toFile().length());
            fileStore.downloadTo(TEST_FILE_KEY, os);
            var fileContent = os.toByteArray();

            Assertions.assertArrayEquals(Files.readAllBytes(tempFile), fileContent);
        } finally {
            Files.delete(tempFile);
        }
    }

    @Test
    public void givenFileUrl_whenUploadFileByUrlAndDownloadToBytes_thenSuccess() throws IOException {
        try (var inputStream = TEST_FILE_URL.openStream()) {
            var bytes = inputStream.readAllBytes();

            fileStore.upload(TEST_FILE_KEY, new ByteArrayInputStream(bytes));
            var fileContent = fileStore.downloadAsBytes(TEST_FILE_KEY);

            Assertions.assertArrayEquals(bytes, fileContent);
        }
    }

    @Test
    public void givenFileContent_whenUploadFileByContentAndDownloadToBytes_thenSuccess() {
        var message = "Hello World";
        var fileKey = "txt/test.txt";

        fileStore.upload(fileKey, message.getBytes());
        var fileContent = fileStore.downloadAsBytes(fileKey);

        Assertions.assertArrayEquals(message.getBytes(), fileContent);
    }

    @Test
    public void givenFileContent_whenUploadAndDelete_thenSuccess() {
        var message = "Hello World";
        var fileKey = "txt/test.txt";

        fileStore.upload(fileKey, message.getBytes());
        fileStore.delete(fileKey);

        assertEquals(0, fileStore.list(TEST_FILE_KEY_PREFIX).count());
    }

    @Test
    public void givenFileContent_whenUploadAndCheckFileExist_thenTrue() throws IOException {
        try (var inputStream = TEST_FILE_URL.openStream()) {
            var fileContent = inputStream.readAllBytes();
            fileStore.upload(TEST_FILE_KEY, fileContent);

            var fileExist = fileStore.exists(TEST_FILE_KEY);
            Assertions.assertTrue(fileExist);
        }
    }

    @Test
    public void givenFile_whenUploadAndAccessSignedUrl_thenSuccess() throws IOException {
        var tempFile = Files.createTempFile("googlelogo", ".png");

        try (var inputStream = TEST_FILE_URL.openStream()) {
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            fileStore.upload(TEST_FILE_KEY, tempFile);

            var signedUrl = fileStore.generateDownloadUrl(TEST_FILE_KEY);
            var os = new ByteArrayOutputStream((int) tempFile.toFile().length());
            try (os; var is = signedUrl.openStream()) {
                is.transferTo(os);
            }

            Assertions.assertArrayEquals(Files.readAllBytes(tempFile), os.toByteArray());
        } finally {
            Files.delete(tempFile);
        }
    }
}
