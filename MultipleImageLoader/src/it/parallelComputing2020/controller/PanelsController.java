package it.parallelComputing2020.controller;

import it.parallelComputing2020.model.Model;
import it.parallelComputing2020.utils.ImageProcessingUtils;
import it.parallelComputing2020.utils.Variables;
import it.parallelComputing2020.view.BenchmarkPanel;
import it.parallelComputing2020.view.DisplayImagePanel;
import it.parallelComputing2020.view.ImageListPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;

public class PanelsController {
    private Model model;
    private ImageListPanel view;
    private JPanel displayImage;
    private BenchmarkPanel benchmarkView;
    private String filenameObserved;

    public PanelsController(Model model, JPanel view, JPanel displayImage, JPanel benchmarkView){
        this.view = (ImageListPanel) view;
        this.model = model;
        this.displayImage = displayImage;
        this.benchmarkView = (BenchmarkPanel) benchmarkView;

        if(!chooseFolder()){
            System.exit(1);
        }
        setUpListeners();

    }

    public boolean chooseFolder(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(view);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            model.setDirectory(selected);
            return true;
        }else{
            return false;

        }

    }

    public void changeImage(JLabel label){
        displayImage.removeAll();

        //adding the image to this view
        displayImage.add( label, BorderLayout.CENTER );
        displayImage.revalidate();

    }

    public void setUpListeners(){
        view.getSequentially().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    //Clearing the image displayed
                    displayImage.removeAll();
                    displayImage.add(new JLabel("Select an image to be displayed here",JLabel.CENTER));

                    //removing Observers for parallel image loading
                    model.detachImageObservers();
                    filenameObserved = null;

                    //loading the images sequentially
                    model.loadImages();

                    JOptionPane.showMessageDialog(view, "Images Loaded");
                }catch (FileNotFoundException  exception){
                    JOptionPane.showMessageDialog(benchmarkView, exception.getMessage(),"Error!",JOptionPane.ERROR_MESSAGE);
                    System.out.println(exception.getMessage());
                }
            }
        });

        view.getParallel().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    //Clearing the image displayed
                    displayImage.removeAll();
                    displayImage.add(new JLabel("Select an image to be displayed here",JLabel.CENTER));

                    //disabling buttons to lock other operations
                    view.getChangeFolder().setEnabled(false);
                    view.getParallel().setText("Loading, please wait...");
                    view.getParallel().setEnabled(false);
                    view.getSequentially().setEnabled(false);
                    benchmarkView.getBenchmark().setEnabled(false);
                    benchmarkView.getChangeFolder().setEnabled(false);

                    //loading the images in parallel
                    ParallelSwingWorker imageloader = new ParallelSwingWorker(view, benchmarkView, model);
                    imageloader.execute();

                }catch (Exception  exception){
                    JOptionPane.showMessageDialog(benchmarkView, exception.getMessage(),"Error!",JOptionPane.ERROR_MESSAGE);
                    System.out.println(exception.getMessage());
                }
            }
        });

        view.getTable().addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                JTable table =(JTable) mouseEvent.getSource();
                Point point = mouseEvent.getPoint();
                int row = table.rowAtPoint(point);
                String fileName = (String) table.getValueAt(row, 0);
                if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
                    if(filenameObserved!=null){
                        model.detach((DisplayImagePanel) displayImage, filenameObserved);
                        ((DisplayImagePanel) displayImage).setFilenameObserved(null);
                        filenameObserved = null;
                    }
                    try {
                        BufferedImage image = model.getImage(fileName);

                        //resizing the image to the ImagePanel (but capped to maxSizeAllowed, see Class Variables)
                        int[] maxValuesAllowed = ImageProcessingUtils.getMaxAllowed(displayImage);
                        Image scaledImage = ImageProcessingUtils.resizeImage(image, maxValuesAllowed[0], maxValuesAllowed[1]);

                        ImageIcon imageIcon = new ImageIcon(scaledImage, fileName);
                        JLabel label = new JLabel("", imageIcon, JLabel.CENTER);
                        changeImage(label);
                    }catch(IllegalArgumentException exception){
                        JLabel label = new JLabel("The selected image is not loaded yet",JLabel.CENTER);
                        changeImage(label);
                        //if the image is not available we'll start observing it
                        model.attach((DisplayImagePanel) displayImage, fileName);
                        ((DisplayImagePanel) displayImage).setFilenameObserved(fileName);
                        filenameObserved = fileName;
                    }
                }
            }
        });

        view.getChangeFolder().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(chooseFolder()) {
                    changeImage(new JLabel("Select an image to be displayed here", JLabel.CENTER));
                    model.detachImageObservers();
                    filenameObserved = null;
                }
            }

        });

        benchmarkView.getChangeFolder().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(chooseFolder()) {
                    changeImage(new JLabel("Select an image to be displayed here", JLabel.CENTER));
                    model.detachImageObservers();
                    filenameObserved = null;
                }
            }

        });

        benchmarkView.getBenchmark().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int minThreads = (int) benchmarkView.getMinThreads().getValue();
                int maxThreads = (int) benchmarkView.getMaxThreads().getValue();
                if(minThreads>maxThreads){
                    JOptionPane.showMessageDialog(benchmarkView, "The number of max threads must be greater than the number of min threads!","Error!",JOptionPane.ERROR_MESSAGE);
                }else{
                    //we won't update other views during benchmark, since measuring performances is our only interest this time
                    model.detachImageObservers();
                    filenameObserved = null;

                    model.startBenchmark();
                    StringBuilder benchmarkingStats = new StringBuilder();
                    int previousNumThreads = Variables.numThreads;
                    try {
                        model.loadImages();
                        benchmarkingStats.append(model.getSequentialStats() + "Number of Images Loaded: "+model.getImageNumber()+"<br><hr>");
                        for (int i = minThreads; i <= maxThreads; i++) {
                            Variables.numThreads = i;
                            model.loadImagesParallel();
                            benchmarkingStats.append(model.getParallelStats() + "Number of Images Loaded: "+model.getImageNumber()+"<br><hr>" );
                        }
                    }catch(FileNotFoundException exception){
                        System.out.println(exception.getMessage());
                        JOptionPane.showMessageDialog(benchmarkView, exception.getMessage(),"Error!",JOptionPane.ERROR_MESSAGE);
                        benchmarkingStats = new StringBuilder(exception.getMessage());
                    }
                    Variables.numThreads = previousNumThreads;
                    model.stopBenchmark();
                    benchmarkView.getOutput().setText("<html>"+benchmarkingStats.toString()+"</html>");
                }
            }
        });

        ResizeListener rs = new ResizeListener(model);
        displayImage.addComponentListener(rs);



    }
}
