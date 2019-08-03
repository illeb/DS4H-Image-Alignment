package DS4H.removedialog;

import DS4H.ImageFile;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.MessageFormat;

public class ImageFileRenderer extends JPanel implements ListCellRenderer<ImageFile> {

    private JLabel lbl_caption = new JLabel();
    private JPanel pnl_images = new JPanel();
    private JPanel panelText;
    private boolean drawed = false;

    public ImageFileRenderer() {
        setLayout(new BorderLayout(5, 5));
        panelText = new JPanel(new GridLayout(0, 1));
        panelText.add(lbl_caption);
        add(pnl_images, BorderLayout.CENTER);
        add(panelText, BorderLayout.NORTH);

        /*Border border = getBorder();
        Border margin = new EmptyBorder(10,10,10,10);
        setBorder(new CompoundBorder(border, margin));*/
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ImageFile> list, ImageFile value, int index, boolean isSelected, boolean cellHasFocus) {

        lbl_caption.setText(MessageFormat.format("File {0}, {1} Images", index + 1, value.getPathFile()));

        if(!drawed) {
            value.getThumbs().forEach(thumb -> {
                JLabel lbl = new JLabel();
                Border border = lbl.getBorder();
                Border margin = new EmptyBorder(10,10,10,10);
                lbl.setBorder(new CompoundBorder(border, margin));
                lbl.setIcon(new ImageIcon(thumb));
                pnl_images.add(lbl);
            });
            drawed = true;
        }

        // when select item
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            lbl_caption.setBackground(list.getSelectionBackground());
            panelText.setBackground(list.getSelectionBackground());
            pnl_images.setBackground(list.getSelectionBackground());
        } else { // when don't select
            setBackground(list.getBackground());
            lbl_caption.setBackground(list.getBackground());
            panelText.setBackground(list.getBackground());
            pnl_images.setBackground(list.getBackground());
        }
        return this;
    }
}
