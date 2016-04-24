/** * Contains the logic for the client GU Interface. * Processes client and server communications. * * @Author Alex Brown * @Author Andy Makous */import javax.swing.*;import java.io.*;import java.net.Socket;import java.util.ArrayList;import java.util.Arrays;import java.util.concurrent.ConcurrentLinkedQueue;public class ChatController implements Runnable {    private static JTextArea chat;    private static JTextArea userListPane;    private static String username = "";    // Server Communication    private static Socket socket;    private static OutputStream toServer = null;    private static BufferedReader fromServer = null;    private static ConcurrentLinkedQueue<String> userInput = new ConcurrentLinkedQueue<String>();    private static ArrayList<String> userList = new ArrayList<String>();    private static Boolean disconnect = false, updateUserList = false;    public ChatController(Socket socket, CustomJFrame window) {        try {            fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));            toServer = new BufferedOutputStream(socket.getOutputStream());        } catch (IOException ioe) { System.err.println(ioe.getMessage()); }        chat = window.getChat();        userListPane = window.getUserList();    }    /**     * Contains the loops to send user input to the server,     * read lines sent by the server,     * and update the user list.     */    public void run() {        while (!disconnect) {            try {                while (!userInput.isEmpty()) {                    toServer.write(userInput.poll().getBytes());                    toServer.flush();                }                while (fromServer.ready()) {                    parseFromServerLine(fromServer.readLine());                }                if (updateUserList){                    String newUserList = "";                    for (String user : userList)                        newUserList += user + '\n';                    userListPane.setText(newUserList);                    updateUserList = false;                }            } catch (IOException ioe) { System.err.println(ioe.getMessage()); }        }    }    /**     * Disconnects client and sets disconnect boolean to false to end the while loop in run().     * Doesn't need to close socket because the server handles that.     */    public static void disconnect() {        if (!username.equals("")) {            userInput.add("7\r\n");        }        disconnect = true;    }    /**     * Splits the line sent by the server and performs actions based upon its command code.     * @param fromServerLine     */    public void parseFromServerLine(String fromServerLine){        String[] split = fromServerLine.split("\\p{javaSpaceChar}", 5);        switch (split[0]){            // Server accepts username and sends user list            case ("1"):                createUserList(split[1]);                break;            // General message            case ("5"):                chat.append("[" + split[2] + "] " + split[1] + ": " +                        fromServerLine.substring(fromServerLine.indexOf(split[2])+split[2].length()+1) + "\r\n");                break;            // Private message            case ("6"):                // Server bounces back all messages                if (split[1].equals(username)) {                    chat.append("[" + split[3] + "] Whispered " + split[2] + ": " +                            fromServerLine.substring(fromServerLine.indexOf(split[3]) + split[3].length() + 1) + "\r\n");                } else {                    chat.append("[" + split[3] + "] " + split[1] + " whispers: " +                            fromServerLine.substring(fromServerLine.indexOf(split[3]) + split[3].length() + 1) + "\r\n");                }                break;            // User disconnect            case ("9"):                userList.remove(split[1]);                chat.append(split[1] + " has disconnected.\r\n");                updateUserList = true;                break;            // User connect            case ("10"):                userList.add(split[1]);                chat.append(split[1] + " has connected.\r\n");                updateUserList = true;                break;        }    }    /**     * Initially populates the user list.     * @param serverUserList     */    private static void createUserList(String serverUserList){        if (serverUserList.indexOf(',') == -1) {            userList = new ArrayList<String>();            userList.add(serverUserList);        } else {            userList = new ArrayList<String>(Arrays.asList(serverUserList.split(",")));        }        updateUserList = true;    }    /**     * Sends the user to the server and sets data field.     * Assumes username will be accepted, but if not, the data field will be updated in a later attempt.     *     * @param user     */    public static void connect(String user) {        username = user;        userInput.add("0 " + username + "\r\n");    }    /**     * Processes the input from the text field in the chat tab.     *     * @param input from "message"     * @return message with proper command code prepended     */    public static void processMessageField(String input) {        // Slash commands!        if (input.startsWith("/")) {            // Split into: [slash command] [username] [message]            String[] split = input.split("\\p{javaSpaceChar}", 3);            // Switch statement to process slash commands            switch (split[0].toLowerCase()) {                case "/whisper":                case "/w":                    if (userList.contains(split[1]))                        userInput.add("4 " + username + " " + split[1] + " " + split[2] + "\r\n");                    break;                case "/help":                case "/h":                case "/?":                    chat.append("/w [username] [message]\r\n" +                            "/whisper [username] [message]\r\n");            }        }        else {            userInput.add("3 " + input + "\r\n");        }    }}