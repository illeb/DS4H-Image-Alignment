package DS4H.maindialog.event;

public class PreviewImageEvent implements IMainDialogEvent {
    private boolean show;
    public PreviewImageEvent(boolean show) {
        this.show = show;
    }

    public boolean getValue() {
        return show;
    }
}
