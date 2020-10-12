package DS4H.MainDialog.event;

public class PreviewImageEvent implements IMainDialogEvent {
    private boolean show;
    public PreviewImageEvent(boolean show) {
        this.show = show;
    }

    public boolean getValue() {
        return show;
    }
}
