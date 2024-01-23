package it.parallelComputing2020.model;

import it.parallelComputing2020.view.Observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class ObservableSubject { //implementing multiple aspects: see GOF Design Patterns, Observer implementation (pull)
    protected HashMap<Aspects,ArrayList<Observer>> observersMap = new HashMap<Aspects,ArrayList<Observer>>();
    protected HashMap<String, ConcurrentLinkedQueue<Observer>> imageObserversMap = new HashMap<String,ConcurrentLinkedQueue<Observer>>(); //this is for concurrent notifications
    //It has to be concurrent because this update will happen outside main thread otherwise it will throw a ConcurrentModificationException

    public void attach(Observer o, Aspects aspect){
        if(observersMap.containsKey(aspect)){
            observersMap.get(aspect).add(o);
        }else{
            ArrayList<Observer> newAspectList = new ArrayList<Observer>();
            newAspectList.add(o);
            observersMap.put(aspect,newAspectList);
        }
    }
    public void detach(Observer o, Aspects aspect) throws NoSuchFieldException{
        if(!observersMap.containsKey(aspect)){
            throw new NoSuchFieldException("Aspect not found!");
        }
        observersMap.get(aspect).remove(o);
        if(observersMap.get(aspect).isEmpty()){
            observersMap.remove(aspect);
        }
    }
    public void notify(Observer o){
        o.update();
    }
    public void notify(Aspects aspect) throws NoSuchFieldException {
        if(!observersMap.containsKey(aspect)){
            throw new NoSuchFieldException("Aspect not found!");
        }
        ArrayList<Observer> observerToNotify = observersMap.get(aspect);
        for(Observer o: observerToNotify){
            o.update();
        }
    }

    //the following methods have a different signature and are used to notify when single images are loaded
    public void notify(String filename){
        if(imageObserversMap.containsKey(filename)){
            ConcurrentLinkedQueue<Observer> observerToNotify = imageObserversMap.get(filename);
            for(Observer o: observerToNotify){
                o.update();
            }
        }
    }
    public void attach(Observer o, String filename){
        if(imageObserversMap.containsKey(filename)){
            imageObserversMap.get(filename).add(o);
        }else{
            ConcurrentLinkedQueue<Observer> newFilenameList = new ConcurrentLinkedQueue<Observer>();
            newFilenameList.add(o);
            imageObserversMap.put(filename,newFilenameList);
        }
        System.out.println("Observer starts observe "+ filename);
    }
    public void detach(Observer o, String filename){
        if(imageObserversMap.containsKey(filename)){
            imageObserversMap.get(filename).remove(o);
            if(imageObserversMap.get(filename).isEmpty()){
                imageObserversMap.remove(filename);
            }
            System.out.println("Observer stops observe "+ filename);
        }

    }
    public void detachImageObservers(){
        imageObserversMap.clear();
        System.out.println("All image observers are detached");
    }

}

