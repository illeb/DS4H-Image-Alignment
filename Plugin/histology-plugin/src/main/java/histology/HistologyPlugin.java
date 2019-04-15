package histology;/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */


import histology.maindialog.OnDialogEventListener;
import histology.maindialog.event.*;
import ij.*;
import ij.gui.*;

import net.imagej.ops.Op;
import net.imagej.ops.OpEnvironment;
import org.scijava.AbstractContextual;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import net.imagej.ImageJ;

import java.awt.*;
import java.util.Arrays;

/** Loads and displays a dataset using the ImageJ API. */
@Plugin(type = Command.class, headless = true,
		menuPath = "Plugins>histology.HistologyPlugin")
public class HistologyPlugin extends AbstractContextual implements Op, OnDialogEventListener {
	private BufferedImagesManager manager;
	private BufferedImage image = null;
	private ImagePlus previewWindow = null;
	private static ImageJ ij = null;
	private String pathFile;
	// MainDialog dialog;
	private MainDialog dialog;
	public static void main(final String... args) {
		ij = new ImageJ();
		ij.ui().showUI();
		HistologyPlugin plugin = new HistologyPlugin();
		plugin.setContext(ij.getContext());
		plugin.run();

	}

	@Override
	public void run() {

		// Chiediamo come prima cosa il file all'utente
		this.pathFile = MainDialog.PromptForFile();
		if (pathFile.equals("nullnull"))
			System.exit(0);

		try {
			manager = new BufferedImagesManager(pathFile);
			image = manager.next();
			dialog = new MainDialog(image, this);
			dialog.setPrevImageButtonEnabled(manager.hasPrevious());
			dialog.setNextImageButtonEnabled(manager.hasNext());

			dialog.setVisible(true);
			dialog.pack();
			/*previewWindow =  BF.openImagePlus(pathFile)[0];
			previewWindow.flattenStack();
			previewWindow.show();
*/
			this.dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public OpEnvironment ops() {
		return null;
	}

	@Override
	public void setEnvironment(OpEnvironment ops) {

	}

	@Override
	public void onEvent(IDialogEvent dialogEvent) {
		if(dialogEvent instanceof ChangeImageEvent) {
		    ChangeImageEvent event = (ChangeImageEvent)dialogEvent;
			// per evitare memory leaks, invochiamo manualmente il garbage collector ad ogni cambio di immagine
			image = event.getChangeDirection() == ChangeImageEvent.ChangeDirection.NEXT ? this.manager.next() : this.manager.previous();
			dialog.changeImage(image);
			IJ.freeMemory();
			dialog.setPrevImageButtonEnabled(manager.hasPrevious());
			this.dialog.setNextImageButtonEnabled(manager.hasNext());
		}

		if(dialogEvent instanceof DeleteEvent){
		    DeleteEvent event = (DeleteEvent)dialogEvent;
		    image.getManager().select(event.getRoiIndex());
		    image.getManager().runCommand("Delete");
		    dialog.changeImage(image);
        }

		if(dialogEvent instanceof AddRoiEvent) {
			AddRoiEvent event = (AddRoiEvent)dialogEvent;
			OvalRoi roi = new OvalRoi (event.getClickCoords().x - 15, event.getClickCoords().y - 15, 30, 30);
			roi.setCornerDiameter(30);
			roi.setFillColor(Color.red);
			roi.setStrokeColor(Color.blue);
			roi.setStrokeWidth(3);
			roi.setImage(image);
			image.getManager().add(image, roi, 0);

			dialog.updateRoiList(image.getManager());
		}

		if(dialogEvent instanceof SelectedRoiEvent) {
			SelectedRoiEvent event = (SelectedRoiEvent)dialogEvent;
			Arrays.stream(image.getManager().getSelectedRoisAsArray()).forEach(roi -> roi.setStrokeColor(Color.BLUE));
			image.getManager().select(event.getRoiIndex());
			image.getRoi().setStrokeColor(Color.yellow);
			image.updateAndDraw();
		}
	}

	private void updatePreviewWindow() {
		int sliceIndex = manager.getCurrentIndex();

		Overlay overlay = previewWindow.getImageStack().getProcessor(sliceIndex + 1).getOverlay();
		if(overlay == null)
			overlay = new Overlay();

		for (int i = overlay.size(); i > 0; i--)
			overlay.remove(i);
		for(Roi roi : image.getManager().getSelectedRoisAsArray()) {
			Roi newROi = new Roi(roi.getXBase(), roi.getYBase(), roi.getFloatWidth(), roi.getFloatHeight());
			newROi.setStrokeColor(roi.getStrokeColor());
			overlay.add(newROi);
		}
		previewWindow.getImageStack().getProcessor(sliceIndex + 1).setOverlay(overlay);
	}
}

