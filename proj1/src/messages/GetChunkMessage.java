package messages;

public class GetChunkMessage extends Message {
    public GetChunkMessage(String version, String sender_id, String file_id) {
        super(version, "GETCHUNK", sender_id, file_id);
    }
}
