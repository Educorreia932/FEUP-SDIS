package messages;

public class PutChunkMessage extends Message {
    private final int replication_degree;
    private int chunk_no;

    public PutChunkMessage(String version, int sender_id, String file_id, int replication_degree, int chunk_no) {
        super(version, "PUTCHUNK", sender_id, file_id);

        this.replication_degree = replication_degree;
        this.chunk_no = chunk_no;
    }

    public PutChunkMessage(String[] header_fields){
        super(header_fields);
        this.chunk_no = Integer.parseInt(header_fields[Fields.CHUNK_NO.ordinal()]);
        this.replication_degree = Integer.parseInt(header_fields[Fields.REP_DEG.ordinal()]);
    }

    @Override
    public String getHeader() {
        String content = String.format("%d %d", chunk_no, replication_degree);
        return super.getHeader(content);
    }

    @Override
    public String toString(){
        return super.toString() + String.format("%d %d", chunk_no, replication_degree);
    }

    public void setChunkNo(int chunk_no) {
        this.chunk_no = chunk_no;
    }

    public int getReplication_degree() {
        return replication_degree;
    }

    public int getChunk_no() {
        return chunk_no;
    }
}
