package peer.storage;

import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class Storage implements Serializable {
    private final String version;
    private final int peer_id;
    public final AtomicLong max_space;
    public AtomicLong used_space;
    private final ConcurrentHashMap<String, Chunk> stored_chunks;
    private final ConcurrentHashMap<String, BackedUpFile> backed_up_files;
    public static final String FILESYSTEM_FOLDER = "./filesystem/peer";
    public static final String BACKUP_FOLDER = "/backup/";
    public static int MAX_CHUNK_SIZE = 64000;
    private Set<String> deleted_files; // For enhanced delete:

    public Storage(int peer_id, String version) {
        backed_up_files = new ConcurrentHashMap<>();
        stored_chunks = new ConcurrentHashMap<>();
        this.peer_id = peer_id;
        this.max_space = new AtomicLong(Long.MAX_VALUE);
        this.used_space = new AtomicLong(0);
        this.version = version;

        if(version.equals("2.0"))
            deleted_files = ConcurrentHashMap.newKeySet();
    }

    public void makeDirectories() {
        File directory = new File(FILESYSTEM_FOLDER + peer_id + BACKUP_FOLDER);

        if (!directory.exists())
            directory.mkdirs();
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
            if (chunk != null) { // If peer has chunk
                if (increment) // Increment
                    chunk.incrementPerceivedRepDegree(sender_id);

                else // Decrement
                    chunk.decrementPerceivedRepDegree(sender_id);
            }
        }
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

    /* -- SETTERS & ADDS/REMOVES -- */

    public void setMaxSpace(long value) {
        max_space.set(value);
    }

    public void addBackedUpFile(BackedUpFile file) {
        backed_up_files.put(file.getPath(), file);
        if(version.equals("2.0"))
            deleted_files.remove(file.getId());
    }

    public void addStoredChunk(String key, Chunk value){
        if(stored_chunks.put(key, value) == null)
            used_space.set(used_space.get() + value.getSize()); // Updates space if chunk is new
    }

    public void removeBackedUpFile(BackedUpFile file) {
        backed_up_files.remove(file.getPath());
        if(version.equals("2.0"))
            deleted_files.add(file.getId());
    }

    public Chunk removeRandomChunk() {
        for (Map.Entry<String, Chunk> entry : stored_chunks.entrySet()) {
            Chunk chunk = entry.getValue();
            String path = FILESYSTEM_FOLDER + peer_id + BACKUP_FOLDER + chunk.getFile_id()
                    + '/' + chunk.getChunk_no(); // Get chunk path
            File file = new File(path);

            if (file.exists() && !file.isDirectory() && file.delete()) { // If chunk existed ans was deleted
                removeStoredChunk(entry.getKey()); // Remove from map
                return chunk;
            }
        }
        return null;
    }

    public void removeStoredChunk(String path){
        // Remove from map
        Chunk chunk = stored_chunks.remove(path);
        if (chunk != null) // Update used space
            used_space.set(used_space.get() - chunk.getSize());
    }

    public void removeStoredChunk(String file_id, int chunk_no){
        // Remove from map
        Chunk chunk = stored_chunks.remove(getFilePath(file_id, chunk_no));

        if (chunk != null) // Update used space
            used_space.set(used_space.get() - chunk.getSize());
    }

    /* BOOLEAN Functions */

    private boolean isThereAvailableSpace(int chunk_size){
        return used_space.get() + chunk_size < max_space.get() && max_space.get() != 0;
    }

    public AtomicBoolean isFileBackedUp(String file_id) {
        return new AtomicBoolean(backed_up_files.containsValue(new BackedUpFile(file_id)));
    }

    public boolean isChunkStored(String file_id, int chunk_no){
        return (getFile(file_id, chunk_no) != null);
    }

    public boolean canStoreChunk(String file_id, int chunk_size){
        // This peer has original file - cant store chunks & No space for chunk
        return (!isFileBackedUp(file_id).get() && isThereAvailableSpace(chunk_size));
    }

    public Set<String> getDeletedFiles() {
        return deleted_files;
    }
}
