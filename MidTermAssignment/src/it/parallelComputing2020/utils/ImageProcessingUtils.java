package it.parallelComputing2020.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageProcessingUtils {
    public static int[] getMaxAllowed(JPanel jpanel) {
        int maxHeightAllowed = jpanel.getHeight();
        int maxWidthAllowed = jpanel.getWidth();
        if(Variables.maxSizeAllowed>1) { //with maxSizeAllowed <= 0 no limit is applied
            if (maxWidthAllowed > Variables.maxSizeAllowed) {
                maxWidthAllowed = Variables.maxSizeAllowed;
            }
            if (maxHeightAllowed > Variables.maxSizeAllowed) {
                maxHeightAllowed = Variables.maxSizeAllowed;
            }
        }
        return new int[]{maxWidthAllowed, maxHeightAllowed};
    }
    public static Image resizeImage(BufferedImage image, int x, int y) {
        if (((float) x / y) > ((float) image.getWidth() / image.getHeight())) {
            x = -1;
        } else {
            y = -1;
        }
        return image.getScaledInstance(x, y, Image.SCALE_SMOOTH);
    }
}
