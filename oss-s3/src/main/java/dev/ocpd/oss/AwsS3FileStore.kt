package dev.ocpd.oss

import dev.ocpd.slf4k.debug
import dev.ocpd.slf4k.slf4j
import org.jetbrains.annotations.NotNull
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.nio.file.Path
import java.time.Duration
import java.util.stream.Stream
import java.util.stream.StreamSupport

class AwsS3FileStore(
    private val client: S3Client,
    private val s3Presigner: S3Presigner,
    private val bucket: String
) : FileStore {

    private val log by slf4j

    init {
        createBucketIfNotExists()
    }

    private fun createBucketIfNotExists() {
        try {
            client.headBucket { it.bucket(bucket) }
        } catch (e: NoSuchBucketException) {
            client.createBucket { it.bucket(bucket) }
        }
    }

    override fun list(prefix: String?): Stream<String> {
        val iterable = S3ObjectSummaryIterable(client, bucket, prefix)
        return StreamSupport.stream(iterable.spliterator(), false).map(S3Object::key)
    }

    private inner class S3ObjectSummaryIterable(
        private val client: S3Client,
        private val bucket: String,
        private val prefix: String?
    ) : Iterable<S3Object> {

        @NotNull
        override fun iterator(): Iterator<S3Object> = iterator {
            var request = ListObjectsV2Request.builder().bucket(bucket).prefix(prefix).build()
            while (true) {
                val response = client.listObjectsV2(request)
                val objects = response.contents().iterator()
                request = request.toBuilder().continuationToken(response.continuationToken()).build()
                yieldAll(objects)
                if (!response.isTruncated) {
                    break
                }
            }
        }
    }

    override fun upload(key: String, path: Path) {
        client.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(), path)

        log.debug { "Uploaded file to S3: $path -> $key" }
    }

    override fun upload(key: String, content: ByteArray) {
        client.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(), RequestBody.fromBytes(content))

        log.debug { "Uploaded file to S3: ${content.size} bytes -> $key" }
    }

    override fun upload(key: String, ins: InputStream) {
        val contents = ins.readAllBytes()
        client.putObject(
            PutObjectRequest.builder().bucket(bucket).key(key).build(), RequestBody.fromBytes(contents)
        )

        log.debug { "Uploaded file from InputStream to S3: $key" }
    }

    override fun downloadAsBytes(key: String): ByteArray {
        val objectBytes = client.getObjectAsBytes { it.bucket(bucket).key(key) }
        val bytes = objectBytes.asByteArray()

        log.debug { "Downloaded file from S3: $key -> ${bytes.size} bytes" }
        return bytes
    }

    override fun downloadTo(key: String, outputStream: OutputStream) {
        client.getObject { it.bucket(bucket).key(key) }.use {
            it.transferTo(outputStream)
        }
        log.debug { "Downloaded file from S3 to output stream: $key" }
    }

    override fun delete(key: String) {
        client.deleteObject { it.bucket(bucket).key(key) }
    }

    override fun exists(key: String): Boolean {
        return try {
            client.headObject { it.bucket(bucket).key(key) }
            true
        } catch (e: NoSuchKeyException) {
            false
        }
    }

    override fun generateDownloadUrl(key: String, expirySeconds: Long): URL {
        val presignedGetRequest = s3Presigner.presignGetObject {
            it.getObjectRequest { innerBuilder -> innerBuilder.bucket(bucket).key(key) }
                .signatureDuration(Duration.ofSeconds(expirySeconds))
        }
        return presignedGetRequest.url()
    }
}
