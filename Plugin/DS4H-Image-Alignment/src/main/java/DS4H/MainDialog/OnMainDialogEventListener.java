package DS4H.MainDialog;

import DS4H.MainDialog.event.IMainDialogEvent;

public interface OnMainDialogEventListener {
    Thread onMainDialogEvent(IMainDialogEvent event);
}

