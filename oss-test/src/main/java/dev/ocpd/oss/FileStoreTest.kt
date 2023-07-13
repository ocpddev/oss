package dev.ocpd.oss

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.MalformedURLException
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption

abstract class FileStoreTest {

    companion object {
        private const val TEST_FILE_KEY_PREFIX = "oss-test-images"
        private const val TEST_FILE_KEY = "$TEST_FILE_KEY_PREFIX/binglogo.png"
        private val TEST_FILE_URL: URL = try {
            URL("https://www.bing.com/msasignin/cobranding/logo")
        } catch (e: MalformedURLException) {
            throw IllegalArgumentException("Invalid test file URL", e)
        }
    }

    @Autowired
    private lateinit var fileStore: FileStore

    @AfterEach
    fun clear() {
        fileStore.delete(TEST_FILE_KEY)
    }

    @Test
    fun givenFile_whenUploadFileByPathAndList_thenSuccess() {
        val tempFile = Files.createTempFile("binglogo", ".png")

        TEST_FILE_URL.openStream().use {
            Files.copy(it, tempFile, StandardCopyOption.REPLACE_EXISTING)

            fileStore.upload(TEST_FILE_KEY, tempFile)
            val files = fileStore.list(TEST_FILE_KEY_PREFIX).toList()

            assertEquals(1, files.size)
            assertEquals(TEST_FILE_KEY, files[0])
        }

        Files.delete(tempFile)
    }

    @Test
    fun givenFile_whenUploadFileAndDownloadToOutputStream_thenSuccess() {
        val tempFile = Files.createTempFile("binglogo", ".png")

        TEST_FILE_URL.openStream().use {
            Files.copy(it, tempFile, StandardCopyOption.REPLACE_EXISTING)

            fileStore.upload(TEST_FILE_KEY, tempFile)

            val os = ByteArrayOutputStream(tempFile.toFile().length().toInt())
            fileStore.downloadTo(TEST_FILE_KEY, os)
            val fileContent = os.toByteArray()

            assertArrayEquals(Files.readAllBytes(tempFile), fileContent)
        }

        Files.delete(tempFile)
    }

    @Test
    fun givenFileUrl_whenUploadFileByUrlAndDownloadToBytes_thenSuccess() {
        TEST_FILE_URL.openStream().use {
            val bytes = it.readAllBytes()
            fileStore.upload(TEST_FILE_KEY, ByteArrayInputStream(bytes))
            val fileContent = fileStore.downloadAsBytes(TEST_FILE_KEY)

            assertArrayEquals(bytes, fileContent)
        }
    }

    @Test
    fun givenFileContent_whenUploadFileByContentAndDownloadToBytes_thenSuccess() {
        val message = "Hello World"
        val fileKey = "txt/test.txt"
        fileStore.upload(fileKey, message.toByteArray())
        val fileContent = fileStore.downloadAsBytes(fileKey)

        assertArrayEquals(message.toByteArray(), fileContent)
    }

    @Test
    fun givenFileContent_whenUploadAndDelete_thenSuccess() {
        val message = "Hello World"
        val fileKey = "txt/test.txt"
        fileStore.upload(fileKey, message.toByteArray())
        fileStore.delete(fileKey)

        assertEquals(0, fileStore.list(TEST_FILE_KEY_PREFIX).count())
    }

    @Test
    fun givenFileContent_whenUploadAndCheckFileExist_thenTrue() {
        TEST_FILE_URL.openStream().use {
            val fileContent = it.readAllBytes()
            fileStore.upload(TEST_FILE_KEY, fileContent)
            val fileExist = fileStore.exists(TEST_FILE_KEY)

            assertTrue(fileExist)
        }
    }

    @Test
    open fun givenFile_whenUploadAndAccessSignedUrl_thenSuccess() {
        val tempFile = Files.createTempFile("binglogo", ".png")

        TEST_FILE_URL.openStream().use {
            Files.copy(it, tempFile, StandardCopyOption.REPLACE_EXISTING)
            fileStore.upload(TEST_FILE_KEY, tempFile)
            val signedUrl = fileStore.generateDownloadUrl(TEST_FILE_KEY)
            val os = ByteArrayOutputStream(tempFile.toFile().length().toInt())
            os.use { signedUrl.openStream().use { ins -> ins.transferTo(os) } }

            assertArrayEquals(Files.readAllBytes(tempFile), os.toByteArray())
        }

        Files.delete(tempFile)
    }
}
