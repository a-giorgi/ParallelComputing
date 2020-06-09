package it.parallelComputing2020.model;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageLoaderThread extends Thread {
    private File[] files;
    private Model model;
    private int start;
    private int offset;

    public ImageLoaderThread(File[] files, int start, int offset, Model model) {
        this.files = files;
        this.start = start;
        this.offset = offset;
        this.model = model;
    }

    @Override
    public void run(){
        try {
            for (int i = start; i < start+offset; i++) {
                try {
                    //this.sleep(10000);// to check all features uncomment this to slow down the threads
                    BufferedImage imageRaw = ImageIO.read(files[i]);
                    model.getImages().put(files[i].getName(), imageRaw);
                    model.notify(files[i].getName());
                    System.out.println(files[i].getName());
                } catch (IOException exception) {
                    System.out.println("File " + files[i] + "not accessible");
                //} catch (InterruptedException e) {
                //    e.printStackTrace();
                }
            }
        }catch (OutOfMemoryError exception ){
            System.out.println("Memory limit reached");
        }

    }

}
