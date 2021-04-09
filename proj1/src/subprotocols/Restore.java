package subprotocols;

import channels.MC_Channel;
import channels.MDR_Channel;
import messages.GetChunkMessage;
import peer.Peer;
import utils.Pair;

public class Restore extends Subprotocol {
    private final MDR_Channel restore_channel;
    private final int number_of_chunks;
    private final String file_id;
    private final String file_path;
    private final GetChunkMessage message;

    public Restore(Peer initiator_peer, String version, String file_path, String file_id, int number_of_chunks, MDR_Channel restore_channel, MC_Channel control_channel) {
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
        byte[] message_bytes = message.getBytes(null, 0);

        while (chunk_no < number_of_chunks) {
            // Send message
            control_channel.send(message_bytes);
            System.out.printf("< Peer %d sent: %s\n", initiator_peer.id, message.toString());

            try {
                Thread.sleep(500);

                // Check if received chunk
                boolean has_chunk = restore_channel.received_chunks.containsKey(new Pair<>(file_id, chunk_no));

                if (has_chunk) { // Chunk received => Skip to next chunk
                    chunk_no++;
                    message.setChunkNo(chunk_no);
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        restore_channel.restoreFileChunks(file_path, file_id, number_of_chunks);
        System.out.println("RESTORE of " + file_path + " finished.");
    }
}
