import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Arrays;

public class SSLClient {
    public static void main(String[] args) throws IOException {
        InetAddress host = InetAddress.getByName(args[0]);
        int port = Integer.parseInt(args[1]);
        String oper = args[2];

        SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port);

        int cypher_suites_index = 0;
        int num_opnd = 0;

        if (oper.equals("REGISTER")) {
            cypher_suites_index = 5;
            num_opnd = 2;
        }

        else if (oper.equals("LOOKUP")) {
            cypher_suites_index = 4;
            num_opnd = 1;
        }

        else {
            usage();
        }

        String[] opnd = Arrays.copyOfRange(args, 3, 3 + num_opnd);
        String[] cypher_suites = Arrays.copyOfRange(args, cypher_suites_index, args.length);

        sslSocket.setEnabledCipherSuites(cypher_suites);

        OutputStream outputStream = sslSocket.getOutputStream();
        InputStream inputStream = sslSocket.getInputStream();

        if (oper.equals("REGISTER"))
            register(outputStream, inputStream, opnd[0], opnd[1]);

        else
            lookup(outputStream, inputStream, opnd[0]);

        sslSocket.close();
    }

    private static void usage() {
        System.out.println("Usage: java SSLClient <host> <port> <oper> <opnd>* <cypher-suite>*");
        System.exit(1);
    }

    public static void register(OutputStream outputStream, InputStream inputStream, String name, String address) {
        String message = String.format("REGISTER %s %s", name, address);

        try {
            outputStream.write(message.getBytes());
            String response = new String(inputStream.readAllBytes());
            System.out.println("SSLCLIENT: " + message + " : " + response);
        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void lookup(OutputStream outputStream, InputStream inputStream, String name) {
        String message = String.format("LOOKUP %s", name);

        try {
            outputStream.write(message.getBytes());
            String response = new String(inputStream.readAllBytes());
            System.out.println("SSLCLIENT: " + message + " : " + response);
        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
