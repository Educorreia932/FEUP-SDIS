package test;

import peer.RMI;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.*;

public class TestApp {
    public static void main(String[] args){
        if (args.length < 2) {
            usage();
            System.exit(1);
        }

        try {
            // TODO: Remove sleep
            Thread.sleep(500);

            Registry registry = LocateRegistry.getRegistry(); // Default port: 1099
            String peer_ap = args[0];
            RMI stub = (RMI) registry.lookup(peer_ap);

            String operation = args[1];

            switch (operation) {
                case "BACKUP":
                    if (args.length < 4) {
                        usage();
                        System.exit(1);
                    }

                    stub.backupFile(args[2], Integer.parseInt(args[3]));

                    break;

                case "RESTORE":
                    if (args.length < 3) {
                        usage();
                        System.exit(1);
                    }

                    stub.restoreFile(args[2]);

                    break;

                case "DELETE":
                    if (args.length < 3) {
                        usage();
                        System.exit(1);
                    }

                    stub.deleteFile(args[2]);

                    break;

                case "RECLAIM":
                    if (args.length < 3) {
                        usage();
                        System.exit(1);
                    }

                    stub.reclaim(Long.parseLong(args[2]));

                    break;

                case "STATE":
                    System.out.println(stub.getStateInformation());
                    break;

                default:
                    System.out.println("Unknown operation.\n");
                    usage();
                    System.exit(1);
            }
        }

        catch (RemoteException | NotBoundException | InterruptedException e) {
            System.err.println("ERROR: Failed to connect to remote interface. \nAborting...");
            e.printStackTrace();
        }
    }

    private static void usage() {
        System.out.println("usage: java test.TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
    }
}

