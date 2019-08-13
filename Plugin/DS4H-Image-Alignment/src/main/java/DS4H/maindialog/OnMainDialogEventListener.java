package DS4H.maindialog;

import DS4H.maindialog.event.IMainDialogEvent;

public interface OnMainDialogEventListener {
    Thread onMainDialogEvent(IMainDialogEvent event);
}

