package model;

import java.io.*;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Scanner;

public class MessageHandler {
    private final BufferedReader entrada;
    private final PrintWriter saida;
    private final String nome;

    public MessageHandler(InputStream input, OutputStream output, String nome) {
        this.entrada = new BufferedReader(new InputStreamReader(input));
        this.saida = new PrintWriter(output, true);
        this.nome = nome;
    }

    public void iniciarRecebimento() {
        new Thread(() -> {
            try {
                String linha;
                boolean recebendoArquivo = false;
                String nomeArquivo = "";
                StringBuilder conteudoBase64 = new StringBuilder();

                while ((linha = entrada.readLine()) != null) {
                    if (linha.startsWith("!arquivo:")) {
                        nomeArquivo = linha.substring(9).trim();
                        recebendoArquivo = true;
                        conteudoBase64.setLength(0);
                        System.out.println("Recebendo arquivo: " + nomeArquivo);
                    } else if (linha.equals("!fim")) {
                        byte[] dados = Base64.getDecoder().decode(conteudoBase64.toString());
                        Files.write(new File("recebido_" + nomeArquivo).toPath(), dados);
                        System.out.println("Arquivo salvo como: recebido_" + nomeArquivo);
                        recebendoArquivo = false;
                    } else if (recebendoArquivo) {
                        conteudoBase64.append(linha);
                    } else {
                        System.out.println(linha);
                    }
                }
            } catch (IOException e) {
                System.out.println("Conexão encerrada.");
            }
        }).start();
    }

    public void enviar(Scanner scanner) throws IOException {
        while (true) {
            String msg = scanner.nextLine();

            if (msg.startsWith("!arquivo ")) {
                String caminho = msg.substring(9).trim();
                File arquivo = new File(caminho);
                if (arquivo.exists()) {
                    saida.println("!arquivo:" + arquivo.getName());
                    byte[] dados = Files.readAllBytes(arquivo.toPath());
                    String base64 = Base64.getEncoder().encodeToString(dados);
                    saida.println(base64);
                    saida.println("!fim");
                    System.out.println("Arquivo enviado.");
                } else {
                    System.out.println("Arquivo não encontrado.");
                }
            } else {
                saida.println(nome + ": " + msg);
            }
        }
    }
    
    public PrintWriter getSaida() {
        return saida;
    }
    
    public BufferedReader getEntrada() {
        return entrada;
    }
}
