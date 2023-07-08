package dev.ocpd.oss;

import org.springframework.lang.Nullable;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface FileStore {


    /**
     * List all files in the file store.
     * Note: This method will return all file keys in the file store, which may be a very large number.
     * It must be used within a try-with-resources statement or similar control structure to ensure that the stream's underlying resources are promptly released.
     *
     * <pre class="code">
     *     {@literal @}Test
     *     public void listAllFiles() {
     *         try (Stream<String> stream = fileStore.list()) {
     *             stream.forEach(System.out::println);
     *          }
     *     }
     * </pre>
     *
     * @param prefix The prefix of the file keys to be listed.
     * @return A stream of file keys.
     */
    Stream<String> list(@Nullable String prefix);


    /**
     * Upload a file to the file store.
     *
     * @param key  The key of the file to be uploaded.
     * @param path The path of the file to be uploaded.
     */
    void upload(String key, Path path);

    /**
     * Upload a file to the file store.
     *
     * @param key     The key of the file to be uploaded.
     * @param content The content of the file to be uploaded.
     */
    void upload(String key, byte[] content);

    /**
     * Upload a file to the file store.
     *
     * @param key The key of the file to be uploaded.
     * @param is  The input stream of the file to be uploaded, which will not be closed.
     */
    void upload(String key, InputStream is);

    /**
     * Download a file from the file store.
     *
     * @param key The key of the file to be downloaded.
     * @return The content of the file.
     */
    byte[] downloadAsBytes(String key);

    /**
     * Download a file from the file store.
     *
     * @param key          The key of the file to be downloaded.
     * @param outputStream The output stream to write the file content.
     */
    void downloadTo(String key, OutputStream outputStream);

    /**
     * Delete a file from the file store.
     *
     * @param key The key of the file to be deleted.
     */
    void delete(String key);

    /**
     * Check if a file exists in the file store.
     *
     * @param key The key of the file to be checked.
     * @return True if the file exists, false otherwise.
     */
    boolean exists(String key);

    /**
     * Generate the signed url for downloading a file.
     * Note: The url can only be used to download the file, not for uploading or other operations. The url will be expired after the specified seconds.
     *
     * @param key           The key of the file.
     * @param expirySeconds The seconds before the url expires.
     * @return The signed url.
     */
    URL generateDownloadUrl(String key, long expirySeconds);

    /**
     * Generate the signed url for downloading a file. The url will be expired after 10 minutes.
     *
     * @param key The key of the file.
     * @return The signed url.
     */
    default URL generateDownloadUrl(String key) {
        return generateDownloadUrl(key, 600);
    }
}
