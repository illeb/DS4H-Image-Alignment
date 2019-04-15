package histology;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;

import java.awt.*;
import java.util.Arrays;

public class BufferedImage extends ImagePlus {
    private RoiManager manager;
    private Roi[] roisBackup;
    BufferedImage(String text, Image image, RoiManager manager) {
        super(text, image);
        this.manager = manager;
    }

    public RoiManager getManager() {
        return this.manager;
    }

    public void restoreRois() {
        Arrays.stream(this.roisBackup).forEach(roi -> manager.add(this, roi, 0));
    }

    public void backupRois() {
        this.roisBackup = this.manager.getRoisAsArray();
    }
}
