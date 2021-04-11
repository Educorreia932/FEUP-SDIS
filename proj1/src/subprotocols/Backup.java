package subprotocols;

import channels.MC_Channel;
import channels.MDB_Channel;
import messages.PutChunkMessage;
import peer.Peer;
import peer.storage.Storage;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Backup extends Subprotocol {
    private final File file;
    private PutChunkMessage message;
    private final int replication_degree;
    private final MDB_Channel mdb_channel;
    private final int number_of_chunks;

    public Backup(Peer initiator_peer, String version, File file, String file_id, int number_of_chunks,
                  int replication_degree, MDB_Channel mdb_channel, MC_Channel control_channel) {
        super(control_channel, version, initiator_peer);

        this.file = file;
        this.replication_degree = replication_degree;
        this.number_of_chunks = number_of_chunks;
        this.mdb_channel = mdb_channel;
        message = new PutChunkMessage(version, initiator_peer.id, file_id, replication_degree, 0);
    }

    @Override
    public void run() {
        int read_bytes;
        byte[] message_bytes;

        Path path = Paths.get(file.getPath());
        int position = 0;

        try {
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
            ByteBuffer buffer = ByteBuffer.allocate(Storage.MAX_CHUNK_SIZE);

            for (int chunk_no = 0; chunk_no < number_of_chunks; chunk_no++) {
                Future<Integer> operation = fileChannel.read(buffer, position); // Read from file

                message.setChunkNo(chunk_no);

                if ((read_bytes = operation.get()) == -1)
                    read_bytes = 0; // Reached EOF

                position += read_bytes;
                message_bytes = message.getBytes(buffer.array(), read_bytes);

                if (!sendPutChunk(message_bytes)) {
                    System.out.println("Failed to achieve desired replication degree. Giving up...");

                    return; // Give up
                }

                buffer.clear();
            }

            System.out.println("BACKUP of " + file.getPath() + " finished.");

            fileChannel.close();
        }

        catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.out.println("ERROR: Failed to read chunks. Aborting backup...");
        }
    }

    public boolean sendPutChunk(byte[] message_bytes) throws InterruptedException {
        short MAX_TRIES = 5;
        int sleep_time = 1000, perceived_rp;

        for (int tries = 1; tries <= MAX_TRIES; tries++) {
            // Send message to MDB multicast data channel
            mdb_channel.send(message_bytes);
            System.out.printf("< Peer %d sent: %s\n", initiator_peer.id, message.toString());
            Thread.sleep(sleep_time);

            // Check perceived replication degree
            perceived_rp = initiator_peer.storage.getPerceivedRP(file.getPath(), message.getChunk_no());

            if (perceived_rp < replication_degree)
                sleep_time *= 2;  // Double sleep time

            else                  // Achieved desired replication degree
                return true;
        }

        return false; // Max tries => give up
    }
}
