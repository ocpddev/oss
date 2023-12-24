package dev.ocpd.oss

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.URL
import java.net.http.HttpClient.newHttpClient
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpRequest.newBuilder
import java.net.http.HttpResponse.BodyHandlers
import java.nio.file.Files
import java.nio.file.StandardCopyOption

interface FileStoreContract<T : FileStore> {

    companion object {
        private const val TEST_FILE_KEY_PREFIX = "oss-test-images"
        private const val TEST_FILE_KEY = "$TEST_FILE_KEY_PREFIX/binglogo.png"
        private val TEST_FILE_URL: URL = URI("https://www.bing.com/msasignin/cobranding/logo").toURL()
    }

    val fileStore: T

    @AfterEach
    fun clear() {
        fileStore.delete(TEST_FILE_KEY)
    }

    @Test
    @DisplayName("file should appear in listing after upload")
    fun list() {
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
    @DisplayName("download to stream")
    fun downloadTo() {
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
    @DisplayName("download as bytes")
    fun downloadAsBytes() {
        TEST_FILE_URL.openStream().use {
            val bytes = it.readAllBytes()
            fileStore.upload(TEST_FILE_KEY, ByteArrayInputStream(bytes))
            val fileContent = fileStore.downloadAsBytes(TEST_FILE_KEY)

            assertArrayEquals(bytes, fileContent)
        }
    }

    @Test
    @DisplayName("upload from bytes")
    fun uploadFromBytes() {
        val message = "Hello World"
        val fileKey = "txt/test.txt"
        fileStore.upload(fileKey, message.toByteArray())
        val fileContent = fileStore.downloadAsBytes(fileKey)

        assertArrayEquals(message.toByteArray(), fileContent)
    }

    @Test
    @DisplayName("file should not appear in listing after deletion")
    fun delete() {
        val message = "Hello World"
        val fileKey = "txt/test.txt"
        fileStore.upload(fileKey, message.toByteArray())
        fileStore.delete(fileKey)

        assertEquals(0, fileStore.list(TEST_FILE_KEY_PREFIX).count())
    }

    @Test
    @DisplayName("file should exist after upload")
    fun exists() {
        TEST_FILE_URL.openStream().use {
            val fileContent = it.readAllBytes()
            fileStore.upload(TEST_FILE_KEY, fileContent)
            val fileExist = fileStore.exists(TEST_FILE_KEY)

            assertTrue(fileExist)
        }
    }

    @Test
    @DisplayName("should be able to move file")
    fun move() {
        val tempFile = Files.createTempFile("binglogo", ".png")

        TEST_FILE_URL.openStream().use {
            Files.copy(it, tempFile, StandardCopyOption.REPLACE_EXISTING)
            fileStore.upload(TEST_FILE_KEY, tempFile)
            val newKey = "$TEST_FILE_KEY_PREFIX/binglogo2.png"
            fileStore.move(TEST_FILE_KEY, newKey)
            val fileExist = fileStore.exists(newKey)
            assertTrue(fileExist)
        }

        Files.delete(tempFile)
    }

    @Test
    @DisplayName("should be able to get file size")
    fun sizeOf() {
        TEST_FILE_URL.openStream().use {
            val fileContent = it.readAllBytes()
            fileStore.upload(TEST_FILE_KEY, fileContent)
            val fileSize = fileStore.sizeOf(TEST_FILE_KEY)
            assertEquals(fileContent.size.toLong(), fileSize)
        }
    }

    @Test
    @DisplayName("should be able to download via signed url")
    fun generateDownloadUrl() {
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

    @Test
    @DisplayName("should be able to upload via signed url")
    fun generateUploadUrl() {
        val tempFile = Files.createTempFile("binglogo", ".png")

        TEST_FILE_URL.openStream().use {
            Files.copy(it, tempFile, StandardCopyOption.REPLACE_EXISTING)
            val signedUrl = fileStore.generateUploadUrl(TEST_FILE_KEY)
            // use http client to upload
            newHttpClient().send(
                newBuilder(signedUrl.toURI()).PUT(BodyPublishers.ofFile(tempFile)).build(),
                BodyHandlers.ofString()
            )
            val fileContent = fileStore.downloadAsBytes(TEST_FILE_KEY)
            assertArrayEquals(Files.readAllBytes(tempFile), fileContent)
        }

        Files.delete(tempFile)
    }
}
