/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */


import ij.*;
import ij.io.OpenDialog;

import io.reactivex.Observable;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.gui.BufferedImageReader;
import net.imagej.ops.Op;
import net.imagej.ops.OpEnvironment;
import org.scijava.AbstractContextual;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import net.imagej.ImageJ;
import org.scijava.tool.Tool;


/** Loads and displays a dataset using the ImageJ API. */
@Plugin(type = Command.class, headless = true,
		menuPath = "Plugins>HistologyPlugin")
public class HistologyPlugin extends AbstractContextual implements Op {
	private BufferedImageReader imageBuffer = null;
	private ImagePlus image = null;
	private int imageIndex = 0;
	private static ImageJ ij = null;
	public static void main(final String... args) throws Exception {
		ij = new ImageJ();
		ij.ui().showUI();
		HistologyPlugin plugin = new HistologyPlugin();
		plugin.setContext(ij.getContext());
		plugin.run();
		// ij.command().run(HistologyPlugin.class, true);
	}
	@Override
	public void run() {
		// Chiediamo come prima cosa il file all'utente
		String pathFile =  chooseInitialFile();

		if(pathFile.equals("nullnull"))
			System.exit(0);

		try {
			final IFormatReader imageReader = new ImageReader(ImageReader.getDefaultReaderClasses());
			imageReader.setId(pathFile);
			imageBuffer = BufferedImageReader.makeBufferedImageReader(imageReader);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		MainDialog dialog = new MainDialog();

		dialog.PrevImageEvent$.subscribe(value -> {
			imageIndex--;
		});
		dialog.NextImageEvent$.subscribe(value -> {
			imageIndex++;
		});
		Observable.merge(dialog.PrevImageEvent$, dialog.NextImageEvent$).subscribe(event -> {
			showImage(imageIndex);
			dialog.setPrevImageButtonEnabled(imageIndex != 0);
			dialog.setNextImageButtonEnabled(imageIndex < imageBuffer.getImageCount() - 1);
		});
		dialog.ResetMarkerEvent$.subscribe(value -> {

		});
		dialog.AddMarkerEvent$.subscribe(value -> {
			Tool tool = ij.tool().getTool("Oval");
			ij.tool().setActiveTool(tool);
		});

		showImage(0);
		dialog.setPrevImageButtonEnabled(imageIndex != 0);
		dialog.setNextImageButtonEnabled(imageIndex < imageBuffer.getImageCount() - 1);

		// image.setOverlay(new Overlay(new Roi(10,10,50,50)));

	}

	private void showImage(int imageIndex) {
		if(image != null)
			image.close();
		// per evitare memory leaks, invochiamo manualmente il garbage collector ad ogni cambio di immagine
		IJ.freeMemory();
		try {
			image = new ImagePlus("", imageBuffer.openImage(imageIndex));
			// api di image2j per mostrare l'immagine
			// Display<?> display = ij.display().createDisplay(image);
		} catch (Exception e) {
			e.printStackTrace();
		}
		image.show();
		image.setIJMenuBar(true);
	}
	private String chooseInitialFile() {
		OpenDialog od = new OpenDialog("Selezionare un'immagine");
		String dir = od.getDirectory();
		String name = od.getFileName();
		return (dir + name);
	}

	@Override
	public OpEnvironment ops() {
		return null;
	}

	@Override
	public void setEnvironment(OpEnvironment ops) {

	}
}

