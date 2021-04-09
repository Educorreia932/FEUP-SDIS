package MessageHandlers;

import channels.MDR_Channel;
import messages.ChunkMessage;
import messages.GetChunkMessage;
import peer.Peer;
import peer.storage.Storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

public class GetChunkMessageHandler implements Runnable{
    private String file_id;
    private int chunk_no;
    private int peer_id;
    private String version;
    private Storage storage;
    private MDR_Channel restore_channel;

    public GetChunkMessageHandler(GetChunkMessage get_chunk_msg, Peer peer){
        file_id = get_chunk_msg.getFile_id();
        chunk_no = get_chunk_msg.getChunk_no();
        version = get_chunk_msg.getVersion();
        storage = peer.storage;
        peer_id = peer.id;
        restore_channel = peer.getRestore_channel();
    }

    @Override
    public void run() {
        File chunk = storage.getFile(file_id, chunk_no);

        if (chunk == null)
            return; // Chunk is not stored

        int read_bytes = 0;
        byte[] body = new byte[Storage.MAX_CHUNK_SIZE], message_bytes;
        ChunkMessage message = new ChunkMessage(version, peer_id, file_id, chunk_no);

        // Create Message
        try {
            // Read chunk
            FileInputStream inputStream = new FileInputStream(chunk.getPath());

            if (inputStream.available() > 0) // Check if empty
                read_bytes = inputStream.read(body); // Read chunk

            // Get message byte array
            message_bytes = message.getBytes(body, read_bytes);

            //Sleep
            restore_channel.received_chunk_msg.set(false);      // TODO: many protocols at same time dont work ???
            Thread.sleep(new Random().nextInt(400));       // Sleep (0-400)ms
            if (restore_channel.received_chunk_msg.get()) return; // Abort if received chunk message

            // Send message
            restore_channel.send(message_bytes);
            System.out.printf("< Peer %d Sent: %s\n", peer_id, message.toString()); // Log
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
