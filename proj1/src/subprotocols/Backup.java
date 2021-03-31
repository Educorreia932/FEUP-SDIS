package subprotocols;

import channels.MDB_Channel;
import messages.PutChunkMessage;
import peer.storage.Chunk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class Backup implements Runnable {
    private final File file;
    private final String file_id;
    private final String version;
    private final int replication_degree;
    private final int initiator_peer;
    private final MDB_Channel channel;

    public Backup(int peer_id, String version, File file, String file_id, int replication_degree, MDB_Channel channel) {
        this.initiator_peer = peer_id;
        this.version = version;
        this.file = file;
        this.file_id = file_id;
        this.replication_degree = replication_degree;
        this.channel = channel;
    }

    @Override
    public void run(){
        int chunk_no = 0;
        byte[] chunk = new byte[Chunk.MAX_CHUNK_SIZE];
        PutChunkMessage message = new PutChunkMessage(version, initiator_peer, file_id, replication_degree, chunk_no);

        try {
            FileInputStream inputStream = new FileInputStream(file.getPath());
            int read_bytes;

            // Read chunk from file
            while ((read_bytes = inputStream.read(chunk)) != -1) {
                message.setChunkNo(chunk_no);
                byte[] message_bytes = message.getBytes(chunk, read_bytes);

                System.out.printf("> Peer %d | %d bytes | Chunk number %d\n", initiator_peer, message_bytes.length, chunk_no);

                // Send message to MDB multicast data channel
                Thread.sleep(100);
                channel.send(message_bytes);
                chunk_no++; // Increment chunk number
            }
        }

        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
