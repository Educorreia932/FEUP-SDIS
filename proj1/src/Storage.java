import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Storage {
    private List<String> backedup_files;
    private static Storage instance;
    public Storage(){
        backedup_files = new ArrayList<>();
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
     * @return File
     */
    public File getFile(String file_pathname){
        File file = new File(file_pathname.trim());
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
        File directory = new File("filesystem/peer" + peer_id + "/backup/");
        if(!directory.exists())
            directory.mkdirs();
    }

    /**
     * Hashes string passed in argument
     * @param tobeHashed
     * @return
     */
    public String hash(String tobeHashed) {
        MessageDigest digest = null;
        // SHA-256
        try {
            digest = MessageDigest.getInstance("SHA-256");

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] encoded_hash = digest.digest(
                tobeHashed.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encoded_hash);
    }


    public void addBackedUpFile(Path path){
        String file_id = getMetadataString(path);
        String hashed_id = hash(file_id);
        // TODO: Check if already hashed
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
}
