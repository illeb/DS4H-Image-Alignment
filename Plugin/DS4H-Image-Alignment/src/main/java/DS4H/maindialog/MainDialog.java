package DS4H.maindialog;

import DS4H.BufferedImage;
import DS4H.maindialog.event.*;
import ij.IJ;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.plugin.Zoom;
import ij.plugin.frame.RoiManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;

public class MainDialog extends ImageWindow {
    private final OnMainDialogEventListener eventListener;

    MenuBar menuBar;
    private JPanel buttonsPanel = new JPanel();

    private JPanel cornersJPanel = new JPanel();
    private JList<String> lst_rois;
    private JButton btn_copyCorners;

    private JPanel actionsJPanel = new JPanel();
    private JCheckBox chk_showPreview;
    private JButton btn_deleteRoi;
    private JButton btn_prevImage;
    private JButton btn_nextImage;


    private JPanel alignJPanel = new JPanel();
    private JButton btn_alignImages;
    private JCheckBox chk_rotateImages;
    private JCheckBox chk_keepOriginal;

    private Panel all = new Panel();

    private DefaultListModel<String> lst_rois_model;
    private BufferedImage image;

    private boolean mouseOverCanvas;
    private static String DIALOG_STATIC_TITLE = "DS4H Image Alignment.";

    private Rectangle oldRect = null;
    public MainDialog(BufferedImage plus, OnMainDialogEventListener listener) {
        super(plus, new CustomCanvas(plus));
        this.image = plus;

        final CustomCanvas canvas = (CustomCanvas) getCanvas();

        chk_showPreview = new JCheckBox("Show preview window");
        chk_showPreview.setToolTipText("Show a preview window");

        btn_deleteRoi = new JButton ("DELETE CORNER");
        btn_deleteRoi.setToolTipText("Delete current corner point selected");
        btn_deleteRoi.setEnabled(false);

        btn_prevImage = new JButton ("PREV IMAGE");
        btn_prevImage.setToolTipText("Select previous image in the stack");

        btn_nextImage = new JButton ("NEXT IMAGE");
        btn_nextImage.setToolTipText("Select next image in the stack");

        btn_alignImages = new JButton ("ALIGN IMAGES");
        btn_alignImages.setToolTipText("Align the images based on the added corner points");
        btn_alignImages.setEnabled(false);

        chk_rotateImages = new JCheckBox("Apply image rotation");
        chk_rotateImages.setToolTipText("Apply rotation algorithms for improved images alignment.");
        chk_rotateImages.setSelected(true);
        chk_rotateImages.setEnabled(false);

        chk_keepOriginal = new JCheckBox("Keep original images");
        chk_keepOriginal.setToolTipText("Keep the original images boundaries, applying stitching where necessary. NOTE: this operation is resource-intensive.");
        chk_keepOriginal.setSelected(false);
        chk_keepOriginal.setEnabled(false);

        // Remove the canvas from the windlow, to add it later
        removeAll();

        setTitle(DIALOG_STATIC_TITLE);

        // Training panel (left side of the GUI)
        cornersJPanel.setBorder(BorderFactory.createTitledBorder("Corners"));
        GridBagLayout trainingLayout = new GridBagLayout();
        GridBagConstraints trainingConstraints = new GridBagConstraints();
        trainingConstraints.anchor = GridBagConstraints.NORTHWEST;
        trainingConstraints.fill = GridBagConstraints.HORIZONTAL;
        trainingConstraints.gridwidth = 1;
        trainingConstraints.gridheight = 1;
        trainingConstraints.gridx = 0;
        trainingConstraints.gridy = 0;
        cornersJPanel.setLayout(trainingLayout);

        cornersJPanel.add(new JLabel("Press \"C\" to add a corner point"), trainingConstraints);
        trainingConstraints.gridy++;
        trainingConstraints.gridy++;
        lst_rois = new JList<>();
        JScrollPane scrollPane = new JScrollPane(lst_rois);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(180, 180));
        scrollPane.setMinimumSize(new Dimension(180, 180));
        scrollPane.setMaximumSize(new Dimension(180, 180));
        cornersJPanel.add(scrollPane, trainingConstraints);
        trainingConstraints.insets = new Insets(5, 0, 10, 0);
        trainingConstraints.gridy++;
        lst_rois.setBackground(Color.white);
        lst_rois.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        btn_copyCorners = new JButton();
        btn_copyCorners.setText("COPY CORNERS");
        btn_copyCorners.setEnabled(false);
        cornersJPanel.add(btn_copyCorners, trainingConstraints);
        cornersJPanel.setLayout(trainingLayout);

