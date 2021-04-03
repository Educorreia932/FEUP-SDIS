package channels;

import messages.Fields;
import messages.Message;
import peer.Peer;

public class MDR_Channel extends Channel {
    public MDR_Channel(String host, int port, Peer peer) {
        super(host, port, peer);
    }

    @Override
    protected void parseMessage(byte[] msg, int msg_len) {
        byte[] header = Message.getHeaderBytes(msg);

        String header_string = new String(header);
        String[] header_fields = header_string.split(" "); // Split header by spaces

        // Ignore message from itself
        int sender_id = Integer.parseInt(header_fields[Fields.SENDER_ID.ordinal()]);
        if (sender_id == peer.id) return;

        String type = header_fields[Fields.MSG_TYPE.ordinal()];
        if(type.equals("CHUNK")){
            System.out.printf("> Peer %d | CHUNK \n", peer.id);
            // TODO: Write chunk to file
        }
    }
}
