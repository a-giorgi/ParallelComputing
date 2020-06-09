package it.parallelComputing2020.view;



import it.parallelComputing2020.model.Model;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.util.Enumeration;

public class ImageListPanel extends JPanel implements Observer {

    private JTable table;
    private Model model;
    private JButton sequentially;
    private JButton parallel;
    private JButton changeFolder;
    private JLabel info;


    public void setModel(Model model) {
        this.model = model;
    }

    public void setOutputTable(JTable table){
        DefaultTableModel tableData = new DefaultTableModel();
        tableData.addColumn("Loaded Images");
        table.setModel(tableData);
        table.setDefaultEditor(Object.class, null); //this will prevent data to be edited
        this.table = table;

    }

    public void setButtons(JButton sequentially,JButton parallel, JButton changeFolder){
        this.parallel = parallel;
        this.sequentially = sequentially;
        this.changeFolder = changeFolder;
    }

    public JButton getSequentially() {
        return sequentially;
    }

    public JButton getParallel() {
        return parallel;
    }

    public JButton getChangeFolder() {
        return changeFolder;
    }

    public void setInfo(JLabel info){
        this.info = info;
    }

    public JLabel getInfo() {
        return info;
    }

    public JTable getTable() {
        return table;
    }

    public void update() {
        if(model!=null) {
            info.setText("Currently selected folder: " + model.getDirectory().getPath());
            File[] imageList = model.getImageList();
            DefaultTableModel tableData = new DefaultTableModel();
            this.getTable().setModel(tableData);
            tableData.addColumn("Loaded Images");
            for (File file : imageList) {
                tableData.addRow(new String[]{file.getName()});
            }
        }else{
            System.out.println("Model not set!");
        }
    }

}