        // Options panel
        actionsJPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        GridBagLayout actionsLayout = new GridBagLayout();
        GridBagConstraints actionsConstraints = new GridBagConstraints();
        actionsConstraints.anchor = GridBagConstraints.NORTHWEST;
        actionsConstraints.fill = GridBagConstraints.HORIZONTAL;
        actionsConstraints.weightx = 1;
        actionsConstraints.gridx = 0;
        actionsConstraints.insets = new Insets(5, 5, 6, 6);
        actionsJPanel.setLayout(actionsLayout);

        actionsJPanel.add(chk_showPreview, actionsConstraints);
        actionsJPanel.add(btn_deleteRoi, actionsConstraints);
        actionsJPanel.add(btn_prevImage, actionsConstraints);
        actionsJPanel.add(btn_nextImage, actionsConstraints);
        actionsJPanel.setLayout(actionsLayout);


        // Options panel
        alignJPanel.setBorder(BorderFactory.createTitledBorder("Alignment"));
        GridBagLayout alignLayout = new GridBagLayout();
        GridBagConstraints alignConstraints = new GridBagConstraints();
        alignConstraints.anchor = GridBagConstraints.NORTHWEST;
        alignConstraints.fill = GridBagConstraints.HORIZONTAL;
        alignConstraints.weightx = 1;
        alignConstraints.gridx = 0;
        alignConstraints.insets = new Insets(5, 5, 6, 6);
        alignJPanel.setLayout(alignLayout);

        alignJPanel.add(chk_rotateImages, actionsConstraints);
        alignJPanel.add(chk_keepOriginal, actionsConstraints);
        alignJPanel.add(btn_alignImages, actionsConstraints);
        alignJPanel.setLayout(alignLayout);

        // Buttons panel
        GridBagLayout buttonsLayout = new GridBagLayout();
        GridBagConstraints buttonsConstraints = new GridBagConstraints();
        buttonsPanel.setLayout(buttonsLayout);
        buttonsConstraints.anchor = GridBagConstraints.NORTHWEST;
        buttonsConstraints.fill = GridBagConstraints.HORIZONTAL;
        buttonsConstraints.gridwidth = 1;
        buttonsConstraints.gridheight = 1;
        buttonsConstraints.gridx = 0;
        buttonsConstraints.gridy = 0;
        buttonsPanel.add(cornersJPanel, buttonsConstraints);
        buttonsConstraints.gridy++;
        buttonsPanel.add(actionsJPanel, buttonsConstraints);
        buttonsConstraints.gridy++;
        buttonsPanel.add(alignJPanel, buttonsConstraints);
        buttonsConstraints.gridy++;
        buttonsConstraints.insets = new Insets(5, 5, 6, 6);

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints allConstraints = new GridBagConstraints();
        all.setLayout(layout);
        // sets a little bit of padding to ensure that the imageplus text is shown and not covered by the panel
        allConstraints.insets = new Insets(5, 0, 0, 0);

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
        winc.insets = new Insets(5, 0, 0, 0);
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

        btn_copyCorners.addActionListener(e-> {
             this.eventListener.onMainDialogEvent(new CopyCornersEvent());
        });
        chk_showPreview.addItemListener(e -> this.eventListener.onMainDialogEvent(new PreviewImageEvent(chk_showPreview.isSelected())));
        btn_deleteRoi.addActionListener(e -> {
            int index = lst_rois.getSelectedIndex();
            this.eventListener.onMainDialogEvent(new DeleteRoiEvent(lst_rois.getSelectedIndex()));
            lst_rois.setSelectedIndex(index);
        });
        btn_prevImage.addActionListener(e -> this.eventListener.onMainDialogEvent(new ChangeImageEvent(ChangeImageEvent.ChangeDirection.PREV)));
        btn_nextImage.addActionListener(e -> this.eventListener.onMainDialogEvent(new ChangeImageEvent(ChangeImageEvent.ChangeDirection.NEXT)));
        btn_alignImages.addActionListener(e -> this.eventListener.onMainDialogEvent(new AlignEvent(chk_rotateImages.isSelected(), chk_keepOriginal.isSelected())));

