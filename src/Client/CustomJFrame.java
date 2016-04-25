/**
 * Needed extra functionality of out the JFrame to access the text box windows and text entry boxes.
 *
 * @Author Alex Brown
 * @Author Andy Makoue
 */

package Client;

import java.awt.*;
import javax.swing.*;

public class CustomJFrame extends JFrame {
    private JTextArea chat;
    private JTextArea userList;
    private JTextArea usernameGuidelines;
    private JTabbedPane tabbedPane;

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

    public void setUsernameGuidelines(JTextArea usernameGuidelines) {
        this.usernameGuidelines = usernameGuidelines;
    }

    public JTextArea getUsernameGuidelines() {
        return usernameGuidelines;
    }

    public void setTabbedPane(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }
}
