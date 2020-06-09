package it.parallelComputing2020;

import it.parallelComputing2020.controller.PanelsController;
import it.parallelComputing2020.model.Model;
import it.parallelComputing2020.view.Gui;

import javax.swing.*;


public class Main {
    public static void main(String[] args){
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        Model model = Model.getInstance();
        Gui gui = new Gui(model);
        JFrame frame = new JFrame("Multiple Image Loader");
        frame.setContentPane(gui.getTabbedPane1());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640,480);
        frame.setVisible(true);

        PanelsController panelsController = new PanelsController(model, gui.getImageListPanel(), gui.getImagePanel(), gui.getBenchmarkPanel());
    }

}