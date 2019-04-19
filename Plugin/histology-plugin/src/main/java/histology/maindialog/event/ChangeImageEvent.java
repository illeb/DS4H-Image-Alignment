package histology.maindialog.event;

public class ChangeImageEvent implements IMainDialogEvent {
    private ChangeDirection direction;
    public enum ChangeDirection {
        NEXT,
        PREV
    }
    public ChangeImageEvent(ChangeDirection direction) {
        this.direction = direction;
    }

    public ChangeDirection getChangeDirection() {
        return this.direction;
    }
}
