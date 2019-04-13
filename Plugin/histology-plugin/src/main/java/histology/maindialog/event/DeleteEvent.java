package histology.maindialog.event;

public class DeleteEvent implements IDialogEvent {
    private int roiIndex;

    public DeleteEvent(int roiIndex) {
        this.roiIndex = roiIndex;
    }

    public int getRoiIndex() {
        return this.roiIndex;
    }
}
