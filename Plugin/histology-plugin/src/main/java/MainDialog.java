import ij.io.OpenDialog;
import rx.subjects.PublishSubject;

import javax.swing.*;
import java.awt.*;

public class MainDialog extends JDialog {
    private JPanel contentPane;

    // IRRELEVANT in respect of official wiki guidelines https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#nulls
    enum GUIEvents {
        PREVIOUS,
        NEXT,
        IRRELEVANT
    }

    private JButton btn_addmarker;
    private JButton btn_resetMarkers;
    private JButton btn_prevImage;
    private JButton btn_nextImage;
    public PublishSubject<GUIEvents> AddMarkerEvent$ = PublishSubject.create();
    public PublishSubject<GUIEvents> ResetMarkerEvent$ = PublishSubject.create();
    public PublishSubject<GUIEvents> PrevImageEvent$ = PublishSubject.create();
    public PublishSubject<GUIEvents> NextImageEvent$ = PublishSubject.create();

    public MainDialog() {
        setContentPane(contentPane);
        setTitle("Histology plugin");
        setModal(true);
        setResizable(false);
        setPreferredSize(new Dimension(250, 300));
        setMinimumSize(new Dimension(250, 300));
        getRootPane().setDefaultButton(btn_prevImage);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.MODELESS);

        btn_addmarker.addActionListener(e -> AddMarkerEvent$.onNext(GUIEvents.IRRELEVANT));
        btn_resetMarkers.addActionListener(e -> ResetMarkerEvent$.onNext(GUIEvents.IRRELEVANT));
        btn_prevImage.addActionListener(e -> PrevImageEvent$.onNext(GUIEvents.PREVIOUS));
        btn_nextImage.addActionListener(e -> NextImageEvent$.onNext(GUIEvents.NEXT));

        pack();
        setVisible(true);
    }

    public void setNextImageButtonEnabled(boolean enabled) {
        this.btn_nextImage.setEnabled(enabled);
    }

    public void setPrevImageButtonEnabled(boolean enabled) {
        this.btn_prevImage.setEnabled(enabled);
    }

    public String PromptForFile() {
        OpenDialog od = new OpenDialog("Selezionare un'immagine");
        String dir = od.getDirectory();
        String name = od.getFileName();
        return (dir + name);
    }
}
