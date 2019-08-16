package DS4H;


import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;

import java.awt.*;
import java.io.File;
import java.util.Arrays;

/**
 * Class that represents an image inside the DH4S alignment program.
 */
public class BufferedImage extends ImagePlus {
    private RoiManager manager;
    private Roi[] roisBackup;
    private boolean isReduced;
    private Dimension reducedImageDimensions;
    private String filePath;
    public BufferedImage(String text, Image image, RoiManager manager, boolean isReduced) {
        super(text, image);
        this.manager = manager;
        this.isReduced = isReduced;
    }

    public BufferedImage(String text, Image image, RoiManager manager, Dimension reduceImageDimensions) {
        super(text, image);
        this.manager = manager;
        this.isReduced = true;
        this.reducedImageDimensions = reduceImageDimensions;
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

    public boolean isReduced() {
        return isReduced;
    }

    public Dimension getEditorImageDimension() {
        return reducedImageDimensions;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}