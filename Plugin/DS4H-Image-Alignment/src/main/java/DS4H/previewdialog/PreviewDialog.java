package DS4H.previewdialog;

import DS4H.BufferedImage;
import DS4H.maindialog.CustomCanvas;
import DS4H.previewdialog.event.ChangeImageEvent;
import DS4H.previewdialog.event.CloseDialogEvent;
import ij.IJ;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.plugin.Zoom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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
        Overlay overlay = new Overlay();
		for (int i = overlay.size(); i > 0; i--)
			overlay.remove(i);
		for(Roi roi : this.currentImage.getManager().getRoisAsArray()) {
			Roi newROi = new Roi(roi.getXBase(), roi.getYBase(), roi.getFloatWidth(), roi.getFloatHeight());
			newROi.setStrokeColor(roi.getStrokeColor());
			overlay.add(roi);
		}
		this.getImagePlus().setOverlay(overlay);
    }
}
