package messages;

public class PutChunkMessage extends Message {
    private final int replication_degree;
    private int chunk_no;

    public PutChunkMessage(String version, int sender_id, String file_id, int replication_degree, int chunk_no) {
        super(version, "PUTCHUNK", sender_id, file_id);

        this.replication_degree = replication_degree;
        this.chunk_no = chunk_no;
    }

    @Override
    public String toString() {
        String content = String.format("%d %d", chunk_no, replication_degree);
        // TODO: Include body

        return super.getHeader(content);
    }

    public void setChunkNo(int chunk_no){
        this.chunk_no = chunk_no;
    }
}
