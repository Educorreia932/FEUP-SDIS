package MessageHandlers;

import channels.MC_Channel;
import messages.PutChunkMessage;
import messages.StoredMessage;
import peer.Peer;
import peer.storage.Chunk;
import peer.storage.Storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class PutChunkMessageHandler implements Runnable{
    private final byte[] body;
    private final int chunk_no;
    private final int replication_degree;
    private final String version;
    private final String file_id;
    private final Storage storage;
    private final MC_Channel control_channel;
    private final Peer peer;

    public PutChunkMessageHandler(PutChunkMessage put_chunk_msg, byte[] body, Peer peer){
        chunk_no = put_chunk_msg.getChunk_no();
        replication_degree = put_chunk_msg.getReplication_degree();
        version =  put_chunk_msg.getVersion();
        file_id = put_chunk_msg.getFile_id();
        this.body = body;
        storage = peer.storage;
        control_channel = peer.getControl_channel();
        this.peer = peer;
    }

    @Override
    public void run() {
        // If store was successful send STORED
        if (putChunk()) {
            peer.saveStorage(); // Update storage

            try { // Sleep (0-400ms)
                Thread.sleep(new Random().nextInt(400));
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Send STORED msg
            StoredMessage store_msg = new StoredMessage(version, peer.id, file_id, chunk_no);
            control_channel.send(store_msg.getBytes(null, 0));
            System.out.printf("< Peer %d sent: %s\n", peer.id, store_msg.toString()); // Log
        }
    }

    private boolean putChunk() {
        // Set chunk_size
        int chunk_size = 0;
        if(body != null) // Chunk is not empty
            chunk_size = body.length;

        // Checks
        if (storage.isChunkStored(file_id, chunk_no))
            return true; // Chunk already stored

        if(!storage.canStoreChunk(file_id,chunk_size))
            return false; // Can't store chunk

        // Make directory
        File directory = new File(storage.getFilePath(file_id));
        if (!directory.exists())  // Create folder for file
            if (!directory.mkdirs())
                return false; // Failed to create folder to store chunk

        String chunk_path = storage.getFilePath(file_id, chunk_no);
        try (FileOutputStream stream = new FileOutputStream(chunk_path)) {

            if (chunk_size != 0)  // Don't write if empty chunk
                stream.write(body);

            // Add to map
            storage.addStoredChunk(chunk_path, new Chunk(file_id, chunk_no, chunk_size, replication_degree, peer.id));
            return true;
        }
        catch (IOException e) {
            System.err.println("ERROR: Couldn't write chunk to file.");
            return false;
        }
    }
}
