package peer.storage;

import java.util.Objects;

public class BackedUpChunk {
    private String file_id;
    private int chunk_no;
    private int size;
    private int desired_rep_deg;

    public BackedUpChunk(String file_id, int chunk_no, int size, int desired_rep_deg) {
        this.file_id = file_id;
        this.chunk_no = chunk_no;
        this.size = size;
        this.desired_rep_deg = desired_rep_deg;
    }

    public BackedUpChunk(String file_id, int chunk_no) {
        this.file_id = file_id;
        this.chunk_no = chunk_no;
    }

    public String getFile_id() {
        return file_id;
    }

    public int getSize() {
        return size;
    }

    public int getDesired_rep_deg() {
        return desired_rep_deg;
    }

    public int getChunk_no() {
        return chunk_no;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BackedUpChunk that = (BackedUpChunk) o;
        return chunk_no == that.chunk_no && Objects.equals(file_id, that.file_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file_id, chunk_no);
    }
}
