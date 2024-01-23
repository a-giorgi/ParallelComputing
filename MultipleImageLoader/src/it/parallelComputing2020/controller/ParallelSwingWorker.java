package it.parallelComputing2020.controller;

import it.parallelComputing2020.model.Model;
import it.parallelComputing2020.view.BenchmarkPanel;
import it.parallelComputing2020.view.ImageListPanel;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;

class ParallelSwingWorker extends SwingWorker<String , Integer>
{
    private Model model;
    private ImageListPanel view;
    private BenchmarkPanel benchmarkPanel;

    protected String doInBackground(){
        try {
            model.loadImagesParallel();
        } catch (FileNotFoundException exception) {
            return exception.getMessage();
        }
        return "Images Loaded";
    }

    protected void done()
    {
        try {
            JOptionPane.showMessageDialog(view, get());
        } catch (InterruptedException | ExecutionException exception) {
            JOptionPane.showMessageDialog(view, exception.getMessage(),"Error!",JOptionPane.ERROR_MESSAGE);
        }

        //enabling previously disabled buttons
        benchmarkPanel.getChangeFolder().setEnabled(true);
        benchmarkPanel.getBenchmark().setEnabled(true);
        view.getParallel().setText("Load images in parallel");
        view.getParallel().setEnabled(true);
        view.getChangeFolder().setEnabled(true);
        view.getSequentially().setEnabled(true);

    }

    public ParallelSwingWorker(ImageListPanel view,BenchmarkPanel benchmarkPanel, Model model){
        this.view = view;
        this.benchmarkPanel = benchmarkPanel;
        this.model = model;

    }
}