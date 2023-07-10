package dev.ocpd.oss;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class GcsFileStore implements FileStore {

    private final Logger log = LoggerFactory.getLogger(GcsFileStore.class);

    private final Storage storage;
    private final String bucket;

    public GcsFileStore(Storage storage, String bucket) {
        this.storage = storage;
        this.bucket = bucket;
    }

    @Override
    public Stream<String> list(@Nullable String prefix) {
        var bucket = storage.get(this.bucket);
        var blobs = prefix != null ? bucket.list(Storage.BlobListOption.prefix(prefix)) : bucket.list();

        return StreamSupport.stream(blobs.iterateAll().spliterator(), false)
                            .map(Blob::getName);
    }

    @Override
    public void upload(String key, Path path) {
        try {
            var blobInfo = BlobInfo.newBuilder(bucket, key).build();
            storage.create(blobInfo, Files.readAllBytes(path));

            log.debug("Uploaded file: {} with key: {}", path, key);
        } catch (IOException e) {
            throw new UncheckedIOException("Error uploading file from file: " + path, e);
        }
    }

    @Override
    public void upload(String key, byte[] content) {
        var blobInfo = BlobInfo.newBuilder(bucket, key).build();
        storage.create(blobInfo, content);

        log.debug("Uploaded file with key: {}", key);
    }

    @Override
    public void upload(String key, InputStream is) {
        var blobInfo = BlobInfo.newBuilder(bucket, key).build();
        try {
            var fileContent = ByteStreams.toByteArray(is);
            storage.create(blobInfo, fileContent);

            log.debug("Upload file from input stream with key: {}", key);
        } catch (IOException e) {
            throw new UncheckedIOException("Error uploading file from input stream", e);
        }
    }

    @Override
    public byte[] downloadAsBytes(String key) {
        var fileContent = storage.readAllBytes(bucket, key);

        log.debug("Downloaded file with key: {}", key);

        return fileContent;
    }

    @Override
    public void downloadTo(String key, OutputStream outputStream) {
        var blob = storage.get(bucket, key);
        if (blob != null) {
            blob.downloadTo(outputStream);

            log.debug("Downloaded file with key: {} to output stream", key);
        } else {
            throw new IllegalArgumentException("File does not exist: " + key);
        }
    }

    @Override
    public void delete(String key) {
        if (!storage.delete(bucket, key)) {
            log.warn("File does not exist: " + key);
        }
    }

    @Override
    public boolean exists(String key) {
        return storage.get(bucket, key) != null;
    }

    @Override
    public URL generateDownloadUrl(String key, long expirySeconds) {
        var blobInfo = BlobInfo.newBuilder(bucket, key).build();

        return storage.signUrl(
            blobInfo,
            expirySeconds,
            TimeUnit.SECONDS,
            Storage.SignUrlOption.withV4Signature()
        );
    }
}
