package histology.maindialog.event;

public class PreviewImageEventMain implements IMainDialogEvent {
    private boolean show;
    public PreviewImageEventMain(boolean show) {
        this.show = show;
    }

    public boolean getValue() {
        return show;
    }
}
