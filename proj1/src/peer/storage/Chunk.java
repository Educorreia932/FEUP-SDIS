package peer.storage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Chunk implements Serializable {
    private String file_id;
    private int chunk_no;
    private int size;
    private int desired_rep_deg;
    private int perceived_rep_deg;
    private List<Integer> peers;

    public Chunk(String file_id, int chunk_no, int size, int desired_rep_deg, int sender_id) {
        this.file_id = file_id;
        this.chunk_no = chunk_no;
        this.size = size;
        this.desired_rep_deg = desired_rep_deg;
        this.perceived_rep_deg = 1;
        this.peers = new ArrayList<>(Arrays.asList(sender_id));
    }

    public Chunk(String file_id, int chunk_no, int desired_rep_deg, int sender_id) {
        this.file_id = file_id;
        this.chunk_no = chunk_no;
        this.desired_rep_deg = desired_rep_deg;
        this.perceived_rep_deg = 1;
        this.peers = new ArrayList<>(Arrays.asList(sender_id));
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

    public synchronized void incrementPerceivedRepDegree(int sender_id) {
        if(!peers.contains(sender_id)){ // Check if sender already sent stored msg
            this.peers.add(sender_id); // Add sender
            this.perceived_rep_deg++;  // Increment rep deg
        }
    }

    public synchronized int getPerceived_rep_deg() {
        return perceived_rep_deg;
    }
}
