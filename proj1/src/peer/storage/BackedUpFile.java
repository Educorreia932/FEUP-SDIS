package peer.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class BackedUpFile {
    private String file_id;
    private int number_of_chunks;
    private String path;
    private int replication_degree;

    public BackedUpFile(Path path, int replication_degree){
        this.path = path.toString();
        String id = getMetadataString(path);
        this.file_id = hash(id);
        this.replication_degree = replication_degree;

        try {
            this.number_of_chunks = calculateNumOfChunks(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int calculateNumOfChunks(Path path) throws IOException {
        long bytes = Files.size(path);
        return (int)(bytes / Storage.MAX_CHUNK_SIZE + 1);
    }

    public String getPath(){
        return path;
    }

    public int getNumberOfChunks() {
        return number_of_chunks;
    }

    public String getId(){
        return file_id;
    }

    public int getReplication_degree() {
        return replication_degree;
    }

    /**
     * Enscrypts file data string using SHA-256
     *
     * @param toBeHashed String to be hashed
     * @return File identifier
     */
    private String hash(String toBeHashed) {
        MessageDigest digest = null;

        try {
            digest = MessageDigest.getInstance("SHA-256");
        }

        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        byte[] encoded_hash = digest.digest(toBeHashed.getBytes(StandardCharsets.UTF_8));

        return bytesToHex(encoded_hash);
    }

    /**
     * Returns a string that contains metadata about a file
     *
     * @param path Path to file
     * @return String containg the named, modified time and owner of the file
     */
    private String getMetadataString(Path path) {
        String modified_time = "", owner = "", name = path.toString();

        // Get metadata
        try {
            modified_time = Files.getLastModifiedTime(path).toString();
            owner = Files.getOwner(path).getName();
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        return name + modified_time + owner;
    }

    /**
     * Convert from byte array to hexadecimal.
     * From: https://www.baeldung.com/sha-256-hashing-java
     */
    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);

        for (byte b : hash) {
            String hex = Integer.toHexString(0xFF & b);

            if (hex.length() == 1)
                hexString.append('0');

            hexString.append(hex);
        }

        return hexString.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BackedUpFile that = (BackedUpFile) o;
        return file_id.equals(that.file_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file_id);
    }
}
