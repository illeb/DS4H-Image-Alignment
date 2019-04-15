package histology;

import histology.maindialog.CustomCanvas;
import histology.maindialog.OnDialogEventListener;
import histology.maindialog.event.AddRoiEvent;
import histology.maindialog.event.ChangeImageEvent;
import histology.maindialog.event.DeleteEvent;
import histology.maindialog.event.SelectedRoiEvent;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.io.OpenDialog;
import ij.plugin.frame.RoiManager;
import loci.plugins.BF;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.Arrays;

public class MainDialog extends ImageWindow {
    private final OnDialogEventListener eventListener;


    /** constraints for annotation panel */
    private GridBagConstraints annotationsConstraints = new GridBagConstraints();
    /** Panel with class radio buttons and lists */

    private JPanel buttonsPanel = new JPanel();

    private JPanel trainingJPanel = new JPanel();
    private JPanel optionsJPanel = new JPanel();

    private Panel all = new Panel();

    /** save data button */
    private JButton btn_deleteRoi;
    /** settings button */
    private JButton btn_prevImage;
    /** create new class button */
    private JButton btn_nextImage;

    private JList<String> lst_rois;

    private BufferedImage image;

    public MainDialog(ImagePlus plus, OnDialogEventListener listener) {
        super(plus, new CustomCanvas(plus));

        final CustomCanvas canvas = (CustomCanvas) getCanvas();


        btn_deleteRoi = new JButton ("DELETE");
        btn_deleteRoi.setToolTipText("Delete current ROI selected");
        btn_deleteRoi.setEnabled(false);

        btn_prevImage = new JButton ("PREV IMAGE");
        btn_prevImage.setToolTipText("Select previous image in the stack");

        btn_nextImage = new JButton ("NEXT IMAGE");
        btn_nextImage.setToolTipText("Select next image in the stack");


        // Remove the canvas from the window, to add it later
        removeAll();

        setTitle("Histology Plugin");

        // Annotations panel
        annotationsConstraints.anchor = GridBagConstraints.NORTHWEST;
        annotationsConstraints.gridwidth = 1;
        annotationsConstraints.gridheight = 1;
        annotationsConstraints.gridx = 0;
        annotationsConstraints.gridy = 0;

        // Training panel (left side of the GUI)
        trainingJPanel.setBorder(BorderFactory.createTitledBorder("Rois"));
        GridBagLayout trainingLayout = new GridBagLayout();
        GridBagConstraints trainingConstraints = new GridBagConstraints();
        trainingConstraints.anchor = GridBagConstraints.NORTHWEST;
        trainingConstraints.fill = GridBagConstraints.HORIZONTAL;
        trainingConstraints.gridwidth = 1;
        trainingConstraints.gridheight = 1;
        trainingConstraints.gridx = 0;
        trainingConstraints.gridy = 0;
        trainingJPanel.setLayout(trainingLayout);

        lst_rois = new JList<>();
        trainingJPanel.add(lst_rois, trainingConstraints);
        lst_rois.setPreferredSize(new Dimension(200, 200));
        lst_rois.setBackground(Color.white);
        trainingJPanel.setLayout(trainingLayout);

        // Options panel
        optionsJPanel.setBorder(BorderFactory.createTitledBorder("Options"));
        GridBagLayout optionsLayout = new GridBagLayout();
        GridBagConstraints optionsConstraints = new GridBagConstraints();
        optionsConstraints.anchor = GridBagConstraints.NORTHWEST;
        optionsConstraints.fill = GridBagConstraints.HORIZONTAL;
        optionsConstraints.gridwidth = 1;
        optionsConstraints.gridheight = 1;
        optionsConstraints.gridx = 0;
        optionsConstraints.gridy = 0;
        optionsConstraints.insets = new Insets(5, 5, 6, 6);
        optionsJPanel.setLayout(optionsLayout);

        optionsJPanel.add(btn_deleteRoi, optionsConstraints);
        optionsConstraints.gridy++;
        optionsJPanel.add(btn_prevImage, optionsConstraints);
        optionsConstraints.gridy++;
        optionsJPanel.add(btn_nextImage, optionsConstraints);
        optionsConstraints.gridy++;

        // Buttons panel (including training and options)
        GridBagLayout buttonsLayout = new GridBagLayout();
        GridBagConstraints buttonsConstraints = new GridBagConstraints();
        buttonsPanel.setLayout(buttonsLayout);
        buttonsConstraints.anchor = GridBagConstraints.NORTHWEST;
        buttonsConstraints.fill = GridBagConstraints.HORIZONTAL;
        buttonsConstraints.gridwidth = 1;
        buttonsConstraints.gridheight = 1;
        buttonsConstraints.gridx = 0;
        buttonsConstraints.gridy = 0;
        buttonsPanel.add(trainingJPanel, buttonsConstraints);
        buttonsConstraints.gridy++;
        buttonsPanel.add(optionsJPanel, buttonsConstraints);
        buttonsConstraints.gridy++;
        buttonsConstraints.insets = new Insets(5, 5, 6, 6);

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints allConstraints = new GridBagConstraints();
        all.setLayout(layout);

        allConstraints.anchor = GridBagConstraints.NORTHWEST;
        allConstraints.fill = GridBagConstraints.BOTH;
        allConstraints.gridwidth = 1;
        allConstraints.gridheight = 1;
        allConstraints.gridx = 0;
        allConstraints.gridy = 0;
        allConstraints.weightx = 0;
        allConstraints.weighty = 0;

        all.add(buttonsPanel, allConstraints);

        allConstraints.gridx++;
        allConstraints.weightx = 1;
        allConstraints.weighty = 1;
        all.add(canvas, allConstraints);

        GridBagLayout wingb = new GridBagLayout();
        GridBagConstraints winc = new GridBagConstraints();
        winc.anchor = GridBagConstraints.NORTHWEST;
        winc.fill = GridBagConstraints.BOTH;
        winc.weightx = 1;
        winc.weighty = 1;
        setLayout(wingb);
        add(all, winc);

        // Propagate all listeners
        for (Component p : new Component[]{all, buttonsPanel}) {
            for (KeyListener kl : getKeyListeners()) {
                p.addKeyListener(kl);
            }
        }

        this.eventListener = listener;

        btn_deleteRoi.addActionListener(e -> {
            int index = lst_rois.getSelectedIndex();
            this.eventListener.onEvent(new DeleteEvent(lst_rois.getSelectedIndex()));
            lst_rois.setSelectedIndex(index);
        });
        btn_prevImage.addActionListener(e -> this.eventListener.onEvent(new ChangeImageEvent(ChangeImageEvent.ChangeDirection.PREV)));
        btn_nextImage.addActionListener(e -> this.eventListener.onEvent(new ChangeImageEvent(ChangeImageEvent.ChangeDirection.NEXT)));
        lst_rois.addListSelectionListener(e -> {
            int index = lst_rois.getSelectedIndex();
            // Exclude invalid selections of the list (by misclick or from a deletion)
            if (index == -1)
                return;
            this.eventListener.onEvent(new SelectedRoiEvent(index));
            btn_deleteRoi.setEnabled(true);
        });
        pack();

        setCanvasListener();
    }

