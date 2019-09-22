package DS4H.RemoveDialog.event;

public class RemoveImageEvent implements IRemoveDialogEvent {

    private int imageFileIndex;

    public RemoveImageEvent(int imageFileIndex) {
        this.imageFileIndex = imageFileIndex;
    }

    public int getImageFileIndex() {
        return imageFileIndex;
    }
}
