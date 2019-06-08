package DS4H.maindialog.event;

public class AlignEvent implements IMainDialogEvent {
    private boolean rotate;

    public AlignEvent(boolean rotate) {

        this.rotate = rotate;
    }

    public boolean isRotate() {
        return rotate;
    }
}
