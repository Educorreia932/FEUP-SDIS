package Peer.Storage;

import java.io.File;
import java.io.FileNotFoundException;
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

public class Storage{
    private HashMap<String, String> backedup_files;
    private List<Chunk> chunks;
    private int peer_id;
    private final String FILESYSTEM_FOLDER = "../filesystem/peer";
    private final String BACKUP_FOLDER = "/backup/";

    public Storage(int peer_id){
        backedup_files = new HashMap<String, String>();
        this.chunks = new ArrayList<>();
        this.peer_id = peer_id;
    }

    public void execute(String[] header, byte[] body){
        //String version = header[0];
        String msg_type = header[1];
        //String sender_id = header[2];
        String file_id = header[3];
        int chunkno, replication_degree;

        switch(msg_type){
            case "PUTCHUNK":
                chunkno = Integer.parseInt(header[4]);
                //replication_degree = Integer.parseInt(header[5]);
                putChunk(file_id, chunkno, body);
                break;
            case "STORED":
            case "GETCHUNK":
            case "CHUNK":
            case "REMOVED":
            case "DELETE":
                System.out.println("Not implemented");
                break;
        }
    }

    private void putChunk(String file_id, int chunkno, byte[] body){
        System.out.println("Saving chunk...");
        Chunk chunk = new Chunk(file_id, chunkno, body);
        chunks.add(chunk);
        storeChunk(chunk);
    }

    public void storeChunk(Chunk chunk){
        String path = FILESYSTEM_FOLDER + peer_id + BACKUP_FOLDER + chunk.getFileId();
        File directory = new File(path);
        if(!directory.exists())     // Create folder for file
            directory.mkdirs();

        String file_path = path + '/' + chunk.getChunkNo();
        try {
            FileOutputStream stream = new FileOutputStream(file_path);
            stream.write(chunk.getBody());
        } catch (IOException e) {
            System.err.println("ERROR: Couldn't write chunk to file.");
        }
        System.out.println("Stored chunk.");
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
     */
    public void makeDirectories(){
        File directory = new File(FILESYSTEM_FOLDER + peer_id + BACKUP_FOLDER);
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

    public String addBackedUpFile(Path path){
        String file_id = getMetadataString(path);
        String hashed_id = hash(file_id);
        backedup_files.put(path.toString(), hashed_id);
        return hashed_id;
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
