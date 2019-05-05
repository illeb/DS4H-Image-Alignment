package histology.maindialog;

import histology.BufferedImagesManager;
import histology.maindialog.event.*;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.plugin.Zoom;
import ij.plugin.frame.RoiManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;

public class MainDialog extends ImageWindow {
    private final OnMainDialogEventListener eventListener;

    /** constraints for annotation panel */
    private GridBagConstraints annotationsConstraints = new GridBagConstraints();

    private JPanel buttonsPanel = new JPanel();

    private JPanel trainingJPanel = new JPanel();
    private JPanel actionsJPanel = new JPanel();

    private Panel all = new Panel();

    private JCheckBox chk_showPreview;
    private JButton btn_deleteRoi;
    private JButton btn_prevImage;
    private JButton btn_nextImage;
    private JButton btn_mergeImage;

    private JList<String> lst_rois;
    DefaultListModel<String> lst_rois_model;

    private BufferedImagesManager.BufferedImage image;

    private boolean mouseOverCanvas;

    public MainDialog(ImagePlus plus, OnMainDialogEventListener listener) {
        super(plus, new CustomCanvas(plus));

        final CustomCanvas canvas = (CustomCanvas) getCanvas();

        chk_showPreview = new JCheckBox("Show preview window");
        chk_showPreview.setToolTipText("Show a preview window");

        btn_deleteRoi = new JButton ("DELETE");
        btn_deleteRoi.setToolTipText("Delete current ROI selected");
        btn_deleteRoi.setEnabled(false);

        btn_prevImage = new JButton ("PREV IMAGE");
        btn_prevImage.setToolTipText("Select previous image in the stack");

        btn_nextImage = new JButton ("NEXT IMAGE");
        btn_nextImage.setToolTipText("Select next image in the stack");

        btn_mergeImage = new JButton ("MERGE");
        btn_mergeImage.setToolTipText("Merge the images based on the added landmarks");


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
        lst_rois.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        trainingJPanel.setLayout(trainingLayout);

        // Options panel
        actionsJPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        GridBagLayout actionsLayout = new GridBagLayout();
        GridBagConstraints actionsConstraints = new GridBagConstraints();
        actionsConstraints.anchor = GridBagConstraints.NORTHWEST;
        actionsConstraints.fill = GridBagConstraints.HORIZONTAL;
        actionsConstraints.gridwidth = 1;
        actionsConstraints.gridheight = 1;
        actionsConstraints.gridx = 0;
        actionsConstraints.gridy = 0;
        actionsConstraints.insets = new Insets(5, 5, 6, 6);
        actionsJPanel.setLayout(actionsLayout);

        actionsJPanel.add(chk_showPreview, actionsConstraints);
        actionsConstraints.gridy++;
        actionsJPanel.add(btn_deleteRoi, actionsConstraints);
        actionsConstraints.gridy++;
        actionsJPanel.add(btn_prevImage, actionsConstraints);
        actionsConstraints.gridy++;
        actionsJPanel.add(btn_nextImage, actionsConstraints);
        actionsConstraints.gridy++;
        actionsJPanel.add(btn_mergeImage, actionsConstraints);
        actionsConstraints.gridy++;

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
        buttonsPanel.add(actionsJPanel, buttonsConstraints);
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

        chk_showPreview.addItemListener(e -> this.eventListener.onMainDialogEvent(new PreviewImageEvent(chk_showPreview.isSelected())));
        btn_deleteRoi.addActionListener(e -> {
            int index = lst_rois.getSelectedIndex();
            this.eventListener.onMainDialogEvent(new DeleteEvent(lst_rois.getSelectedIndex()));
            lst_rois.setSelectedIndex(index);
        });
        btn_prevImage.addActionListener(e -> this.eventListener.onMainDialogEvent(new ChangeImageEvent(ChangeImageEvent.ChangeDirection.PREV)));
        btn_nextImage.addActionListener(e -> this.eventListener.onMainDialogEvent(new ChangeImageEvent(ChangeImageEvent.ChangeDirection.NEXT)));
        btn_mergeImage.addActionListener(e -> this.eventListener.onMainDialogEvent(new MergeEvent()));

        // Markers addition handlers
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new KeyboardEventDispatcher());
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseOverCanvas = true;
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseOverCanvas = false;
                super.mouseExited(e);
            }
        });

        // Rois list handling
        lst_rois.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lst_rois_model = new DefaultListModel<>();
        lst_rois.setModel(lst_rois_model);
        lst_rois.setSelectionModel(new DefaultListSelectionModel() {
            boolean gestureStarted = false;
            @Override
            public void setSelectionInterval(int index0, int index1) {
                if(!gestureStarted) {
                    if (isSelectedIndex(index0)) {
                        super.removeSelectionInterval(index0, index0);
                        eventListener.onMainDialogEvent(new DeselectedRoiEvent(index0));
                        btn_deleteRoi.setEnabled(false);
                        return;
                    }
                    else{
                        eventListener.onMainDialogEvent(new SelectedRoiEvent(index0));
                        btn_deleteRoi.setEnabled(true);
                        super.setSelectionInterval(index0, index1);
                    }
                }
                gestureStarted = true;
            }

            @Override
            public void setValueIsAdjusting(boolean isAdjusting) {
                if (isAdjusting == false) {
                    gestureStarted = false;
                }
            }
        });

        pack();
    }

     /**
     * Change the actual image displayed in the main view, based on the given BufferedImage istance
     * @param image
     */
    public void changeImage(BufferedImagesManager.BufferedImage image) {
        this.setImage(image);
        image.backupRois();
        image.getManager().reset();
        this.image = image;
        if(lst_rois.getSelectedIndex() == -1)
            btn_deleteRoi.setEnabled(false);
        this.image.restoreRois();
        this.updateRoiList(image.getManager());
        // Let's call the zoom plugin to scale the image to fit in the user window
        new Zoom().run("scale");
        this.pack();
    }

    /**
     * Update the Roi List based on the given RoiManager istance
     * @param manager
     */
    public void updateRoiList(RoiManager manager) {
        lst_rois_model.removeAllElements();
        int idx = 0;
        for (Roi roi : manager.getRoisAsArray())
            lst_rois_model.add(idx++, MessageFormat.format("{0} - {1},{2}", idx, (int)roi.getXBase(), (int)roi.getYBase()));
        manager.runCommand("Show All");
        manager.runCommand("show all with labels");
        if(lst_rois.getSelectedIndex() == -1)
            btn_deleteRoi.setEnabled(false);
    }

    public void setPreviewWindowCheckBox(boolean value) {
        this.chk_showPreview.setSelected(value);
    }

    public void setNextImageButtonEnabled(boolean enabled) {
        this.btn_nextImage.setEnabled(enabled);
    }

    public void setPrevImageButtonEnabled(boolean enabled) {
        this.btn_prevImage.setEnabled(enabled);
    }

    private class KeyboardEventDispatcher implements KeyEventDispatcher {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_C && mouseOverCanvas) {
                Point clickCoords = getCanvas().getCursorLoc();
                eventListener.onMainDialogEvent(new AddRoiEvent(clickCoords));
            }
            return false;
        }
    }
}
