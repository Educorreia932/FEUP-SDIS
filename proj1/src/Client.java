import java.rmi.Remote;

public interface Client extends Remote {
  void backupFile(String file_pathname, int replication_degree);
  void restoreFile(String file_pathname);
  void deleteFile(String file_pathname);
  void setStorageSpace(int max_space);
  void getStateInformation();
}
