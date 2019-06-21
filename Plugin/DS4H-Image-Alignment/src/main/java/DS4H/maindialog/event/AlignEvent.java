package DS4H.maindialog.event;

public class AlignEvent implements IMainDialogEvent {
    private boolean rotate;
    private boolean keepOriginal;

    public AlignEvent(boolean rotate, boolean keepOriginal) {

        this.rotate = rotate;
        this.keepOriginal = keepOriginal;
    }

    public boolean isRotate() {
        return rotate;
    }
    public boolean isKeepOriginal() {
        return keepOriginal;
    }
}
