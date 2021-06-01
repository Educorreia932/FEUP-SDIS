package handlers;

import messages.DeleteMessage;
import peer.Peer;

import java.io.File;

public class DeleteMessageHandler extends MessageHandler {
    private final Peer peer;

    public DeleteMessageHandler(DeleteMessage delete_msg, Peer peer) {
        super(delete_msg.getFile_id(), peer.storage);
        this.peer = peer;
    }

    @Override
    public void run() {
        // Deletes all chunks from file
        deleteAllChunksFromFile(file_id);
        peer.saveStorage(); // Update storage
    }

    private void deleteAllChunksFromFile(String file_id) {
        String path = storage.getFilePath(file_id);
        File folder = new File(path);

        if (folder.exists() && folder.isDirectory()) {
            // Remove all chunks from directory
            File[] chunks = folder.listFiles();

            if (chunks != null)
                for (int chunk_no = 0; chunk_no < chunks.length; chunk_no++)
                    if (chunks[chunk_no].delete()) // Delete file
                        // Delete from map
                        storage.removeStoredChunk(file_id, chunk_no);

            folder.delete(); // Delete folder
        }
    }
}
