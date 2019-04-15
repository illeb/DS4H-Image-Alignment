package histology.maindialog.event;

import java.awt.*;

public class AddRoiEvent implements IDialogEvent {
    private Point coords;
    public AddRoiEvent(Point coords) {
        this.coords = coords;
    }

    public Point getClickCoords() {
        return this.coords;
    }
}
