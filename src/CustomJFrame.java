import javax.swing.*;
import java.awt.*;

/**
 * Created by alex on 4/23/16.
 */
public class CustomJFrame extends JFrame {
    private JTextArea chat;
    private JTextArea userList;

    public CustomJFrame() throws HeadlessException {
        super();
    }

    public CustomJFrame(GraphicsConfiguration gc) {
        super(gc);
    }

    public CustomJFrame(String title) throws HeadlessException {
        super(title);
    }

    public CustomJFrame(String title, GraphicsConfiguration gc) {
        super(title, gc);
    }

    public void setChat(JTextArea chat) {
        this.chat = chat;
    }

    public JTextArea getChat() {
        return chat;
    }

    public void setUserList(JTextArea userList) {
        this.userList = userList;
    }

    public JTextArea getUserList() {
        return userList;
    }
}
