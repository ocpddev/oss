package dev.ocpd.oss

import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.nio.file.Path
import java.util.stream.Stream

interface FileStore {

    /**
     * List all files in the file store.
     * Note: This method will return all file keys in the file store, which may be a very large number.
     * It must be used within a try-with-resources statement or similar control structure to ensure that the stream's underlying resources are promptly released.
     *
     * @param prefix The prefix of the file keys to be listed.
     * @return A stream of file keys.
     */
    fun list(prefix: String?): Stream<String>

    /**
     * Upload a file to the file store.
     *
     * @param key  The key of the file to be uploaded.
     * @param path The path of the file to be uploaded.
     */
    fun upload(key: String, path: Path)

    /**
     * Upload a file to the file store.
     *
     * @param key     The key of the file to be uploaded.
     * @param content The content of the file to be uploaded.
     */
    fun upload(key: String, content: ByteArray)

    /**
     * Upload a file to the file store.
     *
     * @param key The key of the file to be uploaded.
     * @param ins  The input stream of the file to be uploaded, which will not be closed.
     */
    fun upload(key: String, ins: InputStream)

    /**
     * Download a file from the file store.
     *
     * @param key The key of the file to be downloaded.
     * @return The content of the file.
     */
    fun downloadAsBytes(key: String): ByteArray

    /**
     * Download a file from the file store.
     *
     * @param key          The key of the file to be downloaded.
     * @param outputStream The output stream to write the file content.
     */
    fun downloadTo(key: String, outputStream: OutputStream)

    /**
     * Delete a file from the file store.
     *
     * @param key The key of the file to be deleted.
     */
    fun delete(key: String)

    /**
     * Check if a file exists in the file store.
     *
     * @param key The key of the file to be checked.
     * @return True if the file exists, false otherwise.
     */
    fun exists(key: String): Boolean

    /**
     * Generate the signed url for downloading a file.
     * Note: The url can only be used to download the file, not for uploading or other operations. The url will be expired after the specified seconds.
     *
     * @param key           The key of the file.
     * @param expirySeconds The seconds before the url expires.
     * @return The signed url.
     */
    fun generateDownloadUrl(key: String, expirySeconds: Long): URL

    /**
     * Generate the signed url for downloading a file. The url will be expired after 10 minutes.
     *
     * @param key The key of the file.
     * @return The signed url.
     */
    fun generateDownloadUrl(key: String): URL {
        return generateDownloadUrl(key, 600)
    }
}
