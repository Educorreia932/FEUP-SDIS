package peer.storage;

import channels.MC_Channel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class StorageThread implements Runnable{
    private String[] header;
    private byte[] body;
    private MC_Channel mc_channel;
    private Storage storage;
    int peer_id;

    public StorageThread(String[] header, byte[] body, MC_Channel mc_channel, Storage storage, int peer_id) {
        this.header = header;
        this.body = body;
        this.mc_channel = mc_channel;
        this.storage = storage;
        this.peer_id = peer_id;

    }

    @Override
    public void run() {

        //String version = header[0];
        String msg_type = header[1];
        //String sender_id = header[2];
        String file_id = header[3];
        int chunk_no, replication_degree;

        switch (msg_type) {
            case "PUTCHUNK":
                chunk_no = Integer.parseInt(header[4]);
                //replication_degree = Integer.parseInt(header[5]);

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

    /**
     * Stores chunk
     * @param file_id Id of file to be stored
     * @param chunk_no Number of chunk to be stored
     * @param body Body of chunk to be stored
     * @return true if chunk is stored, false otherwise
     */
    private boolean putChunk(String file_id, int chunk_no, byte[] body) {
        System.out.println("Received PUTCHUNK message.");
        Chunk chunk = new Chunk(file_id, chunk_no, body);

        if (storage.chunks.contains(chunk)) // Chunk already stored
            return true;

        if (storeChunk(chunk)) {
            storage.chunks.add(chunk); // Add to list of stored chunks
            System.out.println("Stored chunk.");
            return true;
        }

        return false;
    }

    /**
     * Stores chunk in peer's backup folder.
     * @param chunk to be stored
     * @return True if successful, false otherwise
     */
    public boolean storeChunk(Chunk chunk) {
        String path = storage.FILESYSTEM_FOLDER + peer_id + storage.BACKUP_FOLDER + chunk.getFileId();
        File directory = new File(path);

        if (!directory.exists())     // Create folder for file
            if (!directory.mkdirs()) {
                System.err.println("ERROR: Failed to create directory to store chunk.");
                return false;
            }

        String file_path = path + '/' + chunk.getChunkNo();

        try {
            FileOutputStream stream = new FileOutputStream(file_path);
            stream.write(chunk.getBody());

            return true;
        }

        catch (IOException e) {
            System.err.println("ERROR: Couldn't write chunk to file.");
        }

        return false;
    }
}
