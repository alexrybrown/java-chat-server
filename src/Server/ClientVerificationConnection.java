/**
 * This thread will service single clients
 * and check their usernames before sending them into the chat room
 *
 * @Author Alex Brown
 * @Author Andy Makous
 */

package Server;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class ClientVerificationConnection implements Runnable {
    private Socket client;
    private ConcurrentHashMap<String, Client> clientDetails;
    private String welcomeMessage;

    public ClientVerificationConnection(Socket client, ConcurrentHashMap<String, Client> clientDetails, String welcomeMessage) {
        this.client = client;
        this.clientDetails = clientDetails;
        this.welcomeMessage = welcomeMessage;
    }    

    public void run() {
        BufferedReader fromClient = null;
        OutputStream toClient = null;
        String message = null;
        String response = null;
        String username = null;
        String[] usernames = null;
        Client[] clients = null;
        Object[] temp = null;
        String listOfUsers = "";
        boolean denied = false;
        Client clientObject = null;
        try {
            fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
            toClient = new BufferedOutputStream(client.getOutputStream());
            // First message will be the client username
            message = fromClient.readLine();
            username = message.substring(message.indexOf(' ') + 1);
            temp = clientDetails.keySet().toArray();
            usernames = Arrays.copyOf(temp, temp.length, String[].class);
            // Check and see if the username is already in use
            for (String existing : usernames) {
                if(existing.toLowerCase().equals(username.toLowerCase())) {
                    // If a username is already found set the denied boolean
                    denied = true;
                    break;
                }
                listOfUsers += existing + ",";
            }
            listOfUsers+=username;
            // If the username is more than 16 chars deny the user
            if (username.length() == 0 || username.length() > 16) {
                denied = true;
            }
            // Deny user and close connection without saying anymore
            if(denied) {
                response = "2\r\n";
                toClient.write(response.getBytes());
                toClient.flush();
                // Close the client since we denied their request.
                client.close();
            // Welcome user send a user connection to clients and create user object and add that to client details for the client manager
            } else {
                response = "1 " + listOfUsers + " " + welcomeMessage + "\r\n";
                toClient.write(response.getBytes());
                toClient.flush();
                clientObject = new Client(client, username);
                temp = clientDetails.values().toArray();
                clients = Arrays.copyOf(temp, temp.length, Client[].class);
                // Add to client details after getting the array of clients to send connect message to
                clientDetails.put(username, clientObject);
                // Write a connection message to all other clients
                for (Client client : clients) {
                    toClient = new BufferedOutputStream(client.getSocket().getOutputStream());
                    response = "10 " + username + "\r\n";
                    toClient.write(response.getBytes());
                    toClient.flush();
                }
            }
        }
        catch (IOException ioe) { System.err.println(ioe.getMessage()); }
    }
}

