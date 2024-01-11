package dev.ocpd.oss

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.HttpMethod
import com.google.cloud.storage.Storage
import com.google.common.io.ByteStreams
import dev.ocpd.slf4k.debug
import dev.ocpd.slf4k.slf4j
import dev.ocpd.slf4k.warn
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.stream.Stream
import java.util.stream.StreamSupport

class GcsFileStore(
    private val storage: Storage,
    private val bucket: String
) : FileStore {

    private val log by slf4j

    override fun list(prefix: String?): Stream<String> {
        val bucket = storage[bucket]
        val blobs = if (prefix != null) bucket.list(Storage.BlobListOption.prefix(prefix)) else bucket.list()
        return StreamSupport.stream(blobs.iterateAll().spliterator(), false).map(Blob::getName)
    }

    override fun upload(key: String, path: Path) {
        val blobInfo = BlobInfo.newBuilder(bucket, key).build()
        storage.create(blobInfo, Files.readAllBytes(path))

        log.debug { "Uploaded file: $path with key: $key" }
    }

    override fun upload(key: String, content: ByteArray) {
        val blobInfo = BlobInfo.newBuilder(bucket, key).build()
        storage.create(blobInfo, content)

        log.debug { "Uploaded file with key: $key" }
    }

    override fun upload(key: String, ins: InputStream) {
        val blobInfo = BlobInfo.newBuilder(bucket, key).build()
        val fileContent = ByteStreams.toByteArray(ins)
        storage.create(blobInfo, fileContent)

        log.debug { "Upload file from input stream with key: $key" }
    }

    override fun downloadAsBytes(key: String): ByteArray {
        val fileContent = storage.readAllBytes(bucket, key)

        log.debug { "Downloaded file with key: $key" }
        return fileContent
    }

    override fun downloadTo(key: String, outputStream: OutputStream) {
        val blob = storage[bucket, key] ?: throw IllegalArgumentException("File does not exist: $key")
        blob.downloadTo(outputStream)

        log.debug { "Downloaded file with key: $key to output stream" }
    }

    override fun delete(key: String) {
        if (!storage.delete(bucket, key)) {
            log.warn { "File does not exist: $key" }
        }
    }

    override fun exists(key: String): Boolean {
        return storage[bucket, key] != null
    }

    override fun move(source: String, dest: String): Boolean {
        val sourceBlob = storage[bucket, source] ?: return false
        sourceBlob.copyTo(bucket, dest)
        sourceBlob.delete()

        return exists(dest)
    }

    override fun copy(source: String, dest: String): Boolean {
        val sourceBlob = storage[bucket, source] ?: return false
        sourceBlob.copyTo(bucket, dest)

        return exists(dest)
    }

    /**
     * @return The size of the file in bytes.
     */
    override fun sizeOf(key: String): Long {
        val blob = storage[bucket, key] ?: throw IllegalArgumentException("File does not exist: $key")
        return blob.size
    }

    override fun generateDownloadUrl(key: String, expirySeconds: Long): URL {
        val blobInfo = BlobInfo.newBuilder(bucket, key).build()

        return storage.signUrl(
            blobInfo,
            expirySeconds,
            TimeUnit.SECONDS,
            Storage.SignUrlOption.withV4Signature()
        )
    }

    override fun generateUploadUrl(key: String, expirySeconds: Long): URL {
        val blobInfo = BlobInfo.newBuilder(bucket, key).build()

        return storage.signUrl(
            blobInfo,
            expirySeconds,
            TimeUnit.SECONDS,
            Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
            Storage.SignUrlOption.withV4Signature()
        )
    }
}
