package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int PORTA = 50001;
    private static final List<PrintWriter> clientes = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Servidor iniciado na porta " + PORTA);

        try (ServerSocket servidor = new ServerSocket(PORTA)) {
            while (true) {
                Socket socket = servidor.accept();
                System.out.println("Novo cliente conectado: " + socket.getInetAddress());

                PrintWriter saida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                clientes.add(saida);

                Thread threadCliente = new Thread(() -> {
                    try (
                        BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()))
                    ) {
                        String msg;
                        while ((msg = entrada.readLine()) != null) {
                            System.out.println(msg);
                            for (PrintWriter cliente : clientes) {
                                if (cliente != saida) {
                                    cliente.println(msg);
                                }
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Erro com cliente: " + e.getMessage());
                    } finally {
                        clientes.remove(saida);
                        try {
                            socket.close();
                        } catch (IOException ignored) {}
                        System.out.println("Cliente desconectado.");
                    }
                });

                threadCliente.start();
            }
        } catch (IOException e) {
            System.out.println("Erro no servidor: " + e.getMessage());
        }
    }
}