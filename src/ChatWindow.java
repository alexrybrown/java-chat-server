/**
 * Creates the GU Interface AKA GUI (but GU Interface is much better).
 *
 * @Author Alex Brown
 * @Author Andy Makous
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatWindow {
    // Window design
    private JTabbedPane tabbedPane1;
    private JPanel container;
    private JTextArea UsernameGuidelines;
    private JTextField UsernameInput;
    private JButton connectButton;
    private JTextArea ChatText;
    private JTextArea UserList;
    private JTextField Message;
    private JButton Send;
    // Communication
    private static Socket socket = null;
    // Executor
    public static final Executor exec = Executors.newCachedThreadPool();


    public ChatWindow() {
// Window closing

        // Send username by clicking on button
        connectButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                ChatController.connect(UsernameInput.getText());
            }
        });
        // Send username with enter
        UsernameInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                // Enter is pressed
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    ChatController.connect(UsernameInput.getText());
            }
        });
        // Send chat message with enter
        Message.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                // Enter is pressed
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    ChatController.processMessageField(Message.getText());

            }
        });
        // Send chat message by clicking on button
        Send.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                ChatController.processMessageField(Message.getText());
            }
        });

    }

    public static void main(String[] args) {
        CustomJFrame window = new CustomJFrame("ChatWindow");
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ChatController.disconnect();
                e.getWindow().dispose();
            }
        });
        ChatWindow cw = new ChatWindow();
        window.setContentPane(cw.container);
        window.setChat(cw.ChatText);
        window.setUserList(cw.UserList);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);

        try {
            socket = new Socket("localhost", 1337);
        } catch (IOException ioe) { System.err.println(ioe.getMessage()); }

        Runnable chatControllerThread = new ChatController(socket, window);
        chatControllerThread.run();

    }

}
