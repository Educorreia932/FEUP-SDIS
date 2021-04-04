package peer.storage;

import java.util.Objects;

public class Chunk {
    private int no;
    private byte[] content;
    private String file_id;

    public Chunk(int no, byte[] content, String file_id){
        this.content = content;
        this.file_id = file_id;
        this.no = no;
    }

    public Chunk(int no, String file_id){
        this.file_id = file_id;
        this.no = no;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chunk chunk = (Chunk) o;
        return no == chunk.no && Objects.equals(file_id, chunk.file_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(no, file_id);
    }
}
