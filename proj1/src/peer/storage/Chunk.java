package peer.storage;

import java.util.Arrays;
import java.util.Objects;

public class Chunk {
    private final String file_id;
    private final int chunk_no;
    private byte[] body;

    public Chunk(String file_id, int chunkno, byte[] body){
        this.file_id = file_id;
        this.chunk_no = chunkno;
        this.body = body;
    }

    public int getChunkNo(){
        return chunk_no;
    }

    public String getFileId(){
        return file_id;
    }

    public byte[] getBody(){
        return body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chunk chunk = (Chunk) o;
        return chunk_no == chunk.chunk_no && Objects.equals(file_id, chunk.file_id);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(file_id, chunk_no);
        result = 31 * result + Arrays.hashCode(body);
        return result;
    }
}
