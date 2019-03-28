/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */


import ij.*;
import ij.io.OpenDialog;

import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.gui.BufferedImageReader;
import org.scijava.command.Command;
import org.scijava.command.Previewable;
import org.scijava.plugin.Plugin;

import net.imagej.ImageJ;

/** Loads and displays a dataset using the ImageJ API. */
@Plugin(type = Command.class, headless = true,
		menuPath = "Plugins>HistologyPlugin")
public class HistologyPlugin implements Command, Previewable {
	private BufferedImageReader imageBuffer = null;
	private ImagePlus image = null;
	private int imageIndex = 0;

	public static void main(final String... args) throws Exception {

		final ImageJ ij = new ImageJ();
		ij.launch(args);

		ij.command().run(HistologyPlugin.class, true);
	}

	private static void NextImageRequested(MainDialog.GUIEvents value) {
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

		dialog.NextImageEvent$.subscribe(value -> {
			imageIndex++;
			showImage(imageIndex);
		});
		dialog.PrevImageEvent$.subscribe(value -> {
			imageIndex++;
			showImage(imageIndex);
		});
		dialog.ResetMarkerEvent$.subscribe(value -> {

		});
		dialog.AddMarkerEvent$.subscribe(value -> {

		});

		showImage(0);
	}

	private void showImage(int imageIndex) {
		if(image != null)
			image.close();
		// per evitare memory leaks, invochiamo manualmente il garbage collector ad ogni cambio di immagine
		System.gc();
		try {
			image = new ImagePlus("", imageBuffer.openImage(imageIndex));
		} catch (Exception e) {
			e.printStackTrace();
		}
		image.show();
	}
	private String chooseInitialFile() {
		OpenDialog od = new OpenDialog("Selezionare un'immagine");
		String dir = od.getDirectory();
		String name = od.getFileName();
		return (dir + name);
	}

	@Override
	public void preview() {

	}

	@Override
	public void cancel() {

	}
}

