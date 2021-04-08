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
    private final AtomicLong max_space;
    private final AtomicLong used_space;
    private final ConcurrentHashMap<String, BackedUpFile> backed_up_files;
    private final ConcurrentHashMap<String, Chunk> stored_chunks;
    private final int peer_id;
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
        File chunk = getChunkFile(file_id, chunk_no);

        if (chunk != null) // Chunk already stored
            return true;

        if (isFileBackedUp(file_id).get()) //  This peer has original file
            return false;

        if (used_space.get() + body.length > max_space.get()) // No space for chunk
            return false;

        String path = getBackupFilePath(file_id);
        File directory = new File(path);

        if (!directory.exists())     // Create folder for file
            if (!directory.mkdirs()) {
                System.err.println("ERROR: Failed to create directory to store chunk.");

                return false;
            }

        try (FileOutputStream stream = new FileOutputStream(path + '/' + chunk_no)) {
            int chunk_size = 0;

            if (body.length != 0) { // Don't write if empty chunk
                stream.write(body);
                chunk_size = body.length;
            }

            String key = file_id + '/' + chunk_no;

            if (stored_chunks.put(key, new Chunk(file_id, chunk_no, chunk_size, replication_degree, peer_id)) == null)
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

    public void deleteFile(String file_id) {
        String path = getBackupFilePath(file_id);
        File folder = new File(path);

        if (folder.exists() && folder.isDirectory()) {
            // Empty directory
            File[] chunks = folder.listFiles();

            if (chunks != null) {
                int chunk_no = 0;
                for (File chunk : chunks) {
                    if (chunk.delete()) {
                        Chunk c = stored_chunks.remove(file_id + '/' + chunk_no);
                        if (c != null) // Update used space
                            used_space.set(used_space.get() - c.getSize());
                    }
                    chunk_no++;
                }
            }

            folder.delete(); // Delete folder
        }
    }

    public void removeBackedUpFile(BackedUpFile file) {
        backed_up_files.remove(file.getPath());
    }

    public void writeFile(String file_path, ArrayList<byte[]> chunks) {
        try {
            FileOutputStream stream = new FileOutputStream(file_path);

            for (byte[] chunk : chunks)
                stream.write(chunk);

            stream.flush();
        }

        catch (IOException e) {
            e.printStackTrace();
        }
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
            Chunk chunk = stored_chunks.get(file_id + '/' + chunk_no);
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

                if (file.exists() && !file.isDirectory()) { // If chunk exists
                    if (file.delete()) { // Delete chunk
                        stored_chunks.remove(entry.getKey()); // Remove from map
                        used_space.set(used_space.get() - chunk.getSize()); // Update used_space
                        return chunk;
                    }
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

    /**
     * Returns file chunk
     *
     * @param file_id  ID of file
     * @param chunk_no Number of chunk
     * @return Returns the if chunk its already stored. Null otherwise.
     */
    public File getChunkFile(String file_id, int chunk_no) {
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
     * @return File if it exists, null otherwise
     */
    public File getFile(String file_pathname) {
        String path = FILESYSTEM_FOLDER + peer_id + '/' + file_pathname.trim();
        File file = new File(path);

        if (file.exists() && !file.isDirectory())
            return file;

        return null;
    }

    /**
     * Returns file information for the path given, if it was backed up
     *
     * @param file_name Name of file
     * @return File information
     */
    public BackedUpFile getFileInfo(String file_name) {
        String file_path = FILESYSTEM_FOLDER + peer_id + '/' + file_name;

        return backed_up_files.get(file_path);
    }

    public String getBackedUpFilesInfo() {
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

    public String getBackedUpChunksInfo() {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, Chunk> entry : stored_chunks.entrySet()) {
            Chunk chunk = entry.getValue();
            result.append("ID: ").append(entry.getKey())
                    .append("\nSIZE: ").append(chunk.getSize())
                    .append("\nDESIRED RP: ").append(chunk.getDesired_rep_deg())
                    .append("\nPERCEIVED RP: ").append(chunk.getPerceived_rep_deg())
                    .append('\n');
        }

        return result.toString();
    }

    public AtomicLong getUsedSpace() {
        return used_space;
    }

    public Chunk getStoredChunk(String file_id, int chunk_no) {
        return stored_chunks.get(file_id + '/' + chunk_no);
    }

    public synchronized int getPerceivedRP(String file_path, int chunk_no) {
        BackedUpFile file = backed_up_files.get(file_path);

        if (file == null)
            return 0;

        return file.getPerceivedRP(chunk_no);
    }

    public String getBackupFilePath(String file_id) {
        return FILESYSTEM_FOLDER + peer_id + BACKUP_FOLDER + file_id;
    }

    public static String getBackupPath(int peer_id) {
        return Storage.FILESYSTEM_FOLDER + peer_id + "/storageBackup.txt";
    }

    public void setMaxSpace(long value) {
        max_space.set(value);
    }
}
