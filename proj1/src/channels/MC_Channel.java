package channels;

import messages.Fields;
import messages.Message;
import peer.Peer;

import java.util.concurrent.Semaphore;


public class MC_Channel extends Channel {
    public int stored_msgs_received;
    public final Semaphore sem;

    public MC_Channel(String host, int port, Peer peer) {

        super(host, port, peer);
        stored_msgs_received = 0;
        sem = new Semaphore(1);
    }

    @Override
    protected void parseMessage(byte[] msg, int msg_len) {
        byte[] header = Message.getHeaderBytes(msg);

        String header_string = new String(header);
        String[] header_fields = header_string.split(" "); // Split header by spaces

        // Parse fields
        String type = header_fields[Fields.MSG_TYPE.ordinal()];
        int sender_id = Integer.parseInt(header_fields[Fields.SENDER_ID.ordinal()]);

        // Ignore message from itself
        if (sender_id == peer.id) return;

        switch (type){
            case "STORED":
                try {
                    System.out.printf("> Peer %d | STORED \n", peer.id);
                    // Increment shared resource
                    sem.acquire();
                    stored_msgs_received++;
                    sem.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;

            case "GETCHUNK":
                System.out.printf("> Peer %d | GETCHUNK \n", peer.id);
                peer.getChunk(header_fields);

                break;
        }
    }
}
