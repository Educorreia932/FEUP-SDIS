package subprotocols;

import channels.MC_Channel;
import channels.MDB_Channel;
import messages.PutChunkMessage;
import peer.Peer;
import peer.storage.Chunk;
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
    private final PutChunkMessage message;
    private final int replication_degree;
    private final MDB_Channel mdb_channel;
    private final int number_of_chunks;
    private int chunk_no;

    public Backup(Peer initiator_peer, String version, File file, String file_id, int number_of_chunks,
                  int replication_degree, MDB_Channel mdb_channel, MC_Channel control_channel) {
        super(control_channel, version, initiator_peer);

        chunk_no = 0;
        this.file = file;
        this.replication_degree = replication_degree;
        this.number_of_chunks = number_of_chunks;
        this.mdb_channel = mdb_channel;
        message = new PutChunkMessage(version, initiator_peer.id, file_id, replication_degree, 0);
    }

    public Backup(Peer initiator_peer, String version, File file, Chunk chunk,
                  MDB_Channel mdb_channel, MC_Channel control_channel) {
        super(control_channel, version, initiator_peer);

        chunk_no = chunk.getChunk_no();
        this.file = file;
        this.replication_degree = chunk.getDesired_rep_deg();
        this.mdb_channel = mdb_channel;
        this.number_of_chunks = -1;
        message = new PutChunkMessage(version, initiator_peer.id, chunk.getFile_id(), replication_degree, 0);
    }

    @Override
    public void run() {
        Path path = Paths.get(file.getPath());
        int position = 0;

        try {
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
            ByteBuffer buffer = ByteBuffer.allocate(Storage.MAX_CHUNK_SIZE);

            if(number_of_chunks == -1){ // Backup of chunk, not full file
                if(readAndSendChunk(chunk_no, fileChannel, buffer, position) == -1)
                    return; // Give up
            }
            else{ // Backup of full file. Iterate chunks
                for (; chunk_no < number_of_chunks; chunk_no++) {
                   position = readAndSendChunk(chunk_no, fileChannel, buffer, position);
                   if(position == -1)
                       return; // Give up
                }
            }

            System.out.println("BACKUP of " + file.getPath() + " finished.");

            fileChannel.close();
        }

        catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.out.println("ERROR: Failed to read chunks. Aborting backup...");
        }
    }

    private boolean sendPutChunk(byte[] message_bytes) throws InterruptedException {
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

    private int readAndSendChunk(int chunk_no, AsynchronousFileChannel fileChannel, ByteBuffer buffer, int position) throws InterruptedException, ExecutionException {
        Future<Integer> operation = fileChannel.read(buffer, position); // Read from file
        int read_bytes;

        message.setChunkNo(chunk_no);

        if ((read_bytes = operation.get()) == -1)
            read_bytes = 0; // Reached EOF

        position += read_bytes;
        byte[] message_bytes = message.getBytes(buffer.array(), read_bytes);

        if (!sendPutChunk(message_bytes)) {
            System.out.println("Failed to achieve desired replication degree. Giving up...");

            return -1; // Give up
        }

        buffer.clear();
        return position;
    }
}
