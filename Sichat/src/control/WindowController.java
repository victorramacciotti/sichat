package control;

import view.Window;

import javax.swing.SwingUtilities;

public class WindowController {

    public void iniciar() {
        SwingUtilities.invokeLater(() -> {
            new Window();
        });
    }

    public static void main(String[] args) {
        new WindowController().iniciar();
    }
}
