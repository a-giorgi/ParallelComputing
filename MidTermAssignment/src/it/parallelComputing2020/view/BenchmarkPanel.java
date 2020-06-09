package it.parallelComputing2020.view;

import it.parallelComputing2020.model.Model;

import javax.swing.*;
import java.awt.*;

public class BenchmarkPanel extends JPanel implements Observer{
    private JSpinner minThreads;
    private JSpinner maxThreads;
    private JButton benchmark;
    private JButton changeFolder;
    private JLabel output;
    private Model model;
    private JLabel pathLabel;

    public BenchmarkPanel(){
        this.pathLabel = new JLabel();
        this.add(pathLabel,BorderLayout.SOUTH);
    }

    public JLabel getOutput() {
        return output;
    }

    public JButton getBenchmark() {
        return benchmark;
    }

    public JButton getChangeFolder() {
        return changeFolder;
    }

    public JSpinner getMinThreads(){
        return minThreads;
    }

    public JSpinner getMaxThreads(){
        return maxThreads;
    }



    public void setElements(JSpinner minThreads, JSpinner maxThreads,JButton benchmark, JButton changeFolder,JLabel output, JLabel pathLabel){

        this.minThreads = minThreads;
        this.maxThreads = maxThreads;
        this.benchmark = benchmark;
        this.changeFolder = changeFolder;
        this.output = output;
        this.pathLabel = pathLabel;



    }

    public void setModel(Model model){
        this.model = model;
    }


    @Override
    public void update() {
        if(model!=null){
            pathLabel.setText("Currently selected folder: "+ model.getDirectory().getPath());
        }else{
            System.out.println("Model not set!");
        }
    }
}
