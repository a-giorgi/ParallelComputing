package it.parallelComputing2020.model;

import it.parallelComputing2020.utils.Variables;
import it.parallelComputing2020.utils.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Model extends ObservableSubject{
    private File directory;
    private File[] imageList;
    private int threads = Runtime.getRuntime().availableProcessors();
    //In the first Version with only a sequential loader we used just an hashmap
    //private HashMap<String, BufferedImage> images ;
    private ConcurrentHashMap<String, BufferedImage> images; //Thread Safe Hash Map
    private static Model instance;
    //Stats to be displayed
    private long elapsedSequential = 0;
    private long elapsedParallel = 0;
    private boolean moreImagesThanThreads = false;
    //During benchmark we will not update the views
    private boolean benchmarking = false;


    private Model(){ //Private constructor: I don't want this class to be extended
        directory = null;
    }

    public static Model getInstance(){
        if(instance==null){
            instance = new Model();
        }
        return instance;
    }


    public void startBenchmark(){
        benchmarking = true;
    }

    public void stopBenchmark(){
        benchmarking = false;
    }

    public void loadImages() throws FileNotFoundException{
        long startTime = System.nanoTime();
        if(!directory.exists()){
            throw new FileNotFoundException("Directory not available anymore!");
        }
        if(imageList==null) {
            throw new FileNotFoundException("Directory contains no images!");
        }else{
            if(imageList.length==0){
                throw new FileNotFoundException("Directory contains no images!");
            }
            images = new ConcurrentHashMap<String, BufferedImage>(); // reallocating Hash Map to remove previous loaded images
            try {
                for (File file : imageList) {
                    try {
                        BufferedImage imageRaw = ImageIO.read(file);
                        images.put(file.getName(), imageRaw);
                        System.out.println(file.getName());
                    } catch (IOException exception) {
                        System.out.println("File " + file + "not accessible");
                    }
                }
            }catch (OutOfMemoryError exception ){
                System.out.println("Memory limit reached");
            }
            elapsedSequential = System.nanoTime() - startTime;
            if(!benchmarking){
                try {
                    notify(Aspects.IMAGES_LOADED);
                } catch (NoSuchFieldException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public void loadImagesParallel() throws FileNotFoundException {
        long startTime = System.nanoTime();
        if(Variables.numThreads!=0) {
            threads = Variables.numThreads;
        }else{
            threads = Runtime.getRuntime().availableProcessors();
        }
        //initializing
        if(!directory.exists()){
            throw new FileNotFoundException("Directory not available anymore!");
        }
        if(imageList==null) {
            throw new FileNotFoundException("Directory contains no images!");
        }else{
            if(imageList.length==0){
                throw new FileNotFoundException("Directory contains no images!");
            }
            // reallocating Hash Map to remove previous loaded images
            images = new ConcurrentHashMap<String, BufferedImage>();
            //determining offsets
            int numberOfFiles = imageList.length;
            int offset = 0; //with offset 0 the Thread won't load any image
            moreImagesThanThreads = false;
            if(numberOfFiles < threads){
                threads = numberOfFiles;
                moreImagesThanThreads = true;
            }
            Thread[] imageLoaderThreads = new ImageLoaderThread[threads];
            int elementsPerThread = numberOfFiles / threads;
            int remainder = numberOfFiles % threads; //We will distribute the remaining elements among other threads
            int remainderElement = 0;
            if(remainder!=0) {
                remainderElement = 1;
            }
            int start = 0;
            offset = elementsPerThread + remainderElement;
            for(int i = 0; i < threads; i++){
                System.out.println("Thread "+i+" will have start = "+ start+", offset = "+ offset);
                Thread loader = new ImageLoaderThread(imageList,start, offset, this);
                imageLoaderThreads[i] = loader;
                loader.start();
                start += offset;
                remainder--;
                if(remainder<1){
                    remainderElement = 0;
                }
                offset = elementsPerThread + remainderElement;
            }
            for(int i = 0; i < threads; i++){
                try {
                    imageLoaderThreads[i].join(); //need this to evaluate performances
                }catch(InterruptedException exception){
                    System.out.println("Error dealing with Thread "+i);
                }
            }
            elapsedParallel = System.nanoTime() - startTime;
            if(!benchmarking) {
                try {
                    notify(Aspects.IMAGES_LOADED);
                } catch (NoSuchFieldException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public void setDirectory(File directory){
        this.directory = directory;
        this.imageList = directory.listFiles(FileUtils.getImageFileFilter());
        images = null;
        try {
            notify(Aspects.CHANGE_DIRECTORY);
        } catch (NoSuchFieldException e) {
            System.out.println(e.getMessage());
        }
    }

    public File getDirectory() {
        return directory;
    }

    public File[] getImageList() {
        return imageList;
    }

    public BufferedImage getImage(String key){
        if(images==null){
            throw new IllegalArgumentException("Images not loaded yet!");
        }
        if(images.containsKey(key)) {
            return images.get(key);
        }else{
            throw new IllegalArgumentException("Key does not exist in Images Hash Map, the image may still be loading");
        }
    }

    public Enumeration<String> getKeys(){
        return images.keys();
    }

    public String getSequentialStats(){ //formatted with html
        String sequential;
        if(elapsedSequential!=0){
            sequential = "Time elapsed for sequential Computing: "+ elapsedSequential/1000000 + " ms<br>";
        }else{
            sequential = "Load images sequentially to display stats <br>";
        }
        return sequential ;
    }

    public String getParallelStats(){ //formatted with html
        String parallel;
        String extraData;
        if(moreImagesThanThreads){
            extraData = "<b style=\"color:red\">There were more images than threads. Number of threads was fixed to: "+threads+"</b>";
        }else{
            extraData = "";
        }
        if(elapsedParallel!=0){
            parallel = "Time elapsed for parallel Computing: "+ elapsedParallel/1000000 + " ms<br> Number of threads: "+threads+" <br>"+extraData+"<br>";
        }else{
            parallel = "Load images in parallel to display stats <br>";
        }
        return parallel;
    }

    public int getImageNumber() {
        return images.size();
    }

    public ConcurrentHashMap<String, BufferedImage> getImages() {
        return images;
    }
}