        // Markers addition handlers
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new KeyboardEventDispatcher());
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);

                // Check if the Rois in the image have changed position by user input; if so, update the list and notify the controller
                Rectangle bounds = getImagePlus().getRoi().getBounds();
                if (!bounds.equals(oldRect)) {
                    oldRect = (Rectangle)bounds.clone();
                    eventListener.onMainDialogEvent(new MovedRoiEvent());
                }
            }
        });
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
            // Thanks to https://stackoverflow.com/a/31336378/1306679
            @Override
            public void setSelectionInterval(int startIndex, int endIndex) {
                if (startIndex == endIndex) {
                    if (multipleItemsCurrentlyAreSelected()) {
                        clearSelection();
                    }
                    if (isSelectedIndex(startIndex)) {
                        clearSelection();
                        eventListener.onMainDialogEvent(new DeselectedRoiEvent(startIndex));
                        btn_deleteRoi.setEnabled(false);
                    }
                    else {
                        eventListener.onMainDialogEvent(new SelectedRoiEvent(startIndex));
                        btn_deleteRoi.setEnabled(true);
                        super.setSelectionInterval(startIndex, endIndex);
                    }
                }
            }

            private boolean multipleItemsCurrentlyAreSelected() {
                return getMinSelectionIndex() != getMaxSelectionIndex();
            }

        });
        menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem menuItem = new MenuItem("Open file...");
        menuItem.addActionListener(e -> eventListener.onMainDialogEvent(new OpenFileEvent()));
        fileMenu.add(menuItem);

        menuItem = new MenuItem("Add image to current stack");
        menuItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(image.getFilePath()));
            chooser.setMultiSelectionEnabled(true);
            chooser.showOpenDialog(null);

            File[] files = chooser.getSelectedFiles();
            Arrays.stream(files).forEach(file -> eventListener.onMainDialogEvent(new AddFileEvent(file.getPath())));
        });
        fileMenu.add(menuItem);
        menuItem = new MenuItem("Remove image...");
        menuItem.addActionListener(e -> eventListener.onMainDialogEvent(new RemoveImageEvent()));
        // fileMenu.add(menuItem);
        fileMenu.addSeparator();
        menuItem = new MenuItem("Exit");
        menuItem.addActionListener(e -> eventListener.onMainDialogEvent(new ExitEvent()));
        fileMenu.add(menuItem);

        Menu aboutMenu = new Menu("?");
        menuItem = new MenuItem("About...");
        menuItem.addActionListener(e -> eventListener.onMainDialogEvent(new OpenAboutEvent()));
        aboutMenu.add(menuItem);

        menuBar.add(fileMenu);
        menuBar.add(aboutMenu);
        this.setMenuBar(menuBar);
        new Zoom().run("scale");
        pack();
    }

     /**
     * Change the actual image displayed in the main view, based on the given BufferedImage istance
     * @param image
     */
    public void changeImage(BufferedImage image) {
        this.setImage(image);
        image.backupRois();
        image.getManager().reset();
        this.image = image;
        if(lst_rois.getSelectedIndex() == -1)
            btn_deleteRoi.setEnabled(false);
        this.image.restoreRois();
        this.drawRois(image.getManager());

        // Let's call the zoom plugin to scale the image to fit in the user window
        // The zoom scaling command works on the current active window: to be 100% sure it will work, we need to forcefully select the preview window.
        IJ.selectWindow(this.getImagePlus().getID());
        new Zoom().run("scale");
        this.pack();
    }

    /**
     * Update the Roi List based on the given RoiManager istance
     * @param manager
     */
    public void drawRois(RoiManager manager) {
        manager.runCommand("show all with labels");
        this.refreshROIList(manager);
        if(lst_rois.getSelectedIndex() == -1)
            btn_deleteRoi.setEnabled(false);
    }

    public void refreshROIList(RoiManager manager) {
        lst_rois_model.removeAllElements();
        int idx = 0;
        for (Roi roi : manager.getRoisAsArray())
            lst_rois_model.add(idx++, MessageFormat.format("{0} - {1},{2}", idx, (int)roi.getXBase() + (int)(roi.getFloatWidth() / 2), (int)roi.getYBase() + (int)(roi.getFloatHeight() / 2)));
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

    public void setAlignButtonEnabled(boolean enabled) {
        this.btn_alignImages.setEnabled(enabled);
        this.chk_rotateImages.setEnabled(enabled);
        this.chk_keepOriginal.setEnabled(enabled);
    }

    public void setCopyCornersEnabled(boolean enabled) {
        this.btn_copyCorners.setEnabled(enabled);
    }

    private class KeyboardEventDispatcher implements KeyEventDispatcher {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_RELEASED && e.getKeyCode() == KeyEvent.VK_C && mouseOverCanvas) {
                Point clickCoords = getCanvas().getCursorLoc();
                eventListener.onMainDialogEvent(new AddRoiEvent(clickCoords));
            }
            return false;
        }
    }

    @Override
    public void setTitle(String title){
        super.setTitle(DIALOG_STATIC_TITLE + " " + title);
    }
}
