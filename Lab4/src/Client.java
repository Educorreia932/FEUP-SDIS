import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        InetAddress host = InetAddress.getByName(args[0]);
        int port = Integer.parseInt(args[1]);
        String oper = args[2];

        Socket socket = new Socket(host, port);
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        switch (oper) {
            case "REGISTER":
                register(writer, reader, args[3], args[4]);
                break;

            case "LOOKUP":
                lookup(writer, reader, args[3]);
                break;
        }
    }

    private static void register(PrintWriter writer, BufferedReader reader, String name, String address) {
        String message = String.format("REGISTER %s %s\n", name, address);

        try {
            writer.println(message);
            String response = reader.readLine();
            System.out.printf("REGISTER %s :: %s %s\n", name, response, address);
        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void lookup(PrintWriter writer, BufferedReader reader, String name) {
        String message = String.format("LOOKUP %s\n", name);

        try {
            writer.println(message);
            String response = reader.readLine();
            System.out.printf("LOOKUP %s :: %s\n", name, response);
        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
