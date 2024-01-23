package it.parallelComputing2020.view;

import it.parallelComputing2020.model.Model;

import javax.swing.*;
import java.awt.*;

public class StatsPanel extends JPanel implements Observer {
    private Model model;

    @Override
    public void update() {
        if(model!=null) {
            setMessage();
        }else{
            System.out.println("Model not set!");
        }
    }

    public void setModel(Model model){
        this.model = model;
        setMessage();
    }

    public void setMessage(){
        for (Component statsLabel : this.getComponents()) {
            if ( statsLabel instanceof JLabel ) {
                ((JLabel) statsLabel).setText("<html>"+model.getSequentialStats() + "<br>" + model.getParallelStats() +"</html>");
            }
        }
    }
}
