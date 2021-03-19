package messages;

public class StoredMessage extends Message {
    int chunk_no;

    public StoredMessage(String version, String sender_id, String file_id, int chunk_no) {
        super(version, "STORED", sender_id, file_id);

        this.chunk_no = chunk_no;
    }

    @Override
    public String toString() {
        String content = String.format("%d", chunk_no);

        return super.getHeader(content);
    }
}
