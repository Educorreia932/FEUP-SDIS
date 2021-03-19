package messages;

public class RemovedMessage extends Message{
    private int chunk_no;

    public RemovedMessage(String version, String sender_id, String file_id, int chunk_no) {
        super(version, "REMOVED", sender_id, file_id);
        this.chunk_no = chunk_no;
    }

    @Override
    public String toString() {
        String content = String.format("%d", chunk_no);

        return super.generateHeader(content);
    }
}
