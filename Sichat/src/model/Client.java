package model;

import java.io.IOException;
import java.net.Socket;

public class Client {
    private final String ip;
    private final int porta;

    public Client(String ip, int porta) {
        this.ip = ip;
        this.porta = porta;
    }

    public Socket conectar() throws IOException {
        System.out.println("Tentando conectar ao peer em " + ip + ":" + porta + "...");
        return new Socket(ip, porta);
    }
}
