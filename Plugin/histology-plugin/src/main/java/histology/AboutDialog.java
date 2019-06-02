package histology;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URISyntaxException;

public class AboutDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JLabel lbl_icon;
    private JPanel pnl_title;
    private JLabel lbl_credits;
    private JLabel lbl_version;
    private JLabel lbl_supervisors;
    private JLabel lbl_supervisor1;
    private JLabel lbl_supervisor2;
    private JLabel lbl_author1;
    private JPanel pnl_credits;
    private JPanel pnl_heads;
    private JPanel pnl_authors;

    public AboutDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        this.setMinimumSize(new Dimension(470,340));
        this.setMaximumSize(new Dimension(470,340));
        this.setResizable(false);
        this.setTitle("About...");

        this.lbl_author1.setCursor(new Cursor(Cursor.HAND_CURSOR));
        this.lbl_author1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                try {
                    Desktop.getDesktop().mail(new java.net.URI("mailto:stefano.belli4@studio.unibo.it"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        this.lbl_supervisor1.setCursor(new Cursor(Cursor.HAND_CURSOR));
        this.lbl_supervisor1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                try {
                    Desktop.getDesktop().mail(new java.net.URI("mailto:antonella.carbonaro@unibo.it"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        this.lbl_supervisor2.setCursor(new Cursor(Cursor.HAND_CURSOR));
        this.lbl_supervisor2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                try {
                    Desktop.getDesktop().mail(new java.net.URI("mailto:f.piccinini@unibo.it"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if(visible)
            this.pack();
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        AboutDialog dialog = new AboutDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private void createUIComponents() {
        lbl_icon = new JLabel();

        // Thanks to https://stackoverflow.com/a/18335435/1306679
        ImageIcon imageIcon = new ImageIcon("src/main/assets/info.png"); // load the image to a imageIcon
        Image image = imageIcon.getImage(); // transform it
        Image newimg = image.getScaledInstance(40, 40,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
        imageIcon = new ImageIcon(newimg);  // transform it back

        lbl_icon.setIcon(imageIcon);

        pnl_title = new JPanel();
        pnl_title.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        lbl_version = new JLabel();
        lbl_version.setText("Histology plugin 18.05");

        lbl_credits = new JLabel();
        lbl_credits.setText("<html><body>Made by: Stefano Belli<br>With the supervision of: Prof. Antonella Carbonaro && Prof. Alberto Piccinini</body></html>");

        pnl_credits = new JPanel();
        pnl_credits.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
    }
}
