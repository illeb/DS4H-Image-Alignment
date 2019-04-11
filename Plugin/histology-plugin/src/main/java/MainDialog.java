import ij.gui.Roi;
import ij.io.OpenDialog;

import javax.swing.*;
import java.awt.*;

public class MainDialog extends JDialog {
    private JPanel contentPane;
    private OnDialogEvcentListener eventListener;

    enum GUIEvents {
        PREVIOUS,
        NEXT,
        RESET,
        DELETE
    }

    private JButton btn_delete;
    private JButton btn_resetMarkers;
    private JButton btn_prevImage;
    private JButton btn_nextImage;
    private JList<Roi> list1;

    public MainDialog(OnDialogEvcentListener listener) {
        super(null, java.awt.Dialog.ModalityType.TOOLKIT_MODAL);
        setContentPane(contentPane);
        setTitle("Histology plugin");
        setModal(true);
        setResizable(false);
        getRootPane().setDefaultButton(btn_prevImage);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.MODELESS);

        this.eventListener = listener;

        btn_delete.addActionListener(e -> this.eventListener.onEvent(GUIEvents.DELETE));
        btn_resetMarkers.addActionListener(e -> this.eventListener.onEvent(GUIEvents.RESET));
        btn_prevImage.addActionListener(e -> this.eventListener.onEvent(GUIEvents.PREVIOUS));
        btn_nextImage.addActionListener(e -> this.eventListener.onEvent(GUIEvents.NEXT));
        list1.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> new JLabel("roi n." + (index + 1)));

        pack();
        setPreferredSize(new Dimension(this.getWidth(), 300));
        setMinimumSize(new Dimension(this.getWidth(), 300));
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

    BufferedImage image;
    public void setImage(BufferedImage image) {
        this.image = image;

        DefaultListModel<Roi> model = new DefaultListModel<>();
        int idx = 0;
        for (Roi roi : image.getManager().getRoisAsArray())
            model.add(idx++, roi);

        image.getManager().runCommand("Show All");
        list1.setModel(model);
    }
}

