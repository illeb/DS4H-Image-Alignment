package histology.maindialog.event;

public class SelectedRoiEventMain implements IMainDialogEvent {
    private int roiIndex;
    public SelectedRoiEventMain(int index) {
        this.roiIndex = index;
    }

    public int getRoiIndex() {
        return this.roiIndex;
    }

}
