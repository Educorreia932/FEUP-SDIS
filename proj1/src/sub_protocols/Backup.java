package sub_protocols;

import java.io.File;

public class Backup {
  private File file;
  private int replication_degree;
  private int initiator_peer;

  public Backup(Integer peer_id, File file, int replication_degree){
    this.initiator_peer = peer_id;
    this.file = file;
    this.replication_degree = replication_degree;
  }



}
