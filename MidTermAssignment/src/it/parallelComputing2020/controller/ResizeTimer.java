package it.parallelComputing2020.controller;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;

public class ResizeTimer extends Timer {
    private ComponentEvent evt;
    public ResizeTimer(int delay, ActionListener listener) {
        super(delay, listener);
    }
    public void firedBy(ComponentEvent e){
        this.evt = e;
    }

}
