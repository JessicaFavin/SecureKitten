
import java.net.*;

public class KittenServer {
    public static void main(String[] args) throws Exception {
		InetAddress addr = InetAddress.getByName("127.0.0.1");
        ServerSocket serversocket = new ServerSocket(1234, 50, addr);
        while(true) {
            System.out.println("Waiting for client to connect.");
            new KittenService(serversocket.accept());
            System.out.println("New client connected.");
        }
    }

}
