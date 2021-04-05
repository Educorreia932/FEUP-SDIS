package peer.storage;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;

public class Storage {
    private HashMap<String, BackedUpFile> backed_up_files;
    private int peer_id;
    public final String FILESYSTEM_FOLDER = "../filesystem/peer";
    public final String BACKUP_FOLDER = "/backup/";
    public static int MAX_CHUNK_SIZE = 64000;

    public Storage(int peer_id) {
        backed_up_files = new HashMap<>();
        this.peer_id = peer_id;
    }

    /**
     * Stores chunk in peer's backup folder.
     * @param file_id ID of file to be stored
     * @param chunk_no Number of chunk to be stored
     * @param body Body of chunk to be stored
     * @return True if chunk is stored, false otherwise
     */
    public boolean putChunk(String file_id, int chunk_no, byte[] body) {

        File chunk = getStoredChunk(file_id, chunk_no);
        if (chunk != null) // Chunk already stored
            return true;

        String path = FILESYSTEM_FOLDER + peer_id + BACKUP_FOLDER + file_id;
        File directory = new File(path);

        if (!directory.exists())     // Create folder for file
            if (!directory.mkdirs()) {
                System.err.println("ERROR: Failed to create directory to store chunk.");
                return false;
            }
        try {
            FileOutputStream stream = new FileOutputStream(path + '/' + chunk_no);
            if(body != null) stream.write(body); // Dont write if empty chunk
            return true;
        }
        catch (IOException e) {
            System.err.println("ERROR: Couldn't write chunk to file.");
        }

        return false;
    }

    /**
     * Returns file chunk
     * @param file_id ID of file
     * @param chunk_no Number of chunk
     * @return Returns the if chunk its already stored. Null otherwise.
     */
    public File getStoredChunk(String file_id, int chunk_no){
        String path = FILESYSTEM_FOLDER + peer_id + BACKUP_FOLDER + file_id + '/' + chunk_no;
        File file = new File(path);

        if (file.exists() && !file.isDirectory())
            return file;

        return null;
    }

    /**
     * Returns file with path equal to file_pathname
     *
     * @param file_pathname Path to file
     * @param peer_id Id of peer
     * @return File if it exists, null otherwise
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
     * Returns file information for the path given, if it was backed up
     * @param file_name Name of file
     * @return File information
     */
    public BackedUpFile getFileInfo(String file_name){
        String file_path = FILESYSTEM_FOLDER + peer_id + '/' + file_name;
        return backed_up_files.get(file_path);
    }

    /**
     * Adds file to map of backed files
     * @param path File's path
     * @return Return BackedUpFile corresponding to path
     */
    public BackedUpFile addBackedUpFile(Path path) {
        BackedUpFile file = new BackedUpFile(path);
        backed_up_files.put(path.toString(), file);
        return file;
    }

    public void deleteFile(String file_id){
        String path = FILESYSTEM_FOLDER + peer_id + BACKUP_FOLDER + file_id;
        File folder = new File(path);

        if (folder.exists() && folder.isDirectory()){
            //Empty directory
            File[] chunks = folder.listFiles();
            if(chunks != null){
                for(File chunk : chunks){
                    chunk.delete();
                }
            }
            folder.delete();
        }
    }
}
