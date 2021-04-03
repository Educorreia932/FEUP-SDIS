package peer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class Storage {
    private HashMap<String, String> backed_up_files;
    private int peer_id;
    public final String FILESYSTEM_FOLDER = "../filesystem/peer";
    public final String BACKUP_FOLDER = "/backup/";
    public static int MAX_CHUNK_SIZE = 64000;

    public Storage(int peer_id) {
        backed_up_files = new HashMap<>();
        this.peer_id = peer_id;
    }

    /**
     * Retrieves chunk if stored.
     * @param file_id
     * @param chunk_no
     * @return
     */
    public int getChunk(String file_id, int chunk_no) {

        if (isStoredChunk(file_id, chunk_no)){
            System.out.println("Reading chunk");
        }
        return -1;
    }

    /**
     * Stores chunk in peer's backup folder.
     * @param file_id ID of file to be stored
     * @param chunk_no Number of chunk to be stored
     * @param body Body of chunk to be stored
     * @return True if chunk is stored, false otherwise
     */
    public boolean putChunk(String file_id, int chunk_no, byte[] body) {

        if (isStoredChunk(file_id, chunk_no)) // Chunk already stored
            return true;

        String path = FILESYSTEM_FOLDER + peer_id + BACKUP_FOLDER + file_id;
        File directory = new File(path);

        if (!directory.exists())     // Create folder for file
            if (!directory.mkdirs()) {
                System.err.println("ERROR: Failed to create directory to store chunk.");
                return false;
            }

        try {
            FileOutputStream stream = new FileOutputStream(path);
            stream.write(body);
            return true;
        }
        catch (IOException e) {
            System.err.println("ERROR: Couldn't write chunk to file.");
        }

        return false;
    }

    /**
     * Checks if a chunk is already stored
     * @param file_id ID of file
     * @param chunk_no Number of chunk
     * @return Returns true if chunk is already stored. False otherwise.
     */
    public boolean isStoredChunk(String file_id, int chunk_no){
        String path = FILESYSTEM_FOLDER + peer_id + BACKUP_FOLDER + file_id + '/' + chunk_no;
        File file = new File(path);

        return file.exists() && !file.isDirectory();
    }

    /**
     * Returns file with path equal to file_pathname
     *
     * @param file_pathname Path to file
     * @param peer_id Id of peer
     * @return File
     */
    public File getFile(String file_pathname, int peer_id) {
        String path = FILESYSTEM_FOLDER + peer_id + '/' + file_pathname.trim();
        File file = new File(path);

        if (file.exists() && !file.isDirectory())
            return file;

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
     * Enscrypts file data string using SHA-256
     *
     * @param toBeHashed String to be hashed
     * @return File identifier
     */
    private static String hash(String toBeHashed) {
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
     * Returns file_id for the backed up file name
     * @param file_name Name of file
     * @return File ID
     */
    public String getFileId(String file_name){
        String file_path = FILESYSTEM_FOLDER + peer_id + '/' + file_name;
        return backed_up_files.get(file_path);
    }

    /**
     * Adds file to map of backed files
     * @param path File's path
     * @return Return hashed id of file
     */
    public String addBackedUpFile(Path path) {
        String file_id = getMetadataString(path);
        String hashed_id = hash(file_id);
        backed_up_files.put(path.toString(), hashed_id);
        return hashed_id;
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
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);

        for (byte b : hash) {
            String hex = Integer.toHexString(0xFF & b);

            if (hex.length() == 1)
                hexString.append('0');

            hexString.append(hex);
        }

        return hexString.toString();
    }
}
