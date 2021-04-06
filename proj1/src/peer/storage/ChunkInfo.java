package peer.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkInfo {
    private String file_id;
    private int chunk_no;
    private int size;
    private int desired_rep_deg;
    private int perceived_rep_deg;
    private Set<Integer> peers;

    public ChunkInfo(String file_id, int chunk_no, int size, int desired_rep_deg) {
        this.file_id = file_id;
        this.chunk_no = chunk_no;
        this.size = size;
        this.desired_rep_deg = desired_rep_deg;
        this.perceived_rep_deg = 1;
        this.peers = new ConcurrentHashMap<Integer, Integer>().keySet();
    }

    public ChunkInfo(String file_id, int chunk_no, int desired_rep_deg) {
        this.file_id = file_id;
        this.chunk_no = chunk_no;
        this.desired_rep_deg = desired_rep_deg;
        this.perceived_rep_deg = 1;
        this.peers = new ConcurrentHashMap<Integer, Integer>().keySet();
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
        ChunkInfo that = (ChunkInfo) o;
        return chunk_no == that.chunk_no && Objects.equals(file_id, that.file_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file_id, chunk_no);
    }

    public void incrementPerceivedRepDegree(int sender_id) {
        if(!peers.contains(sender_id)){ // Check if sender already sent stored msg
            //this.peers.add(sender_id); // Add sender TODO: crasha
            this.perceived_rep_deg++;  // Increment rep deg
        }
    }
}
