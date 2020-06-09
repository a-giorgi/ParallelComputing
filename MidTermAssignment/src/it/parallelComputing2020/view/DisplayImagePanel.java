package it.parallelComputing2020.view;

import it.parallelComputing2020.model.Model;
import it.parallelComputing2020.utils.ImageProcessingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class DisplayImagePanel extends JPanel implements Observer {

    private Model model;
    private String filenameObserved;


    @Override
    public void update() {
        if(model!=null){
            try {
                if(filenameObserved!= null) {
                    System.out.println("Image "+filenameObserved+" is now available!");
                    BufferedImage image = model.getImage(filenameObserved);

                    //resizing the image to the ImagePanel (but capped to maxSizeAllowed, see Class Variables)
                    int[] maxValuesAllowed = ImageProcessingUtils.getMaxAllowed(this);
                    Image scaledImage = ImageProcessingUtils.resizeImage(image, maxValuesAllowed[0], maxValuesAllowed[1]);

                    ImageIcon imageIcon = new ImageIcon(scaledImage, filenameObserved);
                    JLabel label = new JLabel("", imageIcon, JLabel.CENTER);
                    changeImage(label);
                    model.detach(this, filenameObserved);
                    System.out.println("DisplayImagePanel stops observe "+filenameObserved);
                    filenameObserved = null;
                }
            }catch(IllegalArgumentException exception){
                JLabel label = new JLabel("Filename does not exist",JLabel.CENTER);
                changeImage(label);
            }
        }else{
            System.out.println("Model not set!");
        }
    }

    public void changeImage(JLabel label){
        this.removeAll();

        //adding the image to this view
        this.add( label, BorderLayout.CENTER );
        this.revalidate();

    }

    public void setModel(Model model){
        this.model = model;
    }


    public void setFilenameObserved(String filenameObserved) {
        this.filenameObserved = filenameObserved;
    }
}
