package peer.storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Storage {
    private HashMap<String, String> backedup_files;
    public List<Chunk> chunks;
    private int peer_id;
    public final String FILESYSTEM_FOLDER = "../filesystem/peer";
    public final String BACKUP_FOLDER = "/backup/";


    public Storage(int peer_id) {
        backedup_files = new HashMap<>();
        this.chunks = new ArrayList<>();
        this.peer_id = peer_id;
    }

    /**
     * Returns file with path equal to file_pathname
     *
     * @param file_pathname
     * @param peer_id
     * @return File
     */
    public File getFile(String file_pathname, int peer_id) {
        String path = FILESYSTEM_FOLDER + peer_id + '/' + file_pathname.trim();
        File file = new File(path);
        if (file.exists() && !file.isDirectory()) {
            return file;
        }
        return null;
    }

    /**
     * Creates directories for peer with id: peer_id
     */
    public void makeDirectories() {
        File directory = new File(FILESYSTEM_FOLDER + peer_id + BACKUP_FOLDER);
        if (!directory.exists())
            directory.mkdirs();
    }

    /**
     * Hashes string passed in argument
     *
     * @param toBeHashed
     * @return
     */
    public String hash(String toBeHashed) {
        MessageDigest digest = null;
        // SHA-256
        try {
            digest = MessageDigest.getInstance("SHA-256");

        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] encoded_hash = digest.digest(
                toBeHashed.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encoded_hash);
    }

    public String addBackedUpFile(Path path) {
        String file_id = getMetadataString(path);
        String hashed_id = hash(file_id);
        backedup_files.put(path.toString(), hashed_id);
        return hashed_id;
    }

    /**
     * Returns a string that contains metadata about a file
     *
     * @param path
     * @return
     */
    private String getMetadataString(Path path) {
        String modified_time = "", owner = "",
                name = path.toString();
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
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
