package histology.maindialog.event;

import java.awt.*;

public class SelectedRoiEvent implements IDialogEvent {
    private int roiIndex;
    public SelectedRoiEvent(int index) {
        this.roiIndex = index;
    }

    public int getRoiIndex() {
        return this.roiIndex;
    }

}
