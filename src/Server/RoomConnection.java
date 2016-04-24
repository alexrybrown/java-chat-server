/**
 * This thread will handle constructing verfied clients
 * and handle removing the clients from the server
 *
 * @Author Alex Brown and Andy Makous
 */

package Server;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class RoomConnection implements Runnable {
    // pass clients to broadcaster that require forming of messages
    private ConcurrentLinkedQueue<Client> broadcastToClients = new ConcurrentLinkedQueue<Client>();
    // Semaphore to signal the broadcaster that is has work to do
    private Semaphore signalBroadcaster = new Semaphore(0);

    private ConcurrentHashMap<String, Client> clientDetails;
    private ConcurrentLinkedQueue<Client> clientDisconnects;
    private Executor exec;
    private Semaphore signalClientManager;

    public RoomConnection(ConcurrentHashMap<String, Client> clientDetails, ConcurrentLinkedQueue<Client> clientDisconnects, Executor exec, Semaphore signalClientManager) {
        this.clientDetails = clientDetails;
        this. clientDisconnects = clientDisconnects;
        this.exec = exec;
        this.signalClientManager = signalClientManager;
    }

    public void run() {
        BufferedReader fromClient = null;
        OutputStream toClient = null;
        Object[] temp = null;
        Client[] clients = null;
        String line = null;
        String response = null;
        int commandCode = -1;
        try {
            // Make broadcaster for this room
            Runnable broadcasterThread = new BroadcasterConnection(clientDetails, broadcastToClients, signalBroadcaster);
            exec.execute(broadcasterThread);

            while(true) {
                // Sleep for a milliseconds
                Thread.sleep(100);
                // Go through client details and see if any clients have posted anything
                temp = clientDetails.values().toArray();
                clients = Arrays.copyOf(temp, temp.length, Client[].class);
                for (Client client : clients) {
                    fromClient = new BufferedReader(new InputStreamReader(client.getSocket().getInputStream()));
                    // Read from the client until the buffer is clear
                    while(fromClient.ready()) {
                        line = fromClient.readLine();
                        // The command code we are looking for will be a char long and be a number less than 10
                        if(line.length() == 1) {
                            commandCode = Integer.parseInt(Character.toString(line.charAt(0)));
                        } else { // All others we will forward to the broadcaster
                            commandCode = -1;
                        }
                        switch(commandCode) {
                            // Handle disconnect request
                            case (7):
                                // Write the disconnect response to client
                                toClient = new BufferedOutputStream(client.getSocket().getOutputStream());
                                response = "8\r\n";
                                toClient.write(response.getBytes());
                                toClient.flush();
                                // Add them to the clients that need to disconnect and then signal the client manager
                                clientDisconnects.add(client);
                                signalClientManager.release();
                                break;
                        }
                        // Pass work on to the broadcaster (in the case of the disconnect, broadcaster still needs to send mass message)
                        client.addMessage(line);
                        // If the client hasn't been added to the queue then add them
                        if(!broadcastToClients.contains(client)) {
                            broadcastToClients.add(client);
                        }
                        // Let the broadcaster know it has work to do.
                        signalBroadcaster.release();
                    }
                }
            }
        }
        catch (InterruptedException ie) { System.err.println(ie.getMessage()); }
        catch (IOException ioe) { System.err.println(ioe.getMessage()); }

    }
}
