package Peer.Storage;

public class Chunk {
    private final String file_id;
    private final int chunkno;
    private byte[] body;

    public Chunk(String file_id, int chunkno, byte[] body){
        this.file_id = file_id;
        this.chunkno = chunkno;
        this.body = body;
    }

    public int getChunkNo(){
        return chunkno;
    }

    public String getFileId(){
        return file_id;
    }

    public byte[] getBody(){
        return body;
    }
}
