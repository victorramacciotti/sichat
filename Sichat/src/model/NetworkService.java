package model;

import control.ChatController;

import javax.swing.*;
import java.io.*;
import java.net.*;

public class NetworkService {

    private Socket socket;
    private ServerSocket serverSocket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private ChatController controller; // Reference to the controller to update GUI
    private volatile boolean connected = false;
    private Thread receiveThread;
    private File downloadDir; // Directory to save received files

    // Protocol constants (using bytes for efficiency)
    private static final byte TYPE_TEXT_MESSAGE = 0;
    private static final byte TYPE_FILE_INFO = 1;
    // File data follows TYPE_FILE_INFO directly

    public NetworkService(ChatController controller) {
        this.controller = controller;
        // Ensure download directory exists
        downloadDir = new File("chat_downloads");
        if (!downloadDir.exists()) {
            if (!downloadDir.mkdirs()) {
                System.err.println("Falha ao criar diretório de downloads: " + downloadDir.getAbsolutePath());
                // Fallback or notify user
                controller.onNetworkError("Não foi possível criar o diretório de downloads.");
                downloadDir = new File("."); // Fallback to current directory
            }
        }
    }

    public boolean isConnected() {
        return connected;
    }

    // --- Connection Management --- //

    public void startServer(int port) throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            throw new IOException("Servidor já iniciado na porta " + port);
        }
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Servidor iniciado na porta " + port + ", aguardando cliente...");

            // Accept connection in a separate thread to avoid blocking GUI
            new Thread(() -> {
                try {
                    socket = serverSocket.accept(); // Blocks until a connection is made
                    System.out.println("Cliente conectado: " + socket.getInetAddress());
                    setupStreams();
                    connected = true;
                    // Notify controller on EDT
                    SwingUtilities.invokeLater(() -> controller.onConnectionEstablished(socket.getRemoteSocketAddress().toString()));
                    startReceiving(); // Start the receiving thread
                } catch (IOException e) {
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        System.err.println("Erro ao aceitar conexão: " + e.getMessage());
                        SwingUtilities.invokeLater(() -> controller.onNetworkError("Erro ao aceitar conexão: " + e.getMessage()));
                    } else {
                        System.out.println("ServerSocket fechado, aceitação cancelada.");
                        // No error needed if closed intentionally
                    }
                    disconnect("Erro de aceitação");
                }
            }).start();

        } catch (IOException e) {
            System.err.println("Erro ao iniciar servidor: " + e.getMessage());
            disconnect("Erro ao iniciar servidor");
            throw e; // Re-throw exception
        }
    }

    public void connectToServer(String ip, int port) throws IOException {
        if (socket != null && socket.isConnected()) {
            throw new IOException("Já conectado");
        }
        try {
            System.out.println("Conectando a " + ip + ":" + port + "...");
            socket = new Socket(ip, port);
            System.out.println("Conectado ao servidor: " + socket.getRemoteSocketAddress());
            setupStreams();
            connected = true;
            // Notify controller on EDT
            SwingUtilities.invokeLater(() -> controller.onConnectionEstablished(socket.getRemoteSocketAddress().toString()));
            startReceiving(); // Start the receiving thread
        } catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
            disconnect("Falha na conexão");
            throw e; // Re-throw exception
        }
    }

    private void setupStreams() throws IOException {
        // Use buffered streams for efficiency
        dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    // --- Data Receiving --- //

    private void startReceiving() {
        receiveThread = new Thread(this::receiveDataLoop);
        receiveThread.setDaemon(true); // Allow program to exit even if this thread is running
        receiveThread.start();
    }

    private void receiveDataLoop() {
        try {
            while (connected && socket != null && !socket.isClosed() && dataInputStream != null) {
                byte dataType = dataInputStream.readByte(); // Read the type indicator

                switch (dataType) {
                    case TYPE_TEXT_MESSAGE:
                        String message = dataInputStream.readUTF();
                        // Notify controller on EDT
                        SwingUtilities.invokeLater(() -> controller.onMessageReceived(message));
                        break;
                    case TYPE_FILE_INFO:
                        String fileName = dataInputStream.readUTF();
                        long fileSize = dataInputStream.readLong();
                        String sender = dataInputStream.readUTF(); // Read sender username
                        // Notify controller on EDT
                        SwingUtilities.invokeLater(() -> controller.onFileReceiving(fileName, fileSize, sender));
                        // Receive the actual file data (in this thread)
                        receiveFile(fileName, fileSize);
                        break;
                    default:
                        System.err.println("Tipo de dados desconhecido: " + dataType);
                        throw new IOException("Tipo de dados desconhecido recebido: " + dataType);
                }
            }
        } catch (EOFException e) {
            System.out.println("Conexão fechada pelo outro lado (EOF).");
            disconnect("Remoto fechou a conexão");
        } catch (SocketException e) {
             System.out.println("Erro de Socket (possivelmente desconectado): " + e.getMessage());
             disconnect("Erro de Socket: " + e.getMessage());
        } catch (IOException e) {
            if (connected) { // Only show error if we didn't intentionally disconnect
                System.err.println("Erro ao receber dados: " + e.getMessage());
                SwingUtilities.invokeLater(() -> controller.onNetworkError("Erro de recebimento: " + e.getMessage()));
                disconnect("Erro de recebimento");
            }
        } finally {
            // Ensure cleanup happens if the loop exits unexpectedly
            if (connected) {
                 disconnect("Fim inesperado do loop de recebimento");
            }
        }
    }

    // --- Data Sending --- //

    public synchronized void sendTextMessage(String message) throws IOException {
        if (dataOutputStream != null && connected) {
            dataOutputStream.writeByte(TYPE_TEXT_MESSAGE);
            dataOutputStream.writeUTF(message);
            dataOutputStream.flush();
            System.out.println("Mensagem de texto enviada.");
        } else {
            throw new IOException("Não conectado ou stream de saída não inicializado.");
        }
    }

    public synchronized void sendFile(File file, String senderUsername) throws IOException {
        if (dataOutputStream != null && connected && file.exists() && file.isFile()) {
            long fileSize = file.length();
            String fileName = file.getName();
            System.out.println("Enviando arquivo: " + fileName + " (" + fileSize + " bytes) por " + senderUsername);

            // 1. Send file info (type, name, size, sender)
            dataOutputStream.writeByte(TYPE_FILE_INFO);
            dataOutputStream.writeUTF(fileName);
            dataOutputStream.writeLong(fileSize);
            dataOutputStream.writeUTF(senderUsername); // Send username
            // Do not flush yet, send data immediately after

            // 2. Send file data
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192]; // 8KB buffer
                int bytesRead;
                long totalSent = 0;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    dataOutputStream.write(buffer, 0, bytesRead);
                    totalSent += bytesRead;
                    // Optional: Update GUI with progress here if needed
                }
                dataOutputStream.flush(); // Flush after sending all data
                System.out.println("Arquivo " + fileName + " enviado com sucesso.");
                // Success message is handled by controller after this method returns
            } catch (IOException e) {
                 System.err.println("Erro ao ler ou enviar arquivo " + fileName + ": " + e.getMessage());
                 throw e; // Re-throw to be caught by the controller
            }
        } else if (!connected) {
            throw new IOException("Não conectado.");
        } else if (!file.exists()){
             throw new FileNotFoundException("Arquivo não encontrado: " + file.getAbsolutePath());
        } else {
             throw new IOException("Stream de saída não inicializado ou item selecionado não é um arquivo.");
        }
    }

    // --- File Receiving Logic --- //

    private void receiveFile(String fileName, long fileSize) {
        // Create a unique file name in the download directory
        File outputFile = new File(downloadDir, fileName);
        int count = 1;
        String baseName = fileName;
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            baseName = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex); // includes the dot
        }
        while (outputFile.exists()) {
            outputFile = new File(downloadDir, baseName + "_" + count + extension);
            count++;
        }
        final String finalFileName = outputFile.getName();
        final String finalSavePath = outputFile.getAbsolutePath();

        System.out.println("Recebendo dados para: " + finalFileName + " (" + fileSize + " bytes)");

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalReceived = 0;
            long remaining = fileSize;

            while (totalReceived < fileSize && (bytesRead = dataInputStream.read(buffer, 0, (int)Math.min(buffer.length, remaining))) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalReceived += bytesRead;
                remaining -= bytesRead;
                // Optional: Update GUI with progress here
            }
            fos.flush();

            if (totalReceived == fileSize) {
                System.out.println("Arquivo " + finalFileName + " recebido com sucesso em " + downloadDir.getName());
                // Notify controller on EDT
                SwingUtilities.invokeLater(() -> controller.onFileReceived(finalFileName, finalSavePath));
            } else {
                System.err.println("Erro: Recebimento incompleto do arquivo " + finalFileName + ". Recebido " + totalReceived + " de " + fileSize + " bytes.");
                SwingUtilities.invokeLater(() -> controller.onNetworkError("Recebimento incompleto do arquivo " + finalFileName));
                outputFile.delete(); // Delete incomplete file
            }

        } catch (IOException e) {
            System.err.println("Erro ao salvar arquivo " + finalFileName + ": " + e.getMessage());
            SwingUtilities.invokeLater(() -> controller.onNetworkError("Erro ao salvar arquivo " + finalFileName + ": " + e.getMessage()));
            if (outputFile.exists()) {
                outputFile.delete(); // Delete potentially corrupt file
            }
        }
    }

    // --- Disconnection --- //

    public void disconnect(String reason) {
        if (!connected && socket == null && serverSocket == null) {
            return; // Already disconnected or never connected
        }
        boolean wasConnected = connected;
        connected = false;
        System.out.println("Desconectando... Razão: " + reason);

        // Close server socket first
        try { if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close(); } catch (IOException e) { System.err.println("Erro ao fechar ServerSocket: " + e.getMessage()); }

        // Close streams
        try { if (dataInputStream != null) dataInputStream.close(); } catch (IOException e) { /* ignore */ }
        try { if (dataOutputStream != null) dataOutputStream.close(); } catch (IOException e) { /* ignore */ }

        // Close client socket
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException e) { /* ignore */ }

        // Interrupt the receiving thread if it's still running
        if (receiveThread != null && receiveThread.isAlive()) {
            receiveThread.interrupt();
        }

        socket = null;
        serverSocket = null;
        dataInputStream = null;
        dataOutputStream = null;
        receiveThread = null;

        // Update GUI only if it was previously connected
        if (wasConnected) {
             SwingUtilities.invokeLater(() -> controller.onDisconnected(reason));
        }
        System.out.println("Desconexão concluída.");
    }
}

