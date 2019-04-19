package histology.maindialog.event;

public class DeselectedRoiEventMain implements IMainDialogEvent {
    private int roiIndex;
    public DeselectedRoiEventMain(int index) {
        this.roiIndex = index;
    }

    public int getRoiIndex() {
        return this.roiIndex;
    }

}
