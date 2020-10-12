package DS4H.MainDialog.event;

public class SelectedRoiFromOvalEvent implements IMainDialogEvent {
    private int roiIndex;
    public SelectedRoiFromOvalEvent(int index) {
        this.roiIndex = index;
    }
    public int getRoiIndex() {
        return this.roiIndex;
    }
}
