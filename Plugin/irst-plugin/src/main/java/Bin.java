import ij.IJ;
import ij.ImagePlus;
import loci.formats.FormatException;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;

import java.io.IOException;

public class Bin {

    /*// checkbox group 1
		/*int rows1=4, columns1=2;
		String[] headings1 = {
				"Dataset Organization",
				"Memory Management"};
		String[] labels1 = {
				"Group files with similar names",
				"Use virtual stack",
				"Open files individually",
				"Record modifications",
				"Swap dimensions",
				"Crop on import",
				"",  // leave blank
				"Specify range for series"};
		boolean[] states1 = {false,false,true,false,false,true,false, false};

		// checkbox group 2
		int rows2=4, columns2=3;
		String[] headings2 = {
				"Color Options",
				"Split",
				"Display"};
		String[] labels2 = {
				"Merge RGB",
				"Channels",
				"Metadata",
				"Colorize",
				"Focal planes",
				"OME-XML",
				"Swap channels",
				"Timepoints",
				"ROIs",
				"Autoscale"};
		boolean[] states2 = {false,true,true,false,false,true,false,false, false,true};
		GenericDialog gd = new GenericDialog("Dialog With Many Options");*/
		/*
		gd.addCheckboxGroup(rows1, columns1, labels1, states1, headings1);
		gd.setInsets(20, 0, 0 );
		gd.addCheckboxGroup(rows2, columns2, labels2, states2, headings2);
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		IJ.log("");
		for (int i=0; i<labels1.length; i++) {
			if (labels1[i].length()!=0) {
				boolean b = gd.getNextBoolean();
				IJ.log(labels1[i]+": "+b);
			}
		}
		IJ.log("");
		for (int i=0; i<labels2.length; i++) {
			if (labels2[i].length()!=0) {
				boolean b = gd.getNextBoolean();
				IJ.log(labels2[i]+": "+b);
			}
		}*/
  /*  String dir = od.getDirectory();
    String name = od.getFileName();
    String id = dir + name;
*/
		/*try {

            /*
            Apertura di semplici file (colore funziona)
            ImagePlus[] imps = BF.openImagePlus(id);
            for (ImagePlus imp : imps) imp.show();

        try {
            ImporterOptions options = new ImporterOptions();
            options.setColorMode(ImporterOptions.COLOR_MODE_DEFAULT);
            options.setId(id);
            ImagePlus[] imps = BF.openImagePlus(options);

            // Imposta la slice da vedere manualmente
            // imps[0].setSlice(3);
            imps[0].setIJMenuBar(false);
            imps[0].show();
            //	image.show();
*/
			/*options.setCBegin(s, c)
			options.setCEnd(s,c)
			options.setSeriesOn(s,True)*/

		/*	ImagePlus[] imps = BF.openImagePlus(options);

			ImageProcessorReader imageProcessorReader = new ImageProcessorReader(
					new ChannelSeparator(LociPrefs.makeImageReader()));
			try {
                // è sostanzialmente un setPath
				imageProcessorReader.setId(id);
				// numero di immagini nello stack
				int numeroImmagini = imageProcessorReader.getImageCount();

				int width = imageProcessorReader.getSizeX();
				int height = imageProcessorReader.getSizeY();
				ImageStack stack = new ImageStack(width, height);

				byte[][][] lookupTable = new byte[imageProcessorReader.getSizeC()][][];
				for (int i = 0; i < numeroImmagini; i++) {
					IJ.showStatus("Reading image pl<ane #" + (i + 1) + "/" + numeroImmagini);
					ImageProcessor imageProcessor = imageProcessorReader.openProcessors(i)[0];
					stack.addSlice("" + (i + 1), imageProcessor);
					int channel = imageProcessorReader.getZCTCoords(i)[1];
					lookupTable[channel] = imageProcessorReader.get8BitLookupTable();

				}
				IJ.showStatus("Constructing image");
				ImagePlus imp = new ImagePlus(name, stack);

				ImagePlus colorizedImage = applyLookupTables(imageProcessorReader, imp, lookupTable);
				imageProcessorReader.close();

				//colorizedImage.show();
				IJ.showStatus("");

				colorizedImage.show();
				IJ.showStatus("");
        } catch (FormatException exc) {
            IJ.error("Sorry, an error occurred: " + exc.getMessage());
        } catch (IOException exc) {
            IJ.error("Sorry, an error occurred: " + exc.getMessage());
        }*/

	/*		ImagePlus imp = IJ.getImage();
			ImageConverter ic = new ImageConverter(imp);
			ic.convertToRGB();
			imp.updateAndDraw();
    }*/


	/*
	private static ImagePlus applyLookupTables(IFormatReader r, ImagePlus imp,
											   byte[][][] lookupTable) {
		// apply color lookup tables, if present
		// this requires ImageJ v1.39 or higher
		if (r.isIndexed()) {
			CompositeImage composite =
					new CompositeImage(imp, CompositeImage.COLOR);
			for (int c = 0; c < r.getSizeC(); c++) {
				composite.setPosition(c + 1, 1, 1);
				LUT lut =
						new LUT(lookupTable[c][0], lookupTable[c][1], lookupTable[c][2]);
				composite.setChannelLut(lut);
			}
			composite.setPosition(1, 1, 1);
			return composite;
		}
		return imp;
	}*/
}
