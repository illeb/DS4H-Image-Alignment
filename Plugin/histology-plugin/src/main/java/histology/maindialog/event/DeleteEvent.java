package histology.maindialog.event;

public class DeleteEvent implements IMainDialogEvent {
    private int roiIndex;

    public DeleteEvent(int roiIndex) {
        this.roiIndex = roiIndex;
    }

    public int getRoiIndex() {
        return this.roiIndex;
    }
}
