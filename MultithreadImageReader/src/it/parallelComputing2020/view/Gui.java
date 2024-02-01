package it.parallelComputing2020.view;

import it.parallelComputing2020.model.Aspects;
import it.parallelComputing2020.model.Model;

import javax.swing.*;

public class Gui {
    private JPanel panel1;
    private JPanel mainPanel;
    private JTabbedPane tabbedPane1;
    private JPanel imagePanel;
    private JPanel imageListPanel;
    private JTable table1;
    private JButton sequentially;
    private JButton parallel;
    private JLabel infoLabel;
    private JButton changeFolderButton;
    private JPanel imageListBtnWrapper;
    private JPanel statsPanel;
    private JLabel statsLabel;
    private JPanel benchmarkPanel;
    private JSpinner minThreads;
    private JButton changeFolderButton1;
    private JButton startBenchmarkButton;
    private JSpinner maxThreads;
    private JLabel outputLabel;
    private JPanel info;
    private JLabel pathLabel;
    private JLabel displayImage;
    private Model model;

    private void createUIComponents() {
        imageListPanel = new ImageListPanel();
        statsPanel = new StatsPanel();
        benchmarkPanel = new BenchmarkPanel();
        SpinnerModel acceptedValueMin = new SpinnerNumberModel(1, 1, 1000, 1);
        minThreads = new JSpinner(acceptedValueMin);
        SpinnerModel acceptedValueMax = new SpinnerNumberModel(8, 1, 1000, 1);
        maxThreads = new JSpinner(acceptedValueMax);
        imagePanel = new DisplayImagePanel();

        //Blocking user to write text inside JSpinners
        ((JSpinner.DefaultEditor) minThreads.getEditor()).getTextField().setEditable(false);
        ((JSpinner.DefaultEditor) maxThreads.getEditor()).getTextField().setEditable(false);

    }

    public Gui(Model model){
        ((ImageListPanel) imageListPanel).setModel(model);
        ((StatsPanel) statsPanel).setModel(model);
        ((BenchmarkPanel) benchmarkPanel).setModel(model);
        ((DisplayImagePanel) imagePanel).setModel(model);
        this.model = model;
        model.attach((ImageListPanel) imageListPanel, Aspects.CHANGE_DIRECTORY);
        model.attach((StatsPanel) statsPanel, Aspects.IMAGES_LOADED);
        model.attach((BenchmarkPanel) benchmarkPanel, Aspects.CHANGE_DIRECTORY);

        ((ImageListPanel) imageListPanel).setOutputTable(table1);
        ((ImageListPanel) imageListPanel).setButtons(sequentially, parallel, changeFolderButton);
        ((ImageListPanel) imageListPanel).setInfo(infoLabel);
        ((BenchmarkPanel) benchmarkPanel).setElements(minThreads,maxThreads,startBenchmarkButton,changeFolderButton1,outputLabel, pathLabel);
        imageListBtnWrapper.setLayout(new WrapLayout());

    }


    public JPanel getImagePanel() {
        return imagePanel;
    }

    public JTabbedPane getTabbedPane1() {
        return tabbedPane1;
    }

    public JPanel getImageListPanel() {
        return imageListPanel;
    }

    public JPanel getBenchmarkPanel() {
        return benchmarkPanel;
    }


}