    public void setNextImageButtonEnabled(boolean enabled) {
        this.btn_nextImage.setEnabled(enabled);
    }

    public void setPrevImageButtonEnabled(boolean enabled) {
        this.btn_prevImage.setEnabled(enabled);
    }

    public void changeImage(BufferedImage image) {
        this.setImage(image);
        image.backupRois();
        image.getManager().reset();
        this.image = image;
        if(lst_rois.getSelectedIndex() == -1)
            btn_deleteRoi.setEnabled(false);
        this.image.restoreRois();
        this.updateRoiList(image.getManager());
    }

    private void setCanvasListener() {
        final CustomCanvas canvas = (CustomCanvas) getCanvas();
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // rendiamo il button di "quick annotaion" pari al click destro del mouse
                if(!SwingUtilities.isRightMouseButton(e))
                    return;
                Point clickCoords = canvas.getCursorLoc();
                eventListener.onEvent(new AddRoiEvent(clickCoords));
            }
        });
    }

    public void updateRoiList(RoiManager manager) {
        DefaultListModel<String> model = new DefaultListModel<>();
        int idx = 0;
        for (Roi roi : manager.getRoisAsArray())
            model.add(idx++, MessageFormat.format("{0} - {1},{2}", idx, (int)roi.getXBase(), (int)roi.getYBase()));
        manager.runCommand("Show All");
        manager.runCommand("show all with labels");
        lst_rois.setModel(model);

        if(lst_rois.getSelectedIndex() == -1)
            btn_deleteRoi.setEnabled(false);
    }

    public static String PromptForFile() {
        OpenDialog od = new OpenDialog("Selezionare un'immagine");
        String dir = od.getDirectory();
        String name = od.getFileName();
        return (dir + name);
    }
}
