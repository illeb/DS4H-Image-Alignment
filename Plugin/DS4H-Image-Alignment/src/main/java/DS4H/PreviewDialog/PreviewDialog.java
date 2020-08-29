package DS4H.PreviewDialog;

import DS4H.BufferedImage;
import DS4H.MainDialog.CustomCanvas;
import DS4H.PreviewDialog.event.ChangeImageEvent;
import DS4H.PreviewDialog.event.CloseDialogEvent;
import ij.IJ;
import ij.Prefs;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.plugin.Zoom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

public class PreviewDialog extends ImageWindow {

    private OnPreviewDialogEventListener listener;
    private Panel all = new Panel();
    BufferedImage currentImage;
    public PreviewDialog(BufferedImage startingImage, OnPreviewDialogEventListener listener, int scrollbarStartingValue, int scrollbarMaximum, String title) {
        super(startingImage, new CustomCanvas(startingImage));
        this.currentImage = startingImage;
        setTitle(title);
        final CustomCanvas canvas = (CustomCanvas) getCanvas();

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

        allConstraints.gridx++;
        allConstraints.weightx = 1;
        allConstraints.weighty = 1;
        all.add(canvas, allConstraints);

        JScrollBar scrollbar = new JScrollBar(Adjustable.HORIZONTAL, scrollbarStartingValue, 1, 0, scrollbarMaximum);
        scrollbar.setBlockIncrement(1);
        allConstraints.gridy++;
        all.add(scrollbar, allConstraints);

        GridBagLayout wingb = new GridBagLayout();
        GridBagConstraints winc = new GridBagConstraints();
        winc.anchor = GridBagConstraints.NORTHWEST;
        winc.fill = GridBagConstraints.BOTH;
        winc.weightx = 1;
        winc.weighty = 1;
        setLayout(wingb);
        add(all, winc);

        all.addMouseWheelListener(e ->{
            if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL && !e.isControlDown()) {
                int totalScrollAmount = e.getUnitsToScroll() < 0 ? -1 : 1;
                if(scrollbar.getValue() + totalScrollAmount > scrollbar.getMaximum()){
                   scrollbar.setValue(scrollbar.getMaximum());
                    return;
                }
                if(scrollbar.getValue() + totalScrollAmount < scrollbar.getMinimum()){
                    scrollbar.setValue(scrollbar.getMinimum());
                    return;
                }
                scrollbar.setValue(scrollbar.getValue() + totalScrollAmount);
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                listener.onPreviewDialogEvent(new CloseDialogEvent());
            }
        });
        scrollbar.addAdjustmentListener(e -> {
            scrollbar.updateUI();
            this.listener.onPreviewDialogEvent(new ChangeImageEvent(scrollbar.getValue()));
        });
        this.setResizable(false);
        this.listener = listener;
        new Zoom().run("scale");
        pack();
    }

    public void changeImage(BufferedImage image, String title) {
        this.setImage(image);
        this.currentImage = image;
        drawRois();

        // The zoom scaling command works on the current active window: to be 100% sure it will work, we need to forcefully select the preview window.
        IJ.selectWindow(this.getImagePlus().getID());
        new Zoom().run("scale");
        this.setTitle(title);
        this.pack();
    }

    public void drawRois() {
        Overlay over = new Overlay();
        over.drawBackgrounds(false);
        over.drawLabels(false);
        over.drawNames(true);
        over.setLabelColor(Color.BLUE);
        over.setStrokeColor(Color.BLUE);
        int strokeWidth = (int) (this.currentImage.getWidth() * 0.0025) > 3 ? (int) (this.currentImage.getWidth() * 0.0025) : 3;
        Arrays.stream(this.currentImage.getManager().getRoisAsArray()).forEach(roi -> over.add(roi));
        over.setLabelFontSize(Math.round(strokeWidth * 3.33f), "scale");
        over.setStrokeWidth((double)strokeWidth);
		this.getImagePlus().setOverlay(over);
    }
}
