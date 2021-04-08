package subprotocols;

import channels.MC_Channel;
import channels.MDB_Channel;
import messages.PutChunkMessage;
import peer.Peer;

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
        super(control_channel, version, initiator_peer);

        this.file = file;
        this.replication_degree = replication_degree;
        this.number_of_chunks = number_of_chunks;
        this.mdb_channel = mdb_channel;
        this.message = new PutChunkMessage(version, initiator_peer.id, file_id, replication_degree, 0);
    }

    @Override
    public void run() {
        boolean send_new_chunk = true;
        int read_bytes;
        byte[] chunk = new byte[MAX_CHUNK_SIZE], message_bytes = null;

        try (FileInputStream inputStream = new FileInputStream(file.getPath())) {
            for (int chunk_no = 0; chunk_no < number_of_chunks; chunk_no++) {
                message.setChunkNo(chunk_no);

                // Read from file
                if ((read_bytes = inputStream.read(chunk)) == -1)
                    read_bytes = 0; // Reached EOF

                message_bytes = message.getBytes(chunk, read_bytes);

                if(!sendPutChunk(message_bytes, chunk_no)) {
                    System.out.println("Failed to achieve desired replication degree. Giving up...");

                    return; // Give up
                }
            }
        }

        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("BACKUP of file " + file.getPath() + " finished.");
    }

    public boolean sendPutChunk(byte[] message_bytes, int chunk_no) throws InterruptedException {
        // Resend chunk
        short MAX_TRIES = 5;
        int  sleep_time = 1000, perceived_rp;

        for (int tries = 1; tries <= MAX_TRIES; tries++) {
            // Send message to MDB multicast data channel
            mdb_channel.send(message_bytes);
            System.out.printf("< Peer %d sent | %d bytes | PUTCHUNK %d\n", initiator_peer.id, message_bytes.length, chunk_no);
            Thread.sleep(sleep_time);

            // Check perceived replication degree
            perceived_rp = initiator_peer.storage.getPerceivedRP(file.getPath(), chunk_no);

            if (perceived_rp < replication_degree)
                sleep_time *= 2;  // Double sleep time

            else                  // Achieved desired replication degree
                return true;
        }

        return false; // Max tries => give up
    }
}
