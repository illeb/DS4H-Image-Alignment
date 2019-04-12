/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */


import ij.*;
import ij.gui.*;

import loci.common.services.ServiceFactory;
import loci.formats.gui.BufferedImageWriter;
import loci.formats.out.TiffWriter;
import loci.formats.services.OMEXMLService;
import net.imagej.ops.Op;
import net.imagej.ops.OpEnvironment;
import org.scijava.AbstractContextual;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import net.imagej.ImageJ;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Loads and displays a dataset using the ImageJ API. */
@Plugin(type = Command.class, headless = true,
		menuPath = "Plugins>HistologyPlugin")
public class HistologyPlugin extends AbstractContextual implements Op, OnDialogEvcentListener {
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
	public void onEvent(MainDialog.GUIEvents event) {
		if(event == MainDialog.GUIEvents.NEXT || event == MainDialog.GUIEvents.PREVIOUS) {
			// per evitare memory leaks, invochiamo manualmente il garbage collector ad ogni cambio di immagine
			IJ.freeMemory();
			if (image != null)
				image.close();
			image = event == MainDialog.GUIEvents.NEXT ? this.manager.next() : this.manager.previous();
			show(image);
			dialog.setPrevImageButtonEnabled(manager.hasPrevious());
			this.dialog.setNextImageButtonEnabled(manager.hasNext());
		}

		if(event == MainDialog.GUIEvents.RESET) {
			TiffWriter a = new TiffWriter();

			ServiceFactory factory;
			try {
				factory = new ServiceFactory();
				OMEXMLService service = factory.getInstance(OMEXMLService.class);
				a.setMetadataRetrieve(service.asRetrieve(manager.getReader().getMetadataStore()));
				a.setId("E:/istologia/Istologia/prova.tiff");
				BufferedImageWriter writer = BufferedImageWriter.makeBufferedImageWriter(a);
				writer.setSeries(0);
				//			writer.setWriteSequentially(true);
				writer.saveImage(manager.currentIndex(), image.getBufferedImage());
				writer.saveImage(manager.currentIndex() + 1, manager.next().getBufferedImage());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

