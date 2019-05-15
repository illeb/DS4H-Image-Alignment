package histology.mergedialog;

import histology.maindialog.event.OpenAboutEvent;
import histology.maindialog.event.OpenFileEvent;
import histology.mergedialog.event.ExitEvent;
import histology.mergedialog.event.ReuseImageEvent;
import histology.mergedialog.event.SaveEvent;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.gui.StackWindow;

import java.awt.*;

public class MergeDialog extends StackWindow {
    public MergeDialog(ImagePlus img, OnMergeDialogEventListener listener) {
        super(img);
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem menuItem = new MenuItem("Save as...");
        menuItem.addActionListener(e -> listener.onMergeDialogEventListener(new SaveEvent()));
        fileMenu.add(menuItem);
        menuItem = new MenuItem("Reuse as source");
        menuItem.addActionListener(e -> listener.onMergeDialogEventListener(new ReuseImageEvent()));
        fileMenu.add(menuItem);
        fileMenu.addSeparator();
        menuItem = new MenuItem("Exit");
        menuItem.addActionListener(e -> listener.onMergeDialogEventListener(new ExitEvent()));
        fileMenu.add(menuItem);
        menuBar.add(fileMenu);
        this.setMenuBar(menuBar);

    }
}
