package dev.ocpd.oss;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class AwsS3FileStore implements FileStore {

    private final Logger log = LoggerFactory.getLogger(AwsS3FileStore.class);

    private final S3Client client;
    private final S3Presigner s3Presigner;
    private final String bucket;

    public AwsS3FileStore(S3Client client, S3Presigner s3Presigner, String bucket) {
        this.client = client;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
        createBucketIfNotExists();
    }

    private void createBucketIfNotExists() {
        try {
            client.headBucket(builder -> builder.bucket(bucket));
        } catch (NoSuchBucketException e) {
            client.createBucket(builder -> builder.bucket(bucket));
        }
    }

    @Override
    public Stream<String> list(@Nullable String prefix) {
        var iterable = new S3ObjectSummaryIterable(client, bucket, prefix);
        return StreamSupport.stream(iterable.spliterator(), false).map(S3Object::key);
    }

    private record S3ObjectSummaryIterable(S3Client client,
                                           String bucket,
                                           String prefix) implements Iterable<S3Object> {

        @NotNull
        @Override
        public Iterator<S3Object> iterator() {
            return new S3ObjectSummaryIterator(client, bucket, prefix);
        }

        private static class S3ObjectSummaryIterator implements Iterator<S3Object> {

            private final S3Client client;
            private ListObjectsV2Request request;
            private ListObjectsV2Response response;
            private Iterator<S3Object> objects;

            private S3ObjectSummaryIterator(S3Client client, String bucket, String prefix) {
                this.client = client;
                this.request = ListObjectsV2Request.builder().bucket(bucket).prefix(prefix).build();
            }

            @Override
            public boolean hasNext() {
                if (objects == null || (!objects.hasNext() && response.isTruncated())) {
                    response = client.listObjectsV2(request);
                    objects = response.contents().iterator();
                    this.request = request.toBuilder().continuationToken(response.continuationToken()).build();
                }
                return objects.hasNext();
            }

            @Override
            public S3Object next() {
                return objects.next();
            }
        }
    }

    @Override
    public void upload(String key, Path path) {
        client.putObject(builder -> builder.bucket(bucket).key(key), path);

        log.debug("Uploaded file to S3: {} -> {}", path, key);
    }

    @Override
    public void upload(String key, byte[] content) {
        client.putObject(builder -> builder.bucket(bucket).key(key), RequestBody.fromBytes(content));

        log.debug("Uploaded file to S3: {} bytes -> {}", content.length, key);
    }

    @Override
    public void upload(String key, InputStream is) {
        try {
            var contents = is.readAllBytes();
            client.putObject(builder -> builder.bucket(bucket).key(key), RequestBody.fromBytes(contents));

            log.debug("Uploaded file from InputStream to S3: {}", key);
        } catch (IOException e) {
            throw new UncheckedIOException("Error reading input stream", e);
        }
    }

    @Override
    public byte[] downloadAsBytes(String key) {
        var objectBytes = client.getObjectAsBytes(builder -> builder.bucket(bucket).key(key));
        var bytes = objectBytes.asByteArray();

        log.debug("Downloaded file from S3: {} -> {} bytes", key, bytes.length);

        return bytes;
    }

    @Override
    public void downloadTo(String key, OutputStream outputStream) {
        try (var responseStream = client.getObject(builder -> builder.bucket(bucket).key(key))) {
            responseStream.transferTo(outputStream);

            log.debug("Downloaded file from S3 to output stream: {}", key);
        } catch (IOException e) {
            throw new UncheckedIOException("Error downloading file:" + key, e);
        }
    }

    @Override
    public void delete(String key) {
        client.deleteObject(builder -> builder.bucket(bucket).key(key));
    }

    @Override
    public boolean exists(String key) {
        try {
            client.headObject(builder -> builder.bucket(bucket).key(key));
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    @Override
    public URL generateDownloadUrl(String key, long expirySeconds) {
        var presignedGetRequest = s3Presigner.presignGetObject(
            builder -> builder.getObjectRequest(getBuilder -> getBuilder.bucket(bucket).key(key))
                              .signatureDuration(Duration.ofSeconds(expirySeconds))
        );
        return presignedGetRequest.url();
    }
}
