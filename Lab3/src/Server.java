import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;

public class Server implements RemoteInterface {
    private Hashtable<String, String> entries = new Hashtable<>();

    public Server() {

    }

    public static void main(String[] args) {
        Server server = new Server();

        try {
            RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(server, 8080);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(args[0], stub);
        }

        catch (RemoteException | AlreadyBoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String register(String name, String address) throws RemoteException {
        entries.put(name, address);

        String ret = name + " " + entries.get(name);

        System.out.println("REGISTER " + name + " " + address + " :: " + ret);

        return ret;
    }

    @Override
    public String lookup(String name) throws RemoteException {
        String ret = name + " " + entries.get(name);

        System.out.println("LOOKUP " + name + " :: " + ret);

        return ret;
    }
}
