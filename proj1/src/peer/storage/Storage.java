package peer.storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class Storage implements Serializable {
    private final int peer_id;
    private final AtomicLong max_space;
    private final AtomicLong used_space;
    private final ConcurrentHashMap<String, Chunk> stored_chunks;
    private final ConcurrentHashMap<String, BackedUpFile> backed_up_files;
    public static final String FILESYSTEM_FOLDER = "../filesystem/peer";
    public static final String BACKUP_FOLDER = "/backup/";
    public static int MAX_CHUNK_SIZE = 64000;

    public Storage(int peer_id) {
        backed_up_files = new ConcurrentHashMap<>();
        stored_chunks = new ConcurrentHashMap<>();
        this.peer_id = peer_id;
        this.max_space = new AtomicLong(Long.MAX_VALUE);
        this.used_space = new AtomicLong(0);
    }

    /**
     * Stores chunk in peer's backup folder.
     *
     * @param file_id            ID of file to be stored
     * @param chunk_no           Number of chunk to be stored
     * @param body               Body of chunk to be stored
     * @param replication_degree desired replication degree of chunk to be stored
     * @return True if chunk is stored, false otherwise
     */
    public boolean putChunk(String file_id, int chunk_no, byte[] body, int replication_degree) {
        File chunk = getFile(file_id, chunk_no);
        int chunk_size = 0;
        if(body != null)
            chunk_size = body.length;

        if (chunk != null) // Chunk already stored
            return true;

        if (isFileBackedUp(file_id).get()) //  This peer has original file - cant store chunks
            return false;

        if (used_space.get() + chunk_size > max_space.get()) // No space for chunk
            return false;

        String path = getFilePath(file_id);
        File directory = new File(path);

        if (!directory.exists())     // Create folder for file
            if (!directory.mkdirs())
                return false; // Failed to create folder to store chunk

        String chunk_path = getFilePath(file_id, chunk_no);

        try (FileOutputStream stream = new FileOutputStream(chunk_path)) {

            if (chunk_size != 0)  // Don't write if empty chunk
                stream.write(body);

            // Add to map
            if (stored_chunks.put(chunk_path, new Chunk(file_id, chunk_no, chunk_size, replication_degree, peer_id)) == null)
                used_space.set(used_space.get() + chunk_size); // Increment used space if chunk is new

            return true;
        }

        catch (IOException e) {
            System.err.println("ERROR: Couldn't write chunk to file.");
        }

        return false;
    }

    /**
     * Creates directories for peer with id: peer_id
     */
    public void makeDirectories() {
        File directory = new File(FILESYSTEM_FOLDER + peer_id + BACKUP_FOLDER);

        if (!directory.exists())
            directory.mkdirs();
    }

    public void addBackedUpFile(BackedUpFile file) {
        backed_up_files.put(file.getPath(), file);
    }

    public void deleteAllChunksFromFile(String file_id) {
        String path = getFilePath(file_id);
        File folder = new File(path);

        if (folder.exists() && folder.isDirectory()) {
            // Remove all chunks from directory
            File[] chunks = folder.listFiles();

            if (chunks != null) {
                for (int chunk_no = 0; chunk_no < chunks.length; chunk_no++) {
                    if (chunks[chunk_no].delete()) { // Delete file
                        // Delete from map
                        Chunk chunk = stored_chunks.remove(getFilePath(file_id, chunk_no));
                        if (chunk != null) // Update used space
                            used_space.set(used_space.get() - chunk.getSize());
                    }
                }
            }
            folder.delete(); // Delete folder
        }
    }

    public void removeBackedUpFile(BackedUpFile file) {
        backed_up_files.remove(file.getPath());
    }

    public synchronized void updateReplicationDegree(String file_id, int chunk_no, int sender_id, boolean increment) {
        // Updates perceived_rep_deg for BackedUpFiles
        BackedUpFile file = null;

        for (BackedUpFile f : backed_up_files.values())
            if (f.getId().equals(file_id))
                file = f;

        if (file != null) // If peer backed up the file
            if (increment) // Increment
                file.incrementReplicationDegree(chunk_no, sender_id);

            else // Decrement
                file.decrementReplicationDegree(chunk_no, sender_id);

        else {
            // Updates perceived_rep-deg for stored chunks
            Chunk chunk = stored_chunks.get(getFilePath(file_id, chunk_no));
            if (chunk != null) // If peer has chunk
                if (increment) // Increment
                    chunk.incrementPerceivedRepDegree(sender_id);

                else // Decrement
                    chunk.decrementPerceivedRepDegree(sender_id);
        }
    }

    public AtomicBoolean isFileBackedUp(String file_id) {
        return new AtomicBoolean(backed_up_files.containsValue(new BackedUpFile(file_id)));
    }

    public Chunk removeRandomChunk() {
        for (Map.Entry<String, Chunk> entry : stored_chunks.entrySet()) {
            Chunk chunk = entry.getValue();
            if (chunk.getSize() > 0) { // Remove chunk if size of chunk > 0
                String path = FILESYSTEM_FOLDER + peer_id + BACKUP_FOLDER + chunk.getFile_id()
                        + '/' + chunk.getChunk_no(); // Get chunk path
                File file = new File(path);

                if (file.exists() && !file.isDirectory() && file.delete()) { // If chunk existed ans was deleted
                    stored_chunks.remove(entry.getKey()); // Remove from map
                    used_space.set(used_space.get() - chunk.getSize()); // Update used_space
                    return chunk;
                }
            }
        }
        return null;
    }

    public String wasFileModified(String file_path, String new_file_id) {
        BackedUpFile old_file = backed_up_files.get(file_path);

        if (old_file != null && !old_file.getId().equals(new_file_id))
            return old_file.getId();

        return null;
    }

    /* -- GETTERS -- */

    public File getFile(String file_id, int chunk_no) {
        String path = getFilePath(file_id, chunk_no);
        File file = new File(path);

        if (file.exists() && !file.isDirectory())
            return file;

        return null;
    }

    public File getFile(String file_pathname) {
        String path = FILESYSTEM_FOLDER + peer_id + '/' + file_pathname.trim();
        File file = new File(path);

        if (file.exists() && !file.isDirectory())
            return file;

        return null;
    }

    public BackedUpFile getFileInfo(String file_name) {
        String file_path = FILESYSTEM_FOLDER + peer_id + '/' + file_name;
        return backed_up_files.get(file_path);
    }

    public String getBackedUpFilesState() {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, BackedUpFile> entry : backed_up_files.entrySet()) {
            BackedUpFile file = entry.getValue();
            result.append("PATH: ").append(file.getPath())
                    .append("\nID: ").append(file.getId())
                    .append("\nDESIRED RP: ").append(file.getDesired_replication_degree())
                    .append('\n');

            for (int chunk_no = 0; chunk_no < file.getNumberOfChunks(); chunk_no++)
                result.append("CHUNK: ").append(chunk_no)
                        .append(" - Perceived RP: ").append(file.getPerceivedRP(chunk_no))
                        .append('\n');

            result.append('\n');
        }

        return result.toString();
    }

    public String getBackedUpChunksState() {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, Chunk> entry : stored_chunks.entrySet()) {
            Chunk chunk = entry.getValue();
            result.append("ID: ").append(entry.getKey())
                    .append("\nSIZE: ").append(chunk.getSize())
                    .append("\nDESIRED RP: ").append(chunk.getDesired_rep_deg())
                    .append("\nPERCEIVED RP: ").append(chunk.getPerceivedRP())
                    .append('\n');
        }

        return result.toString();
    }

    public AtomicLong getUsedSpace() {
        return used_space;
    }

    public Chunk getStoredChunk(String file_id, int chunk_no) {
        return stored_chunks.get(getFilePath(file_id, chunk_no));
    }

    public synchronized int getPerceivedRP(String file_path, int chunk_no) {
        BackedUpFile file = backed_up_files.get(file_path);
        if (file != null)
            return file.getPerceivedRP(chunk_no);

        Chunk chunk = stored_chunks.get(file_path);
        if(chunk != null)
            return chunk.getPerceivedRP();

        return 0;
    }

    public String getFilePath(String file_id) {
        return FILESYSTEM_FOLDER + peer_id + BACKUP_FOLDER + file_id;
    }

    public String getFilePath(String file_id, int chunk_no) {
        return FILESYSTEM_FOLDER + peer_id + BACKUP_FOLDER + file_id + '/' + chunk_no;
    }

    public static String getStoragePath(int peer_id) {
        return Storage.FILESYSTEM_FOLDER + peer_id + "/storageBackup.txt";
    }

    /* -- SETTERS -- */

    public void setMaxSpace(long value) {
        max_space.set(value);
    }
}
