package histology.previewdialog;

import histology.BufferedImagesManager;
import histology.maindialog.CustomCanvas;
import histology.maindialog.OnMainDialogEventListener;
import histology.previewdialog.event.ChangeImageEvent;
import histology.previewdialog.event.CloseDialogEvent;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.Roi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PreviewDialog extends ImageWindow {

    private OnPreviewDialogEventListener listener;
    private Panel all = new Panel();
    BufferedImagesManager.BufferedImage currentImage;
    public PreviewDialog(BufferedImagesManager.BufferedImage startingImage, OnPreviewDialogEventListener listener, int scrollbarStartingValue, int scrollbarMaximum) {
        super(startingImage, new CustomCanvas(startingImage));
        this.currentImage = startingImage;
        setTitle("Preview window");
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
            if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
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
        scrollbar.addAdjustmentListener(e -> this.listener.onPreviewDialogEvent(new ChangeImageEvent(scrollbar.getValue())));
        this.listener = listener;
        pack();

        updateRoisOnScreen();
    }

    public void changeImage(BufferedImagesManager.BufferedImage image) {
        this.setImage(image);
        this.currentImage = image;
        updateRoisOnScreen();
    }

    public void updateRoisOnScreen() {
        Overlay overlay = new Overlay();
		for (int i = overlay.size(); i > 0; i--)
			overlay.remove(i);
		for(Roi roi : this.currentImage.getManager().getSelectedRoisAsArray()) {
			Roi newROi = new Roi(roi.getXBase(), roi.getYBase(), roi.getFloatWidth(), roi.getFloatHeight());
			newROi.setStrokeColor(roi.getStrokeColor());
			overlay.add(roi);
		}
		this.getImagePlus().setOverlay(overlay);
    }
}
