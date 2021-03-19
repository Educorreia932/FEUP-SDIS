import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Storage {
    private Set<String> backedup_files;
    private static Storage instance;
    private final String FILESYSTEM_FOLDER = "../filesystem/peer";

    public Storage(){
        backedup_files = new HashSet<String>();
    }

    public static Storage getInstance() {
        if(instance == null){
            instance = new Storage();
        }
        return instance;
    }

    /**
     * Returns file with path equal to file_pathname
     * @param file_pathname
     * @param peer_id
     * @return File
     */
    public File getFile(String file_pathname, int peer_id){
        String path = FILESYSTEM_FOLDER + peer_id + '/' + file_pathname.trim();
        File file = new File(path);
        if (file.exists() && !file.isDirectory()){
            return file;
        }
        return null;
    }

    /**
     * Creates directories for peer with id: peer_id
     * @param peer_id
     */
    public void makeDirectories(int peer_id){
        File directory = new File(FILESYSTEM_FOLDER + peer_id + "/backup/");
        if(!directory.exists())
            directory.mkdirs();
    }

    /**
     * Hashes string passed in argument
     * @param toBeHashed
     * @return
     */
    public String hash(String toBeHashed) {
        MessageDigest digest = null;
        // SHA-256
        try {
            digest = MessageDigest.getInstance("SHA-256");

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] encoded_hash = digest.digest(
                toBeHashed.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encoded_hash);
    }


    public void addBackedUpFile(Path path){
        String file_id = getMetadataString(path);
        String hashed_id = hash(file_id);
        backedup_files.add(hashed_id);
    }

    /**
     * Returns a string that contains metadata about a file
     * @param path
     * @return
     */
    private String getMetadataString(Path path){
        String modified_time = "", owner = "",
                name = path.toString();
        // Get metadata
        try {
            modified_time = Files.getLastModifiedTime(path).toString();
            owner = Files.getOwner(path).getName();
        } catch (IOException e) {
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
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public void getChunks(File file){

    }
}
