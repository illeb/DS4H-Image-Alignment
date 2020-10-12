package DS4H.AlignDialog;

import DS4H.AlignDialog.event.ExitEvent;
import DS4H.AlignDialog.event.ReuseImageEvent;
import DS4H.AlignDialog.event.SaveEvent;
import ij.ImagePlus;
import ij.gui.StackWindow;

import java.awt.*;

public class AlignDialog extends StackWindow {
    public AlignDialog(ImagePlus img, OnAlignDialogEventListener listener) {
        super(img);
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem menuItem = new MenuItem("Save as...");
        menuItem.addActionListener(e -> listener.onAlignDialogEventListener(new SaveEvent()));
        fileMenu.add(menuItem);
        menuItem = new MenuItem("Reuse as source");
        menuItem.addActionListener(e -> listener.onAlignDialogEventListener(new ReuseImageEvent()));
        fileMenu.add(menuItem);
        fileMenu.addSeparator();
        menuItem = new MenuItem("Exit");
        menuItem.addActionListener(e -> listener.onAlignDialogEventListener(new ExitEvent()));
        fileMenu.add(menuItem);
        menuBar.add(fileMenu);
        this.setMenuBar(menuBar);

    }
}
