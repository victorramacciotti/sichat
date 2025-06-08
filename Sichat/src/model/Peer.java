package model;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Peer {
    private static final int PORTA = 50001;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Seu nome: ");
        String nome = scanner.nextLine();

        System.out.print("IP do outro peer: ");
        String ip = scanner.nextLine().trim();

        Socket socket = null;

        try {
            socket = new Client(ip, PORTA).conectar();
        } catch (IOException e) {
            try {
                System.out.println("Não foi possível conectar. Iniciando como servidor...");
                socket = new Server(PORTA).esperarConexao();
            } catch (IOException ex) {
                System.err.println("Erro ao iniciar servidor: " + ex.getMessage());
                return;
            }
        }

        try {
            MessageHandler handler = new MessageHandler(socket.getInputStream(), socket.getOutputStream(), nome);
            handler.iniciarRecebimento();
            handler.enviar(scanner);
        } catch (IOException e) {
            System.err.println("Erro na comunicação: " + e.getMessage());
        }
    }
}
