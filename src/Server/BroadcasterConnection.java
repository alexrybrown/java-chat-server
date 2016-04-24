/**
 * This thread will handle constructing verfied clients
 * and handle removing the clients from the server
 *
 * @Author Alex Brown
 * @Author Andy Makous
 */

package Server;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

public class BroadcasterConnection implements Runnable {
    private ConcurrentHashMap<String, Client> clientDetails;
    private ConcurrentLinkedQueue<Client> broadcastToClients;
    private Semaphore signalBroadcaster;

    public BroadcasterConnection(ConcurrentHashMap<String, Client> clientDetails, ConcurrentLinkedQueue<Client> broadcastToClients, Semaphore signalBroadcaster) {
        this.clientDetails = clientDetails;
        this.broadcastToClients = broadcastToClients;
        this.signalBroadcaster = signalBroadcaster;
    }

    public void run() {
        Client client = null;
        String message = null;

        try {
            // Try to see if there is work to be done, otherwise go dormant until signaled (The room will be producing work for this thread)
            while(true) {
                signalBroadcaster.acquire();

                while (!broadcastToClients.isEmpty()) {
                    client = broadcastToClients.poll();
                    while (!client.getFromClient().isEmpty()) {
                        parseAndSend(client, client.getFromClient().poll());
                    }
                }

                // If there are multiple permits, reduce it down to 1 to keep the permits in check and keep useful functionality
                if (signalBroadcaster.availablePermits() > 1) {
                    // Leave 1 so we know there is still work to be done and it won't wait on the next time through the while loop.
                    signalBroadcaster.acquire(signalBroadcaster.availablePermits() - 1);
                }
            }
        }
        catch (InterruptedException ie) { System.err.println(ie.getMessage()); }
    }

    /**
     * Parses the message sent it and then send it to the send function to be sent to the
     * relevant clients
     *
     * @param client Server.Client that wrote the message
     * @param message Full message sent by the client to the server
     */
    private void parseAndSend(Client client, String message) {
        int commandCode = -1;
        String parsedMessage = null;
        String broadcastMessage = null;
        String fromUser = null;
        String toUser = null;
        Object[] temp = null;
        Client[] clients = null;

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));

        // If it is a char there won't be a space
        if (message.length() == 1) {
            commandCode = Integer.parseInt(Character.toString(message.charAt(0)));
        } else if (message.length() != 0) { // All others will have a space
            commandCode = Integer.parseInt(message.substring(0, message.indexOf(' ')));
        }
        switch (commandCode) {
            // Server.Client sent a general message
            case (3):
                // Parse relevant information
                parsedMessage = message.substring(message.indexOf(' ') + 1);
                // Setup message, grab clients, and send
                broadcastMessage = "5 " + client.getUsername() + " " + fmt.format(timestamp) + " " + parsedMessage + "\r\n";
                temp = clientDetails.values().toArray();
                clients = Arrays.copyOf(temp, temp.length, Client[].class);
                send(clients, broadcastMessage);
                break;
            // Server.Client sent a private message
            case (4):
                // Get the different spaces
                String[] split = message.split("\\p{javaSpaceChar}", 4);
                // Parse relevant information
                fromUser = split[1];
                toUser = split[2];
                parsedMessage = split[3];
                // Setup message, grab clients, and send
                broadcastMessage = "6 " + fromUser + " " + toUser + " " + fmt.format(timestamp) + " " + parsedMessage + "\r\n";
                Client[] twoClients = {clientDetails.get(fromUser), clientDetails.get(toUser)};
                send(twoClients, broadcastMessage);
                break;
            case (7):
                broadcastMessage = "9 " + client.getUsername() + "\r\n";
                temp = clientDetails.values().toArray();
                ArrayList<Client> clientsArray = new ArrayList<Client>();
                for (Object obj : temp) {
                    Client tempClient = (Client) obj;
                    // Don't add the client that is disconnecting
                    if (!(tempClient.getUsername().equals(client.getUsername()))) {
                        clientsArray.add(tempClient);
                    }
                }
                clients = Arrays.copyOf(clientsArray.toArray(), clientsArray.toArray().length, Client[].class);
                send(clients, broadcastMessage);
                break;
        }
    }

    /**
     * Send the given message to all of the given clients
     *
     * @param clients Array of clients to send the message to
     * @param broadcastMessage Parsed message to be sent to the given clients
     */
    private void send(Client[] clients, String broadcastMessage) {
        OutputStream toClient = null;

        try {
            for (Client client : clients) {
                toClient = new BufferedOutputStream(client.getSocket().getOutputStream());
                toClient.write(broadcastMessage.getBytes());
                toClient.flush();
            }
        }
        catch (IOException ioe) { System.err.println(ioe.getMessage()); }
    }
}
