package subprotocols;

import channels.MC_Channel;
import messages.DeleteMessage;

public class Delete implements Runnable{
    private String version;
    private int initiator_peer;
    private MC_Channel mc_channel;
    private DeleteMessage message;
    private final int MAX_TRIES = 3;

    public Delete(String version, int peer_id, String file_id, MC_Channel mc_channel){
        this.version = version;
        this.initiator_peer = peer_id;
        this.mc_channel = mc_channel;
        this.message = new DeleteMessage(version, peer_id, file_id);
    }


    @Override
    public void run() {
        for(int i = 0; i < MAX_TRIES; i++) {
            byte[] message_bytes = message.getBytes(null, 0);
            mc_channel.send(message_bytes);
            System.out.printf("< Peer %d | %d bytes | DELETE \n", initiator_peer, message_bytes.length);

            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
