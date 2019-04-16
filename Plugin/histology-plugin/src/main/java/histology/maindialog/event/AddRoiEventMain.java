package histology.maindialog.event;

import java.awt.*;

public class AddRoiEventMain implements IMainDialogEvent {
    private Point coords;
    public AddRoiEventMain(Point coords) {
        this.coords = coords;
    }

    public Point getClickCoords() {
        return this.coords;
    }
}
