import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) {
        String host = args[0];

        try {
            Registry registry = LocateRegistry.getRegistry(host);
            RemoteInterface stub = (RemoteInterface) registry.lookup(args[1]);

            String oper = args[2];
            String response;

            switch (oper) {
                case "register":
                    response = stub.register(args[3], args[4]);

                    System.out.println(args[2] + " " + args[3] + " " + args[4] + " :: " + response);

                    break;
                case "lookup":
                    response = stub.lookup(args[3]);

                    System.out.println(args[2] + " " + args[3] + " :: " + response);

                    break;
            }
        }

        catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
