package control;

import model.*;
import view.Window;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class ChatController {
    private MessageHandler handler;
    private final Window window;
    private String nome;

    public ChatController(Window window) {
        this.window = window;
    }

    public boolean conectar(String ip, String nome) {
        this.nome = nome;
        final int PORTA = 50001;
        Socket socket;

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
                protected void onMensagemTexto(String linha) {
                    int idx = linha.indexOf(": ");
                    if (idx > 0) {
                        String remetente = linha.substring(0, idx);
                        String conteudo = linha.substring(idx + 2);
                        window.appendPeerMessage(remetente, conteudo);
                    }
                }

                @Override
                protected void onArquivoInicio(String nomeArquivo) {
                    window.appendSystemMessage("Recebendo arquivo: " + nomeArquivo);
                }

                @Override
                protected void onArquivoRecebido(File arquivoSalvo) {
                    window.appendSystemMessage("Arquivo salvo em: " + arquivoSalvo.getAbsolutePath());
                }

                @Override
                protected void onConexaoEncerrada() {
                    window.appendSystemMessage("Conexão encerrada.");
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
            handler.enviarMensagem(mensagem);
        }
    }

    public void enviarArquivo(File file) {
        if (handler != null) {
            try {
                handler.enviarArquivo(file);
                window.appendSystemMessage("Você enviou o arquivo: " + file.getName());
            } catch (IOException e) {
                window.appendSystemMessage("Erro ao enviar o arquivo.");
            }
        }
    }
}
