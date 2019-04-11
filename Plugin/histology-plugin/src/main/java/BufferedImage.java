import ij.ImagePlus;
import ij.plugin.frame.RoiManager;

import java.awt.*;

public class BufferedImage extends ImagePlus {
    private RoiManager manager;
    BufferedImage(String text, Image image, RoiManager manager) {
        super(text, image);
        this.manager = manager;
    }

    RoiManager getManager() {
        return this.manager;
    }
}
