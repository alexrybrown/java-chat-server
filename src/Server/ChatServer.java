/**
 * A chat server listening on port 1337. 
 * This chat server services clients in a chat room
 *
 * This services each request in a separate thread.
 *
 * @Author Alex Brown
 * @Author Andy Makous
 */

package Server;

import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class ChatServer
{
    public static final int DEFAULT_PORT = 1337;
    
	private static final Executor exec = Executors.newCachedThreadPool();
    // Queue for the server to pass pending clients to the client manager
    private static ConcurrentLinkedQueue<Socket> pendingClients = new ConcurrentLinkedQueue<Socket>();
    // Semaphore to signal the client manager that is has work to do
    private static Semaphore signalClientManager = new Semaphore(0);
	
    public static void main(String[] args) throws IOException {
        ServerSocket socket = null;
        Socket pendingClient = null;
		
		try { 
            // Make client manager thread and pass the port number, socket, pending clients, executor, exec mutex lock, and semaphore to signal client manager
            socket = new ServerSocket(DEFAULT_PORT);
            Runnable clientManagerThread = new ClientManagerConnection(pendingClients, exec, signalClientManager);
            exec.execute(clientManagerThread);
			// establish the client socket
			while (true) {
                // Accept the client, let the client manager know it has work to do, pass to client manager to check the username
                pendingClient = socket.accept();
                pendingClients.add(pendingClient);
                signalClientManager.release();
			}
		}
        catch (IOException ioe) { System.err.println(ioe.getMessage()); }
        finally {
            if (socket != null)
				socket.close();
		}
	}
}
