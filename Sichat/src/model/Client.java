package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        String servidorIP = "localhost";
        int porta = 50001;

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Digite seu nome: ");
            String nome = scanner.nextLine();

            try (
                Socket socket = new Socket(servidorIP, porta);
                PrintWriter saida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                System.out.println("Conectado como " + nome);

                Thread receber = new Thread(() -> {
                    try {
                        String linha;
                        boolean recebendoArquivo = false;
                        String nomeArquivo = "";
                        PrintWriter escritorArquivo = null;

                        while ((linha = entrada.readLine()) != null) {
                            if (linha.startsWith("!arquivo:")) {
                                nomeArquivo = linha.substring(9).trim();
                                escritorArquivo = new PrintWriter("recebido_" + nomeArquivo);
                                recebendoArquivo = true;
                                System.out.println("Recebendo arquivo: " + nomeArquivo);
                            } else if (linha.equals("!fim")) {
                                if (escritorArquivo != null) {
                                    escritorArquivo.close();
                                    System.out.println("Arquivo salvo: recebido_" + nomeArquivo);
                                }
                                recebendoArquivo = false;
                            } else if (recebendoArquivo && escritorArquivo != null) {
                                escritorArquivo.println(linha);
                            } else {
                                System.out.println(linha);
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Desconectado do servidor.");
                    }
                });
                receber.start();

                while (true) {
                    String msg = scanner.nextLine();
                    if (msg.startsWith("!arquivo ")) {
                        String caminho = msg.substring(9).trim();
                        File arquivo = new File(caminho);
                        if (arquivo.exists() && arquivo.isFile()) {
                            saida.println("!arquivo:" + arquivo.getName());
                            try (BufferedReader leitor = new BufferedReader(new FileReader(arquivo))) {
                                String linha;
                                while ((linha = leitor.readLine()) != null) {
                                    saida.println(linha);
                                }
                            }
                            saida.println("!fim");
                            System.out.println("Arquivo enviado.");
                        } else {
                            System.out.println("Arquivo não encontrado.");
                        }
                    } else {
                        saida.println(nome + ": " + msg);
                    }
                }

            } catch (IOException e) {
                System.out.println("Erro ao conectar: " + e.getMessage());
            }
        }
    }
}