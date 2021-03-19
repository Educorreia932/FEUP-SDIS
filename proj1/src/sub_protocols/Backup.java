package sub_protocols;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Backup {
  private File file;
  private int replication_degree;
  private int initiator_peer;
  private final int CHUNKSIZE = 64000;

  public Backup(Integer peer_id, File file, int replication_degree){
    this.initiator_peer = peer_id;
    this.file = file;
    this.replication_degree = replication_degree;

    sendChunks();
  }

  private void sendChunks(){
    try {
      FileInputStream inputStream = new FileInputStream(file.getPath());
      byte[] chunk = new byte[CHUNKSIZE];
      int read_bytes;
      int chunk_no = 0;
      while((read_bytes = inputStream.read(chunk, 0, CHUNKSIZE)) != -1){
        chunk_no++;
        System.out.println(chunk_no);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
