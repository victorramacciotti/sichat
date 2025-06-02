package model;

public class File {
	
	public synchronized void sendFile(File file) throws IOException {
        if (dataOutputStream != null && connected && file.exists() && file.isFile()) {
            long fileSize = file.length();
            String fileName = file.getName();
            System.out.println("Enviando arquivo: " + fileName + " (" + fileSize + " bytes)");

            // 1. Send file info
            dataOutputStream.writeByte(TYPE_FILE_INFO);
            dataOutputStream.writeUTF(fileName);
            dataOutputStream.writeLong(fileSize);
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
                    // System.out.println("Sent " + totalSent + " / " + fileSize);
                }
                dataOutputStream.flush(); // Flush after sending all data
                System.out.println("Arquivo " + fileName + " enviado com sucesso.");
                // Update GUI on success (using SwingUtilities if called from non-EDT)
                SwingUtilities.invokeLater(() -> chatGUI.appendMessage("Você: Arquivo '" + fileName + "' enviado."));
            } catch (IOException e) {
                 System.err.println("Erro ao ler ou enviar arquivo " + fileName + ": " + e.getMessage());
                 // Notify GUI of error
                 SwingUtilities.invokeLater(() -> chatGUI.appendMessage("Erro ao enviar arquivo " + fileName + ": " + e.getMessage()));
                 throw e; // Re-throw to be caught by the caller in ChatGUI
            }
        } else if (!connected) {
            throw new IOException("Não conectado.");
        } else if (!file.exists()){
             throw new FileNotFoundException("Arquivo não encontrado: " + file.getAbsolutePath());
        } else {
             throw new IOException("Stream de saída não inicializado ou item selecionado não é um arquivo.");
        }
    }

    // Method to receive and save a file
    private void receiveFile(String fileName, long fileSize) {
        // Sanitize filename potentially? For now, use as is but save in downloadDir.
        File outputFile = new File(downloadDir, fileName);

        // Handle potential file name collisions (e.g., append _1, _2, etc.)
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
        final String finalFileName = outputFile.getName(); // Use the potentially modified name

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
                // System.out.println("Received " + totalReceived + " / " + fileSize);
            }
            fos.flush(); // Ensure all buffered data is written

            if (totalReceived == fileSize) {
                System.out.println("Arquivo " + finalFileName + " recebido com sucesso em " + downloadDir.getName());
                chatGUI.appendMessage("Arquivo '" + finalFileName + "' recebido com sucesso em '" + downloadDir.getName() + "'.");
            } else {
                System.err.println("Erro: Recebimento incompleto do arquivo " + finalFileName + ". Recebido " + totalReceived + " de " + fileSize + " bytes.");
                chatGUI.appendMessage("Erro: Recebimento incompleto do arquivo '" + finalFileName + "'.");
                // Optionally delete the incomplete file
                outputFile.delete();
            }

        } catch (IOException e) {
            System.err.println("Erro ao salvar arquivo " + finalFileName + ": " + e.getMessage());
            chatGUI.appendMessage("Erro ao salvar arquivo '" + finalFileName + "': " + e.getMessage());
            // Optionally delete the potentially corrupt file
            if (outputFile.exists()) {
                outputFile.delete();
            }
        }
    }
	
	private void sendFile() {
        if (networkManager != null && networkManager.isConnected()) {
            JFileChooser fileChooser = new JFileChooser();
            // Set current directory for convenience
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            int result = fileChooser.showOpenDialog(this); // Show file chooser dialog

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                appendMessage("Você: Enviando arquivo: " + selectedFile.getName() + "...");
                // Run in a new thread to avoid blocking the GUI during file reading/sending
                new Thread(() -> {
                    try {
                        networkManager.sendFile(selectedFile);
                        // GUI update for success is handled within NetworkManager or its callbacks now
                    } catch (IOException ex) {
                        // Use SwingUtilities to update GUI from background thread
                        SwingUtilities.invokeLater(() -> {
                            appendMessage("Erro ao enviar arquivo " + selectedFile.getName() + ": " + ex.getMessage());
                            updateStatus("Erro de envio de arquivo", true);
                            JOptionPane.showMessageDialog(this, "Erro ao enviar arquivo: " + ex.getMessage(), "Erro de Envio", JOptionPane.ERROR_MESSAGE);
                        });
                    }
                }).start();
            }
        } else {
             JOptionPane.showMessageDialog(this, "Você precisa estar conectado para enviar arquivos.", "Não Conectado", JOptionPane.WARNING_MESSAGE);
        }
    }

}
