package DS4H.MainDialog;

import DS4H.BufferedImage.BufferedImage;
import DS4H.BufferedImage.OnBufferedImageEventListener;
import DS4H.BufferedImage.event.IBufferedImageEvent;
import DS4H.BufferedImage.event.RoiSelectedEvent;
import DS4H.MainDialog.event.*;
import DS4H.Utilities;
import ij.IJ;
import ij.Prefs;
import ij.gui.ImageWindow;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
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
    public JList<String> lst_rois;
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
    public static BufferedImage currentImage = null;


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

        chk_keepOriginal = new JCheckBox("Keep all pixel data");
        chk_keepOriginal.setToolTipText("Keep the original images boundaries, applying stitching where necessary. NOTE: this operation is resource-intensive.");
        chk_keepOriginal.setSelected(true);
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


        JLabel lbl = new JLabel("Press \"C\" to add a corner point");
        lbl.setForeground(Color.gray);
        cornersJPanel.add(lbl, trainingConstraints);
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

        lbl = new JLabel("Press \"A\" or \"D\" to change image", JLabel.LEFT);
        lbl.setForeground(Color.gray);
        actionsJPanel.add(lbl, actionsConstraints);
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
        buttonsPanel.setBackground(Color.GRAY);
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.add(cornersJPanel);
        buttonsPanel.add(actionsJPanel);
        buttonsPanel.add(alignJPanel);

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints allConstraints = new GridBagConstraints();
        all.setLayout(layout);
        // sets a little bit of padding to ensure that the imageplus text is shown and not covered by the panel
        allConstraints.insets = new Insets(5, 0, 0, 0);

        allConstraints.anchor = GridBagConstraints.NORTHWEST;
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
        // this is just a cheap trick i made 'cause i don't properly know java swing: let's fake the background of the window so the it seems the column on the left is full length vertically
        all.setBackground(new Color(238,238,238));
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
        MainDialog root = this;
        lst_rois.setSelectionModel(new DefaultListSelectionModel() {
            // Thanks to https://stackoverflow.com/a/31336378/1306679
            @Override
            public void setSelectionInterval(int startIndex, int endIndex) {
                if (startIndex == endIndex) {
                    /*if (multipleItemsCurrentlyArseSelected()) {
                        clearSelection();
                    }
                    if (isSelectedIndex(startIndex)) {
                        clearSelection();
                        eventListener.onMainDialogEvent(new DeselectedRoiEvent(startIndex));
                        btn_deleteRoi.setEnabled(false);
                    }
                    else {*/
                        eventListener.onMainDialogEvent(new SelectedRoiEvent(startIndex));
                        btn_deleteRoi.setEnabled(true);
                        super.setSelectionInterval(startIndex, endIndex);
                    }
               // }
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
        fileMenu.add(menuItem);
        fileMenu.addSeparator();
        menuItem = new MenuItem("Exit");
        menuItem.addActionListener(e -> eventListener.onMainDialogEvent(new ExitEvent()));
        fileMenu.add(menuItem);

        Menu aboutMenu = new Menu("?");
        menuItem = new MenuItem("About...");
        menuItem.addActionListener(e -> eventListener.onMainDialogEvent(new OpenAboutEvent()));
        aboutMenu.add(menuItem);

        MainDialog.currentImage = image;
        menuBar.add(fileMenu);
        menuBar.add(aboutMenu);
        this.addEventListenerToImage();
        this.setMenuBar(menuBar);
        new Zoom().run("scale");
        pack();
    }

     /**
     * Change the actual image displayed in the main view, based on the given BufferedImage istance
     * @param image
     */
    public void changeImage(BufferedImage image) {
        MainDialog.currentImage = image;
        this.setImage(image);
        image.backupRois();
        image.getManager().reset();
        this.image = image;
        if(lst_rois.getSelectedIndex() == -1)
            btn_deleteRoi.setEnabled(false);
        this.image.restoreRois();
        this.drawRois(image.getManager());
        this.addEventListenerToImage();
        // Let's call the zoom plugin to scale the image to fit in the user window
        // The zoom scaling command works on the current active window: to be 100% sure it will work, we need to forcefully select the preview window.
        IJ.selectWindow(this.getImagePlus().getID());
        new Zoom().run("scale");
        this.pack();
    }

    /**
     * Adds an event listener to the current image
     */
    private void addEventListenerToImage() {
        MainDialog root = this;
        this.image.addEventListener(event -> {
            if(event instanceof RoiSelectedEvent) {
                // if a roi is marked as selected, select the appropriate ROI in the listbox in the left of the window
                RoiSelectedEvent roiSelectedEvent = (RoiSelectedEvent)event;
                int index = Arrays.asList(root.image.getManager().getRoisAsArray()).indexOf(roiSelectedEvent.getRoiSelected());
                eventListener.onMainDialogEvent(new SelectedRoiFromOvalEvent(index));
            }
        });
    }

    /**
     * Update the Roi List based on the given RoiManager istance
     * @param manager
     */
    public void drawRois(RoiManager manager) {

        Prefs.useNamesAsLabels = true;
        Prefs.noPointLabels = false;
        int strokeWidth = (int) (image.getWidth() * 0.0025) > 3 ? (int) (image.getWidth() * 0.0025) : 3;
        int roiWidth = Toolkit.getDefaultToolkit().getScreenSize().width > image.getWidth() ? Toolkit.getDefaultToolkit().getScreenSize().width : image.getWidth() ;
        roiWidth = (int)(roiWidth * 0.03);
        Overlay over = new Overlay();
        over.drawBackgrounds(false);
        over.drawLabels(false);
        over.drawNames(true);
        over.setLabelFontSize(Math.round(strokeWidth * 1f), "scale");
        over.setLabelColor(Color.BLUE);
        over.setStrokeWidth((double)strokeWidth);
        over.setStrokeColor(Color.BLUE);
        Arrays.stream(image.getManager().getRoisAsArray()).forEach(roi -> over.add(roi));
        image.getManager().setOverlay(over);

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

    public void setListSelectedIndex(int index) {
        this.lst_rois.setSelectedIndex(index);
    }

    // a simple debounce variable that can put "on hold" a key_release event
    private boolean debounce = false;
    private class KeyboardEventDispatcher implements KeyEventDispatcher {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_RELEASED && e.getKeyCode() == KeyEvent.VK_C && mouseOverCanvas) {
                Point clickCoords = getCanvas().getCursorLoc();
                eventListener.onMainDialogEvent(new AddRoiEvent(clickCoords));
            }
            if(debounce == false && e.getID() == KeyEvent.KEY_RELEASED && (e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_D)) {
                debounce = true;
                new Thread(() -> {
                    try {
                        eventListener.onMainDialogEvent(new ChangeImageEvent(e.getKeyCode() == KeyEvent.VK_A ? ChangeImageEvent.ChangeDirection.PREV : ChangeImageEvent.ChangeDirection.NEXT)).join(0);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    debounce = false;
                }).start();
            }

            if(e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_C)
                e.consume();
            return false;
        }
    }

    @Override
    public void setTitle(String title){
        super.setTitle(DIALOG_STATIC_TITLE + " " + title);
    }
}
