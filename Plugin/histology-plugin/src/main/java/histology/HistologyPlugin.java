package histology;/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */


import histology.maindialog.OnDialogEventListener;
import histology.maindialog.event.ChangeImageEvent;
import histology.maindialog.event.DeleteEvent;
import histology.maindialog.event.IDialogEvent;
import ij.*;
import ij.gui.*;

import loci.common.services.ServiceFactory;
import loci.formats.gui.BufferedImageWriter;
import loci.formats.out.TiffWriter;
import loci.formats.services.OMEXMLService;
import histology.maindialog.MainDialog;
import net.imagej.ops.Op;
import net.imagej.ops.OpEnvironment;
import org.scijava.AbstractContextual;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import net.imagej.ImageJ;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Loads and displays a dataset using the ImageJ API. */
@Plugin(type = Command.class, headless = true,
		menuPath = "Plugins>histology.HistologyPlugin")
public class HistologyPlugin extends AbstractContextual implements Op, OnDialogEventListener {
	private BufferedImagesManager manager;
	private BufferedImage image = null;
	private static ImageJ ij = null;
	private String pathFile;
	MainDialog dialog;
	public static void main(final String... args) throws Exception {
		ij = new ImageJ();
		ij.ui().showUI();
		HistologyPlugin plugin = new HistologyPlugin();
		plugin.setContext(ij.getContext());
		plugin.run();
	}

	@Override
	public void run() {

		// Chiediamo come prima cosa il file all'utente
		dialog = new MainDialog(this);
		this.pathFile = dialog.PromptForFile();

		if (pathFile.equals("nullnull"))
			System.exit(0);

		try {
			manager = new BufferedImagesManager(pathFile);
			DialogEventsHandler(dialog);
			image = manager.next();
			show(image);
			dialog.setPrevImageButtonEnabled(manager.hasPrevious());
			dialog.setNextImageButtonEnabled(manager.hasNext());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void show(BufferedImage image) {
		image.show();
		ImageCanvas canvas = image.getCanvas();
		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// rendiamo il button di "quick annotaion" pari al click destro del mouse
				if(!SwingUtilities.isRightMouseButton(e))
					return;
				Point clickCoords = canvas.getCursorLoc();
				OvalRoi roi = new OvalRoi (clickCoords.x - 15, clickCoords.y - 15, 30, 30);
				roi.setCornerDiameter(30);
				roi.setFillColor(Color.red);
				roi.setStrokeColor(Color.blue);
				roi.setStrokeWidth(3);
				roi.setImage(image);

				image.getManager().add(image, roi, 0);
				dialog.setImage(image);
			}
		});
	}

	private void DialogEventsHandler(MainDialog dialog) {

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
			IJ.freeMemory();
			if (image != null)
				image.close();
			image = event.getChangeDirection() == ChangeImageEvent.ChangeDirection.NEXT ? this.manager.next() : this.manager.previous();
			show(image);
			dialog.setPrevImageButtonEnabled(manager.hasPrevious());
			this.dialog.setNextImageButtonEnabled(manager.hasNext());
			dialog.setImage(image);
		}

		if(dialogEvent instanceof DeleteEvent){
		    DeleteEvent event = (DeleteEvent)dialogEvent;
		    image.getManager().select(event.getRoiIndex());
		    image.getManager().runCommand("Delete");
		    dialog.setImage(image);
        }

	}
}

