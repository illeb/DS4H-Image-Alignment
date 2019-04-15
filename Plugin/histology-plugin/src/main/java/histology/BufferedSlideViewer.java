package histology;

import ij.ImagePlus;
import ij.gui.ImageWindow;

public class BufferedSlideViewer extends ImageWindow {

    BufferedSlideViewer(BufferedImagesManager manager) {
        super(new ImagePlus());
    }
}
