package model;

import util.FileUtils;
import java.io.*;
import java.nio.file.Files;
import java.util.Base64;

public abstract class MessageHandler {
    protected final BufferedReader entrada;
    protected final PrintWriter saida;
    protected final String nome;

    public MessageHandler(InputStream input, OutputStream output, String nome) {
        this.entrada = new BufferedReader(new InputStreamReader(input));
        this.saida = new PrintWriter(output, true);
        this.nome = nome;
    }

    public void iniciarRecebimento() {
        new Thread(this::receberMensagens).start();
    }

    private void receberMensagens() {
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
                    onArquivoInicio(nomeArquivo);
                } else if (linha.equals("!fim")) {
                    byte[] dados = Base64.getDecoder().decode(conteudoBase64.toString());
                    File destino = FileUtils.getDownloadPath(nomeArquivo);
                    Files.write(destino.toPath(), dados);
                    recebendoArquivo = false;
                    onArquivoRecebido(destino);
                } else if (recebendoArquivo) {
                    conteudoBase64.append(linha);
                } else {
                    onMensagemTexto(linha);
                }
            }
        } catch (IOException e) {
            onConexaoEncerrada();
        }
    }

    public void enviarMensagem(String conteudo) {
        saida.println(nome + ": " + conteudo);
    }

    public void enviarArquivo(File file) throws IOException {
        if (!file.exists()) throw new FileNotFoundException();

        saida.println("!arquivo:" + file.getName());
        byte[] dados = Files.readAllBytes(file.toPath());
        String base64 = Base64.getEncoder().encodeToString(dados);
        saida.println(base64);
        saida.println("!fim");
    }

    public PrintWriter getSaida() {
        return saida;
    }

    public BufferedReader getEntrada() {
        return entrada;
    }

    // Métodos abstratos para callback
    protected abstract void onMensagemTexto(String linha);
    protected abstract void onArquivoInicio(String nomeArquivo);
    protected abstract void onArquivoRecebido(File arquivoSalvo);
    protected abstract void onConexaoEncerrada();
}
