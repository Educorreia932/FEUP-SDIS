import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Hashtable;

public class SSLServer {
    private static final Hashtable<String, String> entries = new Hashtable<>();

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        String[] cypher_suites = Arrays.copyOfRange(args, 1, args.length);

        SSLServerSocket sslServerSocket = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(port);
        sslServerSocket.setEnabledCipherSuites(cypher_suites);

        System.out.println("Server is now running");

        while (true) {
            SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
            sslSocket.setEnabledCipherSuites(cypher_suites);

            BufferedReader reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(sslSocket.getOutputStream(), true);

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
            sslSocket.close();
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
