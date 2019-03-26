import ij.ImagePlus;
import ij.gui.GenericDialog;
import loci.formats.FormatException;
import loci.formats.gui.BufferedImageReader;
import sun.net.www.content.text.Generic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class SideDialog {
    GenericDialog dialog;
    BufferedImageReader reader;
    ImagePlus imp;

    Button backButton = new Button("<");
    Button forwardButton = new Button(">");
    int currentImage = 0;
    // FIXME: ricavare pathfile da imp? imp.getFileInfo() è vuoto though
    public SideDialog(BufferedImageReader reader, String pathFile) throws IOException, FormatException {
        this.reader = reader;
        dialog = new GenericDialog("Histology Plugin    ");
        dialog.setResizable(false);
        dialog.hideCancelButton();
        dialog.setModalityType(Dialog.ModalityType.MODELESS);
        dialog.setOKLabel("Esegui");

        dialog.setLayout(new BoxLayout(dialog, BoxLayout.Y_AXIS));
        backButton.addActionListener(e -> {
            // currentImage = currentImage > 0 ? currentImage++ : currentImage--;
            currentImage--;
            try {
                imp.close();
                // per evitare memory leaks, invochiamo manualmente il garbage collector ad ogni cambio di immagine
                System.gc();
                imp = new ImagePlus("", reader.openImage(currentImage));
                imp.show();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
        forwardButton.addActionListener(e -> {
            currentImage++;
             try {
                imp.close();
                // per evitare memory leaks, invochiamo manualmente il garbage collector ad ogni cambio di immagine
                System.gc();
                imp = new ImagePlus("", reader.openImage(currentImage));
                imp.show();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
        dialog.add(backButton, );
        dialog.add(forwardButton);
        imp = new ImagePlus("", reader.openImage(0));
        imp.show();
    }

    public void show() {
        this.dialog.showDialog();
    }
}