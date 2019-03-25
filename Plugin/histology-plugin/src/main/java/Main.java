/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */


import ij.*;
import ij.io.FileInfo;
import ij.io.OpenDialog;

import ij.process.ImageProcessor;

import java.awt.image.BufferedImage;
import java.io.IOException;
import loci.formats.ChannelSeparator;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.gui.BufferedImageReader;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;
import org.scijava.command.Command;
import org.scijava.command.Previewable;
import org.scijava.plugin.Plugin;

import net.imagej.ImageJ;

/** Loads and displays a dataset using the ImageJ API. */
@Plugin(type = Command.class, headless = true,
		menuPath = "Plugins>HistologyPlugin")
public class Main implements Command, Previewable {


	public static void main(final String... args) throws Exception {

		final ImageJ ij = new ImageJ();
		ij.launch(args);

		ij.command().run(Main.class, true);
	}

	@Override
	public void run() {

		// Chiediamo come prima cosa il file all'utente
		String pathFile =  chooseInitialFile();
		if(pathFile.equals("nullnull"))
			System.exit(0);

		// ImporterOptions options;
		ImagePlus imp = null;
		try {
			/*options = generateIRSTOptions(pathFile);
			imp = BF.openImagePlus(options)[0];*/

			long runtime = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			final IFormatReader imageReader = new ImageReader(ImageReader.getDefaultReaderClasses());
			imageReader.setId(pathFile);
			BufferedImageReader reader = BufferedImageReader.makeBufferedImageReader(imageReader);
			imp = new ImagePlus("", reader.openImage(0));

			long aa = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			System.out.println((aa-runtime)/ 1000000);
			} catch (Exception e) {
			e.printStackTrace();
		}

		// Da indagare. fa un "flatting" di canali multiimmagine. Utile in alcuni casi limite, ma quali sono le sue implicazioni?
// 		imp.flattenStack();

		MainDialog dialog = new MainDialog(imp, pathFile);
		dialog.show();
		if(dialog.dialog.wasCanceled())
			System.exit(0);

		// TODO: perché imps sempre 0?
		// Se l'utente vuole aprire l'immagine come multistack, deleghiamo tutto a imagej
		if(dialog.isOpenAsMultiStack()) {
			imp.show();
			return;
		}

		// L'utente richiede di aprire le immagini singolarmente. accontentiamolo (sigh)
		openFileSingularly(pathFile);
	}

	private String chooseInitialFile() {
		OpenDialog od = new OpenDialog("Selezionare un'immagine");

		String dir = od.getDirectory();
		String name = od.getFileName();
		return (dir + name);
	}

	private ImporterOptions generateIRSTOptions(String pathFile) throws IOException {
		ImporterOptions options = new ImporterOptions();
		options.setColorMode(ImporterOptions.COLOR_MODE_DEFAULT);
		options.setId(pathFile);
		// Per aprire files in maniera "ridotta": imps = BF.openThumbImagePlus(options);
		return options;
	}

	// TODO: Controllare se sia necessario passare pathFile! forse basta passargli l'imp
	private void openFileSingularly(String pathFile) {
		ImageProcessorReader imageProcessorReader = new ImageProcessorReader(
				new ChannelSeparator(LociPrefs.makeImageReader()));
		try {
			imageProcessorReader.setId(pathFile);
			int imageCount = imageProcessorReader.getImageCount();


			for (int i = 0; i < imageCount ; i++) {
				ImageProcessor imageProcessor = imageProcessorReader.openProcessors(i)[0];
				ImagePlus plus = new ImagePlus("c" + i, imageProcessor);
				plus.show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void preview() {

	}

	@Override
	public void cancel() {

	}
}

