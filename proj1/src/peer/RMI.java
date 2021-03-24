package peer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMI extends Remote {
  void backupFile(String file_pathname, int replication_degree) throws RemoteException;
  void restoreFile(String file_pathname) throws RemoteException;
  void deleteFile(String file_pathname) throws RemoteException;
  void reclaim(int max_space) throws RemoteException;
  void getStateInformation() throws RemoteException;
}
