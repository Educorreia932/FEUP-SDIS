package MessageHandlers;

import messages.DeleteMessage;
import peer.Peer;
import peer.storage.Chunk;
import peer.storage.Storage;

import java.io.File;

public class DeleteMessageHandler implements Runnable{
    private final String file_id;
    private final Peer peer;
    private final Storage storage;

    public DeleteMessageHandler(DeleteMessage delete_msg, Peer peer){
        file_id = delete_msg.getFile_id();
        this.peer = peer;
        storage = peer.storage;
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

            if (chunks != null) {
                for (int chunk_no = 0; chunk_no < chunks.length; chunk_no++) {
                    if (chunks[chunk_no].delete()) { // Delete file
                        // Delete from map
                        storage.removeStoredChunk(file_id, chunk_no);
                    }
                }
            }
            folder.delete(); // Delete folder
        }
    }
}
