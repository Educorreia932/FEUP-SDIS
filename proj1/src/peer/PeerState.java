package peer;

import peer.storage.Chunk;
import peer.storage.BackedUpFile;
import utils.Pair;

import java.util.HashMap;
import java.util.Set;

public class PeerState {
    private Set<Chunk> backed_up_chunks;
    private HashMap<String, BackedUpFile> backed_up_files;
    private HashMap<Pair<String, Integer>, Integer> stored_chunks;

    public PeerState(HashMap<String, BackedUpFile> backed_up_files, HashMap<Pair<String, Integer>, Integer> stored_chunks,
                     Set<Chunk> backed_up_chunks) {
        this.backed_up_files = backed_up_files;
        this.stored_chunks = stored_chunks;
        this.backed_up_chunks = backed_up_chunks;
    }

    private String getBackedUpFilesInfo() {
        StringBuilder result = new StringBuilder("----------------- \n BACKED UP FILES\n----------------- \n\n");
        for (String path : backed_up_files.keySet()) {
            BackedUpFile file = backed_up_files.get(path);
            result.append("PATH: ").append(path).append('\n').append("ID: ").append(file.getId()).append('\n').append("Desired Replication Degree: ").append(file.getDesired_replication_degree()).append('\n');

            for (int chunk_no = 0; chunk_no < file.getNumberOfChunks(); chunk_no++) {
                int rep_deg = stored_chunks.get(Pair.create(path, chunk_no)); //TODO: Check exists
                result.append("CHUNK: ").append(chunk_no).append(" - Perceived RP: ").append(rep_deg).append("\n");
            }
            result.append('\n');
        }
        return result.toString();
    }

    private String getBackedUpChunksInfo() {
        StringBuilder result = new StringBuilder("------------------ \n BACKED UP CHUNKS\n------------------ \n\n");
        for (Chunk chunk : backed_up_chunks)
            result
                .append("ID: ")
                .append(chunk.getFile_id())
                .append('/')
                .append(chunk.getChunk_no())
                .append("\nSIZE: ")
                .append(chunk.getSize())
                .append("\nDESIRED REPLICATION DEGREE: ")
                .append(chunk.getDesired_rep_deg())
                .append("\nPERCEIVED REPLICATION DEGREE: ")
                .append("TODO")
                .append('\n'); // TODO: Add: desired | Use formatting
        return result.toString();
    }

    @Override
    public String toString() {
        return getBackedUpFilesInfo() + getBackedUpChunksInfo();
    }
}
