package fr.iut.sae.app.main;

import fr.iut.sae.app.controller.LoginController;
import fr.iut.sae.app.view.LoginFrame;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {
            }

            LoginFrame f = new LoginFrame();
            new LoginController(f);
            f.setVisible(true);
        });
    }
}
