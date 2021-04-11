package subprotocols;

import channels.MC_Channel;
import channels.MDR_Channel;
import messages.GetChunkMessage;
import peer.Peer;
import peer.storage.Storage;
import utils.Pair;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Restore extends Subprotocol {
    private final MDR_Channel restore_channel;
    private final int number_of_chunks;
    private final String file_id;
    private final String file_path;
    private final GetChunkMessage message;

    public Restore(Peer initiator_peer, String version, String file_path, String file_id, int number_of_chunks,
                   MDR_Channel restore_channel, MC_Channel control_channel) {
        super(control_channel, version, initiator_peer);

        this.restore_channel = restore_channel;
        this.number_of_chunks = number_of_chunks;
        this.file_id = file_id;
        this.file_path = file_path;
        this.message = new GetChunkMessage(version, initiator_peer.id, file_id, 0);
    }

    @Override
    public void run() {
        int chunk_no = 0;

        while (chunk_no < number_of_chunks) {
            // Send message
            control_channel.send(message.getBytes(null, 0));
            System.out.printf("< Peer %d sent: %s\n", initiator_peer.id, message);

            try {
                Thread.sleep(500);

                // Check if received chunk
                boolean has_chunk = restore_channel.received_chunks.containsKey(new Pair<>(file_id, chunk_no));

                if (has_chunk) // Chunk received => Skip to next chunk
                    message.setChunkNo(++chunk_no);
            }

            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (restoreFileChunks())
            System.out.println("RESTORE of " + file_path + " finished.");

        else
            System.out.println("Failed to restore files of " + file_path);
    }

    /**
     * Write restored file chunks
     */
    private boolean restoreFileChunks() {
        ArrayList<byte[]> chunks = new ArrayList<>(); // Array of chunks by order

        for (int chunk_no = 0; chunk_no < number_of_chunks; chunk_no++) // Add chunks to array
            chunks.add(restore_channel.received_chunks.remove(Pair.create(file_id, chunk_no)));

        Path path = Paths.get(file_path);

        try {
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE);

            ByteBuffer buffer = ByteBuffer.allocate(chunks.size() * Storage.MAX_CHUNK_SIZE);

            for (byte[] chunk : chunks)
                buffer.put(chunk);

            buffer.flip();

            Future<Integer> operation = fileChannel.write(buffer, 0);
            buffer.clear();

            operation.get();

            return true;
        }

        catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();

            return false;
        }
    }
}
