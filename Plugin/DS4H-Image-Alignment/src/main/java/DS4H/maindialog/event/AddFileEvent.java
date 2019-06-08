package DS4H.maindialog.event;

public class AddFileEvent implements IMainDialogEvent {
    private String filePath = "";

    public AddFileEvent(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}
