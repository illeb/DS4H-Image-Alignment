package histology.maindialog.event;

public class ChangeImageEventMain implements IMainDialogEvent {
    private ChangeDirection direction;
    public enum ChangeDirection {
        NEXT,
        PREV
    }
    public ChangeImageEventMain(ChangeDirection direction) {
        this.direction = direction;
    }

    public ChangeDirection getChangeDirection() {
        return this.direction;
    }
}
