package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import control.ChatController;
import java.awt.*;
import java.io.File;

@SuppressWarnings("serial")
public class Window extends JFrame {

    private String userName;

    private JPanel loginPanel;
    private JTextField ipField;
    private JTextField nameField;
    private JButton connectButton;

    private JPanel chatPanel;
    private JTextPane messagePane;
    private StyledDocument doc;
    private JTextField messageField;
    private JButton sendButton;
    private JButton sendFileButton;

    private JFileChooser fileChooser;
    private ChatController controller;

    public Window() {
        super("P2P Chat");
        controller = new ChatController(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);

        fileChooser = new JFileChooser();

        setupLoginPanel();
        add(loginPanel);
        setVisible(true);
    }

    private void setupLoginPanel() {
        loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        loginPanel.add(new JLabel("IP do Peer:"), gbc);
        ipField = new JTextField(20);
        loginPanel.add(ipField, gbc);

        loginPanel.add(new JLabel("Seu Nome:"), gbc);
        nameField = new JTextField(20);
        loginPanel.add(nameField, gbc);

        connectButton = new JButton("Conectar");
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(connectButton, gbc);

        connectButton.addActionListener(e -> {
            String ip = ipField.getText();
            String name = nameField.getText();

            if (controller.conectar(ip, name)) {
                userName = name;
                setTitle("P2P Chat - " + userName);
                switchToChatPanel();
            } else {
                JOptionPane.showMessageDialog(this, "Não foi possível conectar.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void setupChatPanel() {
        chatPanel = new JPanel(new BorderLayout(10, 10));
        chatPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        messagePane = new JTextPane();
        messagePane.setEditable(false);
        messagePane.setBackground(Color.WHITE);
        doc = messagePane.getStyledDocument();
        JScrollPane scrollPane = new JScrollPane(messagePane);
        messagePane.setBorder(new EmptyBorder(10, 10, 10, 10));
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout(10, 0));

        messageField = new JTextField();
        southPanel.add(messageField, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        sendButton = new JButton("Enviar");
        buttonsPanel.add(sendButton);

        sendFileButton = new JButton("Anexar");
        buttonsPanel.add(sendFileButton);

        southPanel.add(buttonsPanel, BorderLayout.EAST);
        chatPanel.add(southPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> {
            String messageText = messageField.getText();
            if (!messageText.trim().isEmpty()) {
                controller.enviarMensagem(messageText);
                appendMyMessage(messageText);
                messageField.setText("");
            }
        });

        messageField.addActionListener(e -> sendButton.doClick());

        sendFileButton.addActionListener(e -> {
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                controller.enviarArquivo(selectedFile);
                appendSystemMessage("Você enviou o arquivo: " + selectedFile.getName());
            }
        });
    }

    private void switchToChatPanel() {
        remove(loginPanel);
        setupChatPanel();
        add(chatPanel);
        revalidate();
        repaint();
    }

    public void appendMyMessage(String message) {
        appendStyledMessage("Você: ", message, Color.RED);
    }

    public void appendPeerMessage(String senderName, String message) {
        appendStyledMessage(senderName + ": ", message, Color.BLUE);
    }

    public void appendSystemMessage(String message) {
        appendStyledMessage("[SISTEMA]: ", message, Color.GRAY);
    }

    private void appendStyledMessage(String prefix, String message, Color color) {
        Style style = messagePane.addStyle("CustomStyle", null);
        StyleConstants.setForeground(style, color);
        try {
            doc.insertString(doc.getLength(), prefix + message + "\n", style);
            messagePane.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    
}
