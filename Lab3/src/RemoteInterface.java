import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {
    String register(String name, String address) throws RemoteException;
    String lookup(String name) throws RemoteException;
}
