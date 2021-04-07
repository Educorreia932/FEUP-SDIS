package peer.storage;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {
    public ConcurrentHashMap<String, FileInfo> backed_up_files;
    public ConcurrentHashMap<Integer, ChunkInfo> stored_chunks;
    private final int peer_id;
    public final String FILESYSTEM_FOLDER = "../filesystem/peer";
    public final String BACKUP_FOLDER = "/backup/";
    public static int MAX_CHUNK_SIZE = 64000;

    public Storage(int peer_id) {
        backed_up_files = new ConcurrentHashMap<>();
        stored_chunks = new ConcurrentHashMap<>();
        this.peer_id = peer_id;
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

        try (FileOutputStream stream = new FileOutputStream(path + '/' + chunk_no)) {
            int chunk_size = 0;

            if (body != null) { // Don't write if empty chunk
                stream.write(body);
                chunk_size = body.length;
            }
            stored_chunks.put(chunk_no, new ChunkInfo(file_id, chunk_no, chunk_size, replication_degree, peer_id));
            return true;
        }

        catch (IOException e) {
            System.err.println("ERROR: Couldn't write chunk to file.");
        }

        return false;
    }

    /**
     * Returns file chunk
     *
     * @param file_id  ID of file
     * @param chunk_no Number of chunk
     * @return Returns the if chunk its already stored. Null otherwise.
     */
    public File getStoredChunk(String file_id, int chunk_no) {
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
     * @param peer_id       Id of peer
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
     *
     * @param file_name Name of file
     * @return File information
     */
    public FileInfo getFileInfo(String file_name) {
        String file_path = FILESYSTEM_FOLDER + peer_id + '/' + file_name;

        return backed_up_files.get(file_path);
    }

    /**
     * Adds file to map of backed files
     *
     * @param path               File's path
     * @param replication_degree File's replication degree
     * @return Return BackedUpFile corresponding to path
     */
    public FileInfo addBackedUpFile(Path path, int replication_degree) {
        FileInfo file = new FileInfo(path, replication_degree);
        backed_up_files.put(path.toString(), file);

        return file;
    }

    public void deleteFile(String file_id) {
        String path = FILESYSTEM_FOLDER + peer_id + BACKUP_FOLDER + file_id;
        File folder = new File(path);

        if (folder.exists() && folder.isDirectory()) {
            // Empty directory
            File[] chunks = folder.listFiles();

            if (chunks != null) {
                int chunk_no = 0;
                for (File chunk : chunks) {
                    chunk.delete();
                    stored_chunks.remove(chunk_no);
                    chunk_no++;
                }
            }

            folder.delete(); // Delete folder
        }
    }

    public void removeBackedUpFile(FileInfo file) {
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

    public synchronized void incrementReplicationDegree(String file_id, int chunk_no, int sender_id) {
        // Updates perceived_rep_deg for BackedUpFiles
        FileInfo file = null;
        for(FileInfo f : backed_up_files.values()){
            if(f.getId().equals(file_id))
                file = f;
        }
        if(file != null) // If peer backed up the file
            file.incrementReplicationDegree(chunk_no, sender_id);


        else{
            //Updates perceived_rep-deg for stored chunks
            ChunkInfo chunk = stored_chunks.get(chunk_no);
            if(chunk != null) // If peer Has chunk
                chunk.incrementPerceivedRepDegree(sender_id);
        }
    }

    public synchronized int getPerceivedRP(String file_path, int chunk_no){
        FileInfo file = backed_up_files.get(file_path);
        if (file == null) return 0;
        return file.getPerceivedRP(chunk_no);
    }
}
