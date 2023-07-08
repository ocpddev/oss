package dev.ocpd.oss;

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
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

public class LocalFileStore implements FileStore {

    private final Logger log = LoggerFactory.getLogger(LocalFileStore.class);

    private final Path root;

    public LocalFileStore(String rootPath) {
        this.root = Path.of(rootPath);

        try {
            Files.createDirectories(this.root);
        } catch (IOException e) {
            throw new UncheckedIOException("Error creating local file system root directory", e);
        }
    }

    @Override
    public Stream<String> list(@Nullable String prefix) {
        try {
            var dirToWalk = prefix != null ? this.root.resolve(prefix) : this.root;
            if (!Files.exists(dirToWalk)) {
                return Stream.empty();
            }

            return Files.walk(dirToWalk)
                        .filter(Files::isRegularFile)
                        .map(this.root::relativize)
                        .map(Path::toString);
        } catch (IOException e) {
            throw new UncheckedIOException("Error listing files from local file system", e);
        }
    }

    @Override
    public void upload(String key, Path path) {
        try {
            var destinationFile = this.root.resolve(key);
            Files.createDirectories(destinationFile.getParent());
            Files.copy(path, destinationFile, StandardCopyOption.REPLACE_EXISTING);

            log.debug("Uploaded file: {} to local file system: {}", path, destinationFile);
        } catch (IOException e) {
            throw new UncheckedIOException("Error uploading file: %s to %s".formatted(path, key), e);
        }
    }

    @Override
    public void upload(String key, byte[] content) {
        try {
            var destinationFile = this.root.resolve(key);
            Files.createDirectories(destinationFile.getParent());
            Files.write(destinationFile, content, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

            log.debug("Uploaded file {} bytes to local file system: {}", content.length, destinationFile);
        } catch (IOException e) {
            throw new UncheckedIOException("Error uploading file:" + key, e);
        }
    }

    @Override
    public void upload(String key, InputStream is) {
        try {
            var destinationFile = this.root.resolve(key);
            Files.createDirectories(destinationFile.getParent());
            Files.copy(is, destinationFile, StandardCopyOption.REPLACE_EXISTING);

            log.debug("Uploaded file from input stream to local file system: {}", destinationFile);
        } catch (IOException e) {
            throw new UncheckedIOException("Error uploading file: " + key, e);
        }
    }

    @Override
    public byte[] downloadAsBytes(String key) {
        try {
            var file = this.root.resolve(key);
            var bytes = Files.readAllBytes(file);

            log.debug("Downloaded file: {} from local file system: {} with size: {} bytes", key, file, bytes.length);

            return bytes;
        } catch (IOException e) {
            throw new UncheckedIOException("Error downloading file: " + key, e);
        }
    }

    @Override
    public void downloadTo(String key, OutputStream outputStream) {
        try {
            var file = this.root.resolve(key);
            Files.copy(file, outputStream);

            log.debug("Downloaded file: {} from local file system: {} to output stream", key, file);
        } catch (IOException e) {
            throw new UncheckedIOException("Error downloading file: " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            var file = this.root.resolve(key);

            if (Files.deleteIfExists(file)) {
                log.debug("Deleted file: {} from local file system: {}", key, file);
            } else {
                log.warn("File: {} does not exist in local file system: {}", key, file);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Error deleting file: " + key, e);
        }
    }

    @Override
    public boolean exists(String key) {
        var file = this.root.resolve(key);
        return Files.exists(file) && Files.isRegularFile(file);
    }

    @Override
    public URL generateDownloadUrl(String key, long expirySeconds) {
        throw new UnsupportedOperationException("Local file store does not support download URL");
    }
}
