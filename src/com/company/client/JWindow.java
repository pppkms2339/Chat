package com.company.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class JWindow extends JFrame {
    private final String WINDOW_TITLE = "Network chat";
    private final int FORM_WIDTH = 601;
    private final int FORM_HEIGHT = 400;
    private final String CLOSE_MESSAGE = "Do you really want to exit?";
    private final String INPUT_TEXT_LABEL = "Write a message";
    private final String BUTTON_SEND_TITLE = "Send";
    private final String MENU_FILE = "File";
    private final String MENU_HELP = "Help";
    private final String MENU_ITEM_EXIT = "Exit";
    private final String MENU_ITEM_ABOUT = "About";
    private final String ABOUT_MESSAGE = "Loshmanov Anton (С), 2017";
    private final String AUTH_BUTTON_TEXT = "Auth";
    private final String LOGIN_TEXT_LABEL = "Write a login";
    private final String PASSWORD_TEXT_LABEL = "Write a password";
    private final String ICON_PATH = "../images/icon.png";

    private JTextArea textArea, userListText;
    private JTextField textField, login;
    private JButton button, authButton;
    private JMenuItem miFileExit;
    private JMenuItem miHelpAbout;
    private boolean isAuthorized = false;
    private JPanel authPanel, panel;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private JPasswordField password;
    private JScrollPane scrollPaneUserList;

    public JWindow() {
        windowInitialize();
        addListeners();

        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            setAuthorized(false);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/authok")) {
                            setAuthorized(true);
                            setTitle(WINDOW_TITLE + " - " + msg.split(" ")[1]);
                            break;
                        }
                        if (msg.equals("/inputnick")) {
                            String s;
                            do {
                                s = (String) JOptionPane.showInputDialog(null, "There are no such login and password in the database." + System.lineSeparator() + "Please enter your nickname.", "Auth", JOptionPane.PLAIN_MESSAGE, null, null, null);
                            } while (s.length() < 2);
                            out.writeUTF("/nick" + " " + login.getText() + " " + new String(password.getPassword()) + " " + s.substring(0, 2));
                        }
                    }
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/userlist")) {
                            userListText.setText("");
                            String[] elements = msg.split(" ");
                            for (int i = 1; i < elements.length; i++) {
                                userListText.append(elements[i] + System.lineSeparator());
                            }
                        } else {
                            textArea.append(msg + System.lineSeparator());
                            textArea.setCaretPosition(textArea.getDocument().getLength());
                        }
                    }
                } catch (IOException e) {
                    //e.printStackTrace();
                    setAuthorized(false);
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        setVisible(true);
        textField.requestFocus();
    }

    private void setAuthorized(boolean authorized) {
        isAuthorized = authorized;
        authPanel.setVisible(!isAuthorized);
        panel.setVisible(isAuthorized);
        scrollPaneUserList.setVisible(isAuthorized);
    }

    private void windowInitialize() {
        setTitle(WINDOW_TITLE);
        setIconImage(Toolkit.getDefaultToolkit().createImage(getClass().getResource(ICON_PATH)));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - FORM_WIDTH) / 2, (screenSize.height - FORM_HEIGHT) / 2, FORM_WIDTH, FORM_HEIGHT);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textField = new JTextField();
        textField.setToolTipText(INPUT_TEXT_LABEL);
        button = new JButton(BUTTON_SEND_TITLE);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(textField, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
        add(panel, BorderLayout.SOUTH);
        panel.setVisible(false);

        login = new JTextField();
        login.setToolTipText(LOGIN_TEXT_LABEL);
        password = new JPasswordField();
        password.setToolTipText(PASSWORD_TEXT_LABEL);
        authButton = new JButton(AUTH_BUTTON_TEXT);
        authPanel = new JPanel();
        authPanel.setLayout(new GridLayout(1, 3));
        authPanel.add(login);
        authPanel.add(password);
        authPanel.add(authButton);
        add(authPanel, BorderLayout.NORTH);

        userListText = new JTextArea();
        userListText.setEditable(false);
        scrollPaneUserList = new JScrollPane(userListText);
        scrollPaneUserList.setPreferredSize(new Dimension(100, 1));
        add(scrollPaneUserList, BorderLayout.EAST);
        scrollPaneUserList.setVisible(false);

        JMenuBar mainMenu = new JMenuBar();
        JMenu mFile = new JMenu(MENU_FILE);
        JMenu mHelp = new JMenu(MENU_HELP);
        miFileExit = new JMenuItem(MENU_ITEM_EXIT);
        miHelpAbout = new JMenuItem(MENU_ITEM_ABOUT);
        setJMenuBar(mainMenu);
        mainMenu.add(mFile);
        mainMenu.add(mHelp);
        mFile.add(miFileExit);
        mHelp.add(miHelpAbout);
    }

    private void sendMessage() {
        String msg = textField.getText();
        textField.setText("");
        try {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exit() {
        int result = JOptionPane.showConfirmDialog(null, CLOSE_MESSAGE, WINDOW_TITLE, JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            try {
                out.writeUTF("/end");
                out.flush();
                socket.close();
            } catch (IOException e) {
                //e.printStackTrace();
                setAuthorized(false);
            }
            setVisible(false);
            dispose();
        }
    }

    private void addListeners() {
        authButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    out.writeUTF("/auth" + " " + login.getText() + " " + new String(password.getPassword()));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });

        textField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getExtendedKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });

        miFileExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });

        miHelpAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, ABOUT_MESSAGE, WINDOW_TITLE, JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }
}
