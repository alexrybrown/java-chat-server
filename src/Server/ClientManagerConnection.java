/**
 * This thread will handle constructing verfied clients
 * and handle removing the clients from the server
 *
 * @Author Alex Brown
 * @Author Andy Makous
 */

package Server;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;

public class ClientManagerConnection implements Runnable {
    // Welcome message for the server
    private static final String WELCOME_MESSAGE = "Welcome to the server!?";
    // Index correlates to the code for each string
    private static final String[] COMMAND_CODES = {
        "Server.Client requests a username",
        "Server accepts username request",
        "Server denies username request",
        "Server.Client sends general message to server",
        "Server.Client sends private message to server",
        "Server sends general message to user",
        "Server sends private message to user",
        "Server.Client sends a disconnect request",
        "Server says goodbye",
        "Server tells client that user has disconnected",
        "Server tels client that user has connected"
    };
    // Keep track of all the clients
    private static ConcurrentHashMap<String, Client> clientDetails = new ConcurrentHashMap<String, Client>();
    // To let the client manager know that a client wants to disconnect
    private static ConcurrentLinkedQueue<Client> clientDisconnects = new ConcurrentLinkedQueue<Client>();

    // Inherited from server, refer back to server for use cases in comments
    private ConcurrentLinkedQueue<Socket> pendingClients;
    private Executor exec;
    private Semaphore signalClientManager;

    public ClientManagerConnection(ConcurrentLinkedQueue<Socket> pendingClients, Executor exec, Semaphore signalClientManager) {
        this.pendingClients = pendingClients;
        this.exec = exec;
        this.signalClientManager = signalClientManager;
    }

    public void run() {
        Client client = null;

        try {
            // Make room thread for global chat room
            Runnable roomThread = new RoomConnection(clientDetails, clientDisconnects, exec, signalClientManager);
            exec.execute(roomThread);
            
            while (true) {
                // Try to see if there is work to be done, otherwise go dormant until signaled (Server and Rooms will be producing work for this thread)
                signalClientManager.acquire();

                // Try and remove clients that have disconnected from the server
                while(!clientDisconnects.isEmpty()) {
                    // Remove clients from the hash map and disconnect them from server
                    client = clientDisconnects.poll();
                    clientDetails.remove(client.getUsername());
                    client.getSocket().close();
                }
                
                // Try and accept clients into the server.
                while(!pendingClients.isEmpty()) {
                    // Get clients and create verification threads for them to create a username and be connected to the global chat
                    Runnable clientVerificationThread = new ClientVerificationConnection(pendingClients.poll(), clientDetails, WELCOME_MESSAGE);
                    exec.execute(clientVerificationThread);
                }
                
                // If there are multiple permits, reduce it down to 1 to keep the permits in check and keep useful functionality
                if (signalClientManager.availablePermits() > 1) {
                    // Leave 1 so we know there is still work to be done and it won't wait on the next time through the while loop.
                    signalClientManager.acquire(signalClientManager.availablePermits() - 1);
                }
            }
        }
        catch (IOException ioe) { System.err.println(ioe.getMessage()); }
        catch (InterruptedException ie) { System.err.println(ie.getMessage()); }
    }
}
