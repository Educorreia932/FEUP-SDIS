package messages;

public class GetChunkMessage extends Message {
    private int chunk_no;

    public GetChunkMessage(String version, int sender_id, String file_id, int chunk_no) {
        super(version, "GETCHUNK", sender_id, file_id);
        this.chunk_no = chunk_no;
    }

    @Override
    public String toString() {
        String content = String.format("%d", chunk_no);

        return super.getHeader(content);
    }

    public void setChunkNo(int chunk_no) {
        this.chunk_no = chunk_no;
    }
}
