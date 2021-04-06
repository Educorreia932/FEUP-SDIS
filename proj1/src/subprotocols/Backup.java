package subprotocols;

import channels.MC_Channel;
import channels.MDB_Channel;
import messages.PutChunkMessage;
import peer.Peer;
import utils.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Backup extends Subprotocol {
    private final File file;
    private final PutChunkMessage message;
    private final int replication_degree;
    private final MDB_Channel mdb_channel;
    private final int number_of_chunks;

    private static final int MAX_CHUNK_SIZE = 64000;

    public Backup(Peer initiator_peer, String version, File file, String file_id, int number_of_chunks,
                  int replication_degree, MDB_Channel mdb_channel, MC_Channel control_channel) {
        super(control_channel, version, initiator_peer, file_id);

        this.file = file;
        this.replication_degree = replication_degree;
        this.number_of_chunks = number_of_chunks;
        this.mdb_channel = mdb_channel;
        this.message = new PutChunkMessage(version, initiator_peer.id, file_id, replication_degree, 0);
    }

    @Override
    public void run() {
        boolean send_new_chunk = true;
        int read_bytes, tries = 1, sleep_time = 1000;
        byte[] chunk = new byte[MAX_CHUNK_SIZE], message_bytes = null;

        try (FileInputStream inputStream = new FileInputStream(file.getPath())) {
            for (int chunk_no = 0; chunk_no < number_of_chunks; ) {
                message.setChunkNo(chunk_no);

                if (send_new_chunk) { // Send new chunk => read from file
                    // Read from file
                    if ((read_bytes = inputStream.read(chunk)) == -1)
                        read_bytes = 0; // Reached EOF
                    message_bytes = message.getBytes(chunk, read_bytes);
                }

                // Send message to MDB multicast data channel
                mdb_channel.send(message_bytes);
                System.out.printf("< Peer %d | %d bytes | PUTCHUNK %d\n", initiator_peer.id, message_bytes.length, chunk_no);
                Thread.sleep(sleep_time);

                // TODO: Verify Rep Deg
                if (1000 < replication_degree) {
                    send_new_chunk = false; // Resend chunk

                    short MAX_TRIES = 5;
                    if (tries >= MAX_TRIES) {
                        System.out.println("Failed to achieve desired replication degree. Giving up...");
                        break; // Max tries => give up
                    }

                    tries++; // Increment number of tries
                    sleep_time *= 2; // Double sleep time
                }

                else {
                    tries = 1; // Reset tries
                    chunk_no++; // Update chunk
                    sleep_time = 1000; // Reset sleep time to 1s
                    send_new_chunk = true; // Send new chunk
                }
            }
        }

        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
