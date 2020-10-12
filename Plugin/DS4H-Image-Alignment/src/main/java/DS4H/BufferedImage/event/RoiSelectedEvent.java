package DS4H.BufferedImage.event;

import ij.gui.Roi;

public class RoiSelectedEvent implements IBufferedImageEvent {

    private Roi roiSelected;

    public RoiSelectedEvent(Roi roiSelected) {
        this.roiSelected = roiSelected;
    }

    public Roi getRoiSelected() {
        return this.roiSelected;
    }
}
