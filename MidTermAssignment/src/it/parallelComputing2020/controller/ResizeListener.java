package it.parallelComputing2020.controller;

import it.parallelComputing2020.model.Model;
import it.parallelComputing2020.utils.ImageProcessingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

public class ResizeListener extends ComponentAdapter {

    private Timer resizeTimer; //I need this to resize image only once I've finish resizing the frame
    private ComponentEvent resizeEvent;
    private int resizeDelay = 50;
    private Model model;


    public void updateImagePanel(JPanel displayImage) throws IllegalAccessException {
        String key = null;
        for (Component imageWrapper : displayImage.getComponents()) {
            if ( imageWrapper instanceof JLabel ) {
                ImageIcon imageIcon = (ImageIcon) ((JLabel) imageWrapper).getIcon();
                if(imageIcon == null){
                    throw new IllegalAccessException("Image not selected or image not loaded yet");
                }
                imageIcon.getDescription();
                key = imageIcon.getDescription();
            }
        }
        if( key == null ){
            throw new IllegalAccessException("No key available!");
        }

        BufferedImage imageraw = model.getImage(key);
        int[] maxSizeAllowed = ImageProcessingUtils.getMaxAllowed(displayImage);

        //wrapping the image into a JLabel
        ImageIcon scaledImage = new ImageIcon(ImageProcessingUtils.resizeImage(imageraw, maxSizeAllowed[0], maxSizeAllowed[1]), key);
        JLabel label = new JLabel("", scaledImage, JLabel.CENTER);

        //changing the image
        displayImage.removeAll();

        //adding the image to this view
        displayImage.add( label, BorderLayout.CENTER );
        displayImage.revalidate();

    }

    public ResizeListener(Model model){
        super();
        this.model = model;
        ActionListener resizeImage = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JPanel displayImage = (JPanel) resizeEvent.getComponent();
                if(displayImage.isShowing()) {
                    try {
                        updateImagePanel(displayImage);
                    }catch(IllegalArgumentException exception){
                        displayImage.removeAll();
                        displayImage.add(new JLabel("The selected image is not loaded yet", JLabel.CENTER));
                    }catch (ClassCastException exception){
                        JOptionPane.showMessageDialog(displayImage, exception.getMessage(),"Error!",JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    }catch(IllegalAccessException  exception){
                        System.out.println(exception.getMessage());
                    }
                }
            }
        };
        resizeTimer = new ResizeTimer(resizeDelay,resizeImage); //only when the timer ends I'll resize the image (there is no "on finish resizing" equivalent)
        resizeTimer.setRepeats(false);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        super.componentResized(e);
        resizeEvent = e;
        if(resizeTimer.isRunning()){
            resizeTimer.restart();
        }else{
            resizeTimer.start();
        }

    }

    @Override
    public void componentShown(ComponentEvent e) {
        super.componentShown(e);
        JPanel imagePanel = (JPanel) e.getComponent();
        try {
            updateImagePanel(imagePanel);
        }catch(IllegalArgumentException exception){
            imagePanel.removeAll();
            imagePanel.add(new JLabel("The selected image is not loaded yet", JLabel.CENTER));
        }catch (ClassCastException exception){
            JOptionPane.showMessageDialog(imagePanel, exception.getMessage(),"Error!",JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }catch(IllegalAccessException  exception){
            System.out.println(exception.getMessage());
        }
    }
}
