package sub_protocols;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Backup implements Runnable{
  private File file;
  private int replication_degree;
  private int initiator_peer;
  private final int CHUNKSIZE = 64000;

  public Backup(Integer peer_id, File file, int replication_degree){
    this.initiator_peer = peer_id;
    this.file = file;
    this.replication_degree = replication_degree;
  }

  @Override
  public void run() {
    sendChunks();
  }

  private void sendChunks(){
    try {
      FileInputStream inputStream = new FileInputStream(file.getPath());
      byte[] chunk;
      int read_bytes, chunk_no = 0, bytes_to_read = CHUNKSIZE;
      long file_size = file.length();

      while(file_size > 0){
        // Read from file and create chunk
        if(file_size < CHUNKSIZE){
          bytes_to_read = (int)file_size;
        }
        chunk = new byte[bytes_to_read];
        read_bytes = inputStream.read(chunk, 0, bytes_to_read);
        file_size -= read_bytes;
        chunk_no++;
        // TODO: Send empty chunk

        //TODO: Send chunk
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
