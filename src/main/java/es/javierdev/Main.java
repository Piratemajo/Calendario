package es.javierdev;

import es.javierdev.ui.Calendario;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
            } catch (Exception e) {
                System.out.println(">> [ERROR] " +e.getMessage());
            }

            Calendario app = new Calendario();
            app.setVisible(true);
        });
    }
}