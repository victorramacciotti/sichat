package view;

import javax.swing.JFrame;
import java.awt.Dimension;

public class Window extends JFrame {

    public Window() {
        setTitle("Chat com Interface");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(900, 700));
        setResizable(false);

        MainScreen mainScreen = new MainScreen();
        setContentPane(mainScreen);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
