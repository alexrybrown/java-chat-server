/**
 * This thread will handle constructing verfied clients
 * and handle removing the clients from the server
 *
 * @Author Alex Brown and Andy Makous
 */

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
        catch (InterruptedException ie) { }
    }

    /**
     * Parses the message sent it and then send it to the send function to be sent to the
     * relevant clients
     *
     * @param client Client that wrote the message
     * @param message Full message sent by the client to the server
     */
    private void parseAndSend(Client client, String message) {
        int commandCode = -1;
        String parsedMessage = null;
        String broadcastMessage = null;
        String fromUser = null;
        String toUser = null;
        int indexFirstSpace = -1;
        int indexSecondSpace = -1;
        int indexThirdSpace = -1;

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));

        // If it is five chars long there won't be a space
        if(message.length() == 5) {
            commandCode = Integer.parseInt(Character.toString(message.charAt(0)));
        } else { // All others will have a space
            commandCode = Integer.parseInt(message.substring(0, message.indexOf(' ')));
        }
        // Client sent a general message
        if (commandCode == 3) {
            // Parse relevant information
            parsedMessage = message.substring(message.indexOf(' ') + 1, message.indexOf("\r\n"));
            // Setup message, grab clients, and send
            broadcastMessage = "5 " + client.getUsername() + " " + fmt.format(timestamp) + " " + parsedMessage + "\r\n";
            Object[] temp = clientDetails.values().toArray();
            Client[] clients = Arrays.copyOf(temp, temp.length, Client[].class);
            send(clients, broadcastMessage);
        } else if (commandCode == 4) { // Client sent a private message
            // Get the different spaces
            indexFirstSpace = message.indexOf(' ');
            indexSecondSpace = message.indexOf(' ', indexFirstSpace + 1);
            indexThirdSpace = message.indexOf(' ', indexSecondSpace + 1);
            // Parse relevant information
            fromUser = message.substring(indexFirstSpace + 1, indexSecondSpace);
            toUser = message.substring(indexSecondSpace + 1, indexThirdSpace);
            parsedMessage = message.substring(indexThirdSpace + 1, message.indexOf("\r\n"));
            // Setup message, grab clients, and send
            broadcastMessage = "6 " + fromUser + " " + toUser + " " + fmt.format(timestamp) + " " + parsedMessage + "\r\n";
            Client[] clients = {clientDetails.get(fromUser), clientDetails.get(toUser)};
            send(clients, broadcastMessage);
        } else if (commandCode == 7) {
            broadcastMessage = "9 " + client.getUsername() + "\r\n";
            Object[] temp = clientDetails.values().toArray();
            Client[] clients = Arrays.copyOf(temp, temp.length, Client[].class);
            send(clients, broadcastMessage);
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
        catch (IOException ioe) { }
    }
}
