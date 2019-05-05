package histology.maindialog.event;

public class DeleteRoiEvent implements IMainDialogEvent {
    private int roiIndex;

    public DeleteRoiEvent(int roiIndex) {
        this.roiIndex = roiIndex;
    }

    public int getRoiIndex() {
        return this.roiIndex;
    }
}
