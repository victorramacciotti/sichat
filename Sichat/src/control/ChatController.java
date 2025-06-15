package control;

import model.*;

import view.Window;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Scanner;

public class ChatController {
    private MessageHandler handler;
    private Window window; // referência da View
    private String nome;

    public ChatController(Window window) {
        this.window = window;
    }

    public boolean conectar(String ip, String nome) {
        this.nome = nome;
        final int PORTA = 50001;
        Socket socket = null;

        try {
            socket = new Client(ip, PORTA).conectar();
        } catch (IOException e) {
            try {
                socket = new Server(PORTA).esperarConexao();
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            }
        }

        try {
            handler = new MessageHandler(socket.getInputStream(), socket.getOutputStream(), nome) {
                @Override
                public void iniciarRecebimento() {
                    new Thread(() -> {
                        try {
                            String linha;
                            boolean recebendoArquivo = false;
                            String nomeArquivo = "";
                            StringBuilder conteudoBase64 = new StringBuilder();

                            while ((linha = getEntrada().readLine()) != null) {
                                if (linha.startsWith("!arquivo:")) {
                                    nomeArquivo = linha.substring(9).trim();
                                    recebendoArquivo = true;
                                    conteudoBase64.setLength(0);
                                    window.appendSystemMessage("Recebendo arquivo: " + nomeArquivo);
                                } else if (linha.equals("!fim")) {
                                    byte[] dados = Base64.getDecoder().decode(conteudoBase64.toString());
                                    Files.write(new File("recebido_" + nomeArquivo).toPath(), dados);
                                    window.appendSystemMessage("Arquivo salvo como: recebido_" + nomeArquivo);
                                    recebendoArquivo = false;
                                } else if (recebendoArquivo) {
                                    conteudoBase64.append(linha);
                                } else {
                                    int idx = linha.indexOf(": ");
                                    if (idx > 0) {
                                        String remetente = linha.substring(0, idx);
                                        String conteudo = linha.substring(idx + 2);
                                        window.appendPeerMessage(remetente, conteudo);
                                    }
                                }
                            }
                        } catch (IOException e) {
                            window.appendSystemMessage("Conexão encerrada.");
                        }
                    }).start();
                }
            };

            handler.iniciarRecebimento();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void enviarMensagem(String mensagem) {
        if (handler != null) {
            handler.getSaida().println(nome + ": " + mensagem);
        }
    }

    public void enviarArquivo(File file) {
        if (handler != null && file.exists()) {
            try {
                handler.getSaida().println("!arquivo:" + file.getName());
                byte[] dados = java.nio.file.Files.readAllBytes(file.toPath());
                String base64 = java.util.Base64.getEncoder().encodeToString(dados);
                handler.getSaida().println(base64);
                handler.getSaida().println("!fim");
                window.appendSystemMessage("Arquivo enviado: " + file.getName());
            } catch (IOException e) {
                window.appendSystemMessage("Erro ao enviar arquivo.");
            }
        }
    }
}
