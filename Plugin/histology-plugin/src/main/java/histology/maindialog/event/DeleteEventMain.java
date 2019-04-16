package histology.maindialog.event;

public class DeleteEventMain implements IMainDialogEvent {
    private int roiIndex;

    public DeleteEventMain(int roiIndex) {
        this.roiIndex = roiIndex;
    }

    public int getRoiIndex() {
        return this.roiIndex;
    }
}
