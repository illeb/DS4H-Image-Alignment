package DS4H.MainDialog.event;

public class DeselectedRoiEvent implements IMainDialogEvent {
    private int roiIndex;
    public DeselectedRoiEvent(int index) {
        this.roiIndex = index;
    }

    public int getRoiIndex() {
        return this.roiIndex;
    }

}
