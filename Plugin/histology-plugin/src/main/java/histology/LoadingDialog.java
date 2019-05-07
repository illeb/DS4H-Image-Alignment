package histology;

import javax.swing.*;
import java.awt.*;

public class LoadingDialog extends JDialog {
    public LoadingDialog() {
        super();
        ImageIcon loading = new ImageIcon("src/main/assets/spinner.gif");
        this.add(new JLabel("", loading, JLabel.CENTER));

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setModalityType(ModalityType.APPLICATION_MODAL);
        this.setSize(200, 200);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
    }
}
