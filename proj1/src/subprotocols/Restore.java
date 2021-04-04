package subprotocols;

import channels.MC_Channel;
import channels.MDR_Channel;
import messages.GetChunkMessage;
import peer.storage.Chunk;

import java.nio.charset.StandardCharsets;

public class Restore implements Runnable{
    private MC_Channel mc_channel;
    private MDR_Channel mdr_channel;
    private String version;
    private int initiator_peer;
    private int number_of_chunks;
    private String file_id;
    private GetChunkMessage message;

    public Restore(int initiator_peer, String version, String file_id, int number_of_chunks,
                   MDR_Channel mdr_channel, MC_Channel mc_channel){
        this.mc_channel = mc_channel;
        this.mdr_channel = mdr_channel;
        this.version = version;
        this.initiator_peer = initiator_peer;
        this.number_of_chunks = number_of_chunks;
        this.message = new GetChunkMessage(version, initiator_peer, file_id, 0);
    }

    @Override
    public void run() { //TODO: make sure all chunks are received
        for(int chunk_no = 0; chunk_no < number_of_chunks;){
            byte[] message_bytes = message.getBytes(null, 0);

            mc_channel.send(message_bytes);
            System.out.printf("< Peer %d | %d bytes | GETCHUNK %d\n", initiator_peer, message_bytes.length, chunk_no);

            try {
                Thread.sleep(500);
                mdr_channel.sem.acquire();
                if(mdr_channel.received_chunks.contains(new Chunk(chunk_no, file_id))){ // TODO: Not working
                    chunk_no++;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // TODO: Restore file
    }
}
