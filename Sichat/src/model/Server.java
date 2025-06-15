package model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final int porta;

    public Server(int porta) {
        this.porta = porta;
    }

    @SuppressWarnings("resource")
	public Socket esperarConexao() throws IOException {
        ServerSocket serverSocket = new ServerSocket(porta);
        System.out.println("Servidor aguardando conexão na porta " + porta + "...");
        return serverSocket.accept();
    }
}
