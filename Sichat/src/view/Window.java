package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
    private JPanel messageListPanel;
    private JScrollPane scrollPane;

    private JPanel chatPanel;
    private JTextField messageField;
    private JButton sendButton;
    private JButton sendFileButton;

    private JFileChooser fileChooser;
    private ChatController controller;
    
    private static final Color COR_FUNDO = new Color(0, 28, 41);
    private static final Color COR_BOLHA_ENVIADA = new Color(105, 128, 140);
    private static final Color COR_BOLHA_RECEBIDA = new Color(158, 168, 174);
    private static final Color COR_TEXTO = new Color(211, 217, 220);
    private static final Color COR_BUTTON = new Color(105, 128, 140);
    private static final Color COR_BUTTON_HOVER = new Color(25, 64, 84);

    public Window() {
        super("[SI]Chat");
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
        loginPanel.setBackground(COR_FUNDO);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel ipLabel = new JLabel("IP do Peer:");
        ipLabel.setForeground(COR_TEXTO);
        loginPanel.add(ipLabel, gbc);
        ipField = new JTextField(20);
        ipField.setBorder(null);
        ipField.setBackground(COR_TEXTO);
        loginPanel.add(ipField, gbc);
        
        JLabel nameLabel = new JLabel("Seu Nome:");
        nameLabel.setForeground(COR_TEXTO);
        loginPanel.add(nameLabel, gbc);
        nameField = new JTextField(20);
        nameField.setBorder(null);
        nameField.setBackground(COR_TEXTO);
        loginPanel.add(nameField, gbc);

        connectButton = new JButton("Conectar");
        connectButton.setBackground(COR_BUTTON);
        connectButton.setForeground(COR_TEXTO);
        aplicarHover(connectButton, COR_BUTTON, COR_BUTTON_HOVER);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(connectButton, gbc);

        connectButton.addActionListener(e -> {
            String ip = ipField.getText();
            String name = nameField.getText();

            if (controller.conectar(ip, name)) {
                userName = name;
                setTitle("[SI]Chat - " + userName);
                switchToChatPanel();
            } else {
                JOptionPane.showMessageDialog(this, "Não foi possível conectar.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void setupChatPanel() {
        chatPanel = new JPanel(new BorderLayout(10, 10));
        chatPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        messageListPanel = new JPanel();
        messageListPanel.setLayout(new BoxLayout(messageListPanel, BoxLayout.Y_AXIS));
        messageListPanel.setBackground(COR_TEXTO);
        messageListPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); 

        scrollPane = new JScrollPane(messageListPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(COR_TEXTO);
        scrollPane.getVerticalScrollBar().setBackground(COR_BUTTON);
        scrollPane.setBackground(COR_TEXTO);
        scrollPane.setBorder(null);

        chatPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout(10, 0));

        messageField = new JTextField();
        messageField.setBorder(null);
        southPanel.add(messageField, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonsPanel.setBackground(COR_FUNDO);
        
        ImageIcon sendBtnIcon = new ImageIcon(getClass().getResource("/icons/send_icon.png"));
		Image sendBtnIconResizer = sendBtnIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
		ImageIcon sendBtnIconSmooth = new ImageIcon(sendBtnIconResizer);

        sendButton = new JButton(sendBtnIconSmooth);
        buttonsPanel.add(sendButton);
        aplicarHover(sendButton, COR_BUTTON, COR_BUTTON_HOVER);
        
        ImageIcon fileBtnIcon = new ImageIcon(getClass().getResource("/icons/attach_file.png"));
		Image fileBtnIconResizer = fileBtnIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
		ImageIcon fileBtnIconSmooth = new ImageIcon(fileBtnIconResizer);
		

        sendFileButton = new JButton(fileBtnIconSmooth);
        buttonsPanel.add(sendFileButton);
        aplicarHover(sendFileButton, COR_BUTTON, COR_BUTTON_HOVER);

        southPanel.add(buttonsPanel, BorderLayout.EAST);
        southPanel.setBackground(COR_FUNDO);
        chatPanel.add(southPanel, BorderLayout.SOUTH);
        chatPanel.setBackground(COR_FUNDO);

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
                appendMyMessage("Você enviou o arquivo: " + selectedFile.getName());
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
        addChatBubble(message, true);
    }

    public void appendPeerMessage(String senderName, String message) {
        addChatBubble(senderName + ": " + message, false);
    }

    public void appendSystemMessage(String message) {
        addChatBubble("[SISTEMA]: " + message, false);
    }


    private void addChatBubble(String message, boolean isMine) {
        JPanel bubble = new JPanel(new BorderLayout());
        bubble.setBackground(isMine ? COR_BOLHA_ENVIADA : COR_BOLHA_RECEBIDA);
        bubble.setBorder(new EmptyBorder(6, 10, 6, 10));

        JTextArea label = new JTextArea(message);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setForeground(COR_TEXTO);
        label.setOpaque(false);
        label.setWrapStyleWord(true);
        label.setLineWrap(true);
        label.setEditable(false);
        label.setFocusable(false);
        label.setBorder(null);
        label.setSize(new Dimension(200, Short.MAX_VALUE));
        label.setPreferredSize(null);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        bubble.add(label, BorderLayout.CENTER);
        bubble.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));

        JPanel wrapper = new JPanel(new FlowLayout(isMine ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        wrapper.setBackground(COR_TEXTO);
        wrapper.add(bubble);
        wrapper.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        messageListPanel.add(wrapper);
        messageListPanel.revalidate();
        messageListPanel.repaint();

        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum()));
    }
    
    private void aplicarHover(JButton botao, Color corPadrao, Color corHover) {
        botao.setBackground(corPadrao);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.setFocusPainted(false);
        botao.setBorderPainted(false);
        botao.setForeground(COR_TEXTO);

        botao.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                botao.setBackground(corHover);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                botao.setBackground(corPadrao);
            }
        });
    }

    
}
