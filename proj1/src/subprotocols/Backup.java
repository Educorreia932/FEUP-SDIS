package subprotocols;

import channels.MC_Channel;
import channels.MDB_Channel;
import messages.PutChunkMessage;
import peer.storage.Chunk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

public class Backup implements Runnable {
    private final File file;
    private final String file_id;
    private final String version;
    private final int replication_degree;
    private final int initiator_peer;
    private final MDB_Channel mdb_channel;
    private final MC_Channel mc_channel;
    private final short MAX_TRIES = 5;
    private Random random;

    public Backup(int peer_id, String version, File file, String file_id, int replication_degree,
                  MDB_Channel mdb_channel, MC_Channel mc_channel) {
        this.initiator_peer = peer_id;
        this.version = version;
        this.file = file;
        this.file_id = file_id;
        this.replication_degree = replication_degree;
        this.mdb_channel = mdb_channel;
        this.mc_channel = mc_channel;
        random = new Random();
    }

    @Override
    public void run(){
        boolean send_new_chunk = true;
        int chunk_no = 0, read_bytes, tries = 1, received = 0, sleep_time;
        byte[] chunk = new byte[Chunk.MAX_CHUNK_SIZE], message_bytes = null;
        PutChunkMessage message = new PutChunkMessage(version, initiator_peer, file_id, replication_degree, chunk_no);

        try {
            FileInputStream inputStream = new FileInputStream(file.getPath());

            while (true){
                message.setChunkNo(chunk_no);

                if(send_new_chunk){ // Send new chunk => read from file
                    // Read from file
                    if((read_bytes = inputStream.read(chunk)) == -1)
                        break; // Reached EOF
                    message_bytes = message.getBytes(chunk, read_bytes);
                }
                // Send message to MDB multicast data channel
                mdb_channel.send(message_bytes);
                System.out.printf("< Peer %d | %d bytes | PUTCHUNK %d\n", initiator_peer, message_bytes.length, chunk_no);

                sleep_time = random.nextInt(400);
                Thread.sleep(sleep_time);

                // Access shared resource
                mc_channel.sem.acquire(); // acquire sem to access shared resource
                received = mc_channel.stored_msgs_received; // store value
                mc_channel.stored_msgs_received = 0; // Reset count
                mc_channel.sem.release(); // release sem

                if(received < replication_degree){
                    send_new_chunk = false; // Resend chunk
                    if(tries >= MAX_TRIES) break; // Max tries => give up
                    tries++; // Increment number of tries
                }
                else{
                    tries = 1; // Reset tries
                    chunk_no++; // Update chunk
                    send_new_chunk = true; // Send new chunk
                }
            }
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
