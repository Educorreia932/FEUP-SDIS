package subprotocols;

import channels.MC_Channel;
import channels.MDR_Channel;
import messages.GetChunkMessage;

import java.nio.charset.StandardCharsets;

public class Restore implements Runnable{
    private MC_Channel mc_channel;
    private MDR_Channel mdr_channel;
    private String version;
    private int initiator_peer;
    private String file_id;

    public Restore(int initiator_peer, String version, String file_id, MDR_Channel mdr_channel,
                   MC_Channel mc_channel){
        this.mc_channel = mc_channel;
        this.mdr_channel = mdr_channel;
        this.version = version;
        this.initiator_peer = initiator_peer;
        this.file_id = file_id;
    }


    @Override
    public void run() {
        int chunk_no = 0;
        // TODO: while (???)
        GetChunkMessage message = new GetChunkMessage(version, initiator_peer, file_id, chunk_no);
        byte[] message_bytes = message.toString().getBytes(StandardCharsets.UTF_8);

        mc_channel.send(message_bytes);
        System.out.printf("< Peer %d | %d bytes | GETCHUNK %d\n", initiator_peer, message_bytes.length, chunk_no);
    }
}
