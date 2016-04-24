/**
 * Server.Client class to store all relevant information for threads dealing with the client
 *
 * @Author Alex Brown
 * @Author Andy Makous
 */

package Server;

import java.net.*;
import java.util.concurrent.*;

public class Client {
    private Socket socket;
    private String username;
    private ConcurrentLinkedQueue<String> fromClient;

    public Client(Socket socket, String username) {
        this.socket = socket;
        this.username = username;
        this.fromClient = new ConcurrentLinkedQueue<String>();
    }

    public Socket getSocket() {
        return socket;
    }

    public String getUsername() {
        return username;
    }

    public ConcurrentLinkedQueue<String> getFromClient() {
        return fromClient;
    }

    public void addMessage(String message) {
        fromClient.add(message);
    }
}

