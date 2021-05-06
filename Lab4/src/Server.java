import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

public class Server {
    private static final Hashtable<String, String> entries = new Hashtable<>();

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        ServerSocket server_socket = new ServerSocket(port);

        while (true) {
            Socket socket = server_socket.accept();

            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String request = reader.readLine();
            String[] fields = request.split(" ");

            switch (fields[0]) {
                case "REGISTER":
                    register(writer, fields[1], fields[2]);
                    break;

                case "LOOKUP":
                    lookup(writer, fields[1]);
                    break;
            }

            reader.close();
            writer.close();
            socket.close();
        }
    }

    private static void register(PrintWriter writer, String name, String address) {
        entries.put(name, address);

        String message = String.format("%d %s %s", entries.size(), name, address);

        writer.println(message);
    }

    private static void lookup(PrintWriter writer, String name) {
        String message = "ERROR";

        if (entries.containsKey(name))
            message = String.format("%d %s %s", entries.size(), name, entries.get(name));

        writer.println(message);
    }
}
