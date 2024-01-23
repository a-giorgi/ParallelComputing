package it.parallelComputing2020.utils;

import java.io.File;

public class FileUtils {
    public static java.io.FileFilter getImageFileFilter(){
        return new java.io.FileFilter() {
            private final String[] okFileExtensions = new String[] { "jpg", "jpeg", "png", "gif" };

            public boolean accept(File file) {
                for (String extension : okFileExtensions) {
                    if (file.getName().toLowerCase().endsWith(extension)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
