package DS4H.MainDialog.event;

public class SelectedRoiEvent implements IMainDialogEvent {
    private int roiIndex;
    public SelectedRoiEvent(int index) {
        this.roiIndex = index;
    }

    public int getRoiIndex() {
        return this.roiIndex;
    }

}
