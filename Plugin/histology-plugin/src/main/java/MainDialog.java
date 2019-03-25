import ij.ImagePlus;
import ij.gui.GenericDialog;
import sun.net.www.content.text.Generic;

import java.awt.*;

public class MainDialog {
    GenericDialog dialog;
    ImagePlus imp;

    // FIXME: ricavare pathfile da imp? imp.getFileInfo() è vuoto though
    public MainDialog(ImagePlus imp, String pathFile) {
        this.imp = imp;
        dialog = new GenericDialog("IRST");
        dialog.setResizable(false);
        dialog.setOKLabel("Esegui");
        dialog.setCancelLabel("Annulla");

        int nChannels = imp.getNChannels();
        int nSlices = imp.getNSlices();
        int nDimensions = imp.getNDimensions();
        int nFrames = imp.getNFrames();
        dialog.addMessage(pathFile);
        dialog.addMessage("Found " + nChannels + " channels, " + nSlices + " slices, " + nDimensions + " dimensions, " + nFrames + " frames");
        dialog.addCheckbox("open as multistack?", true);
    }

    public void show() {
        this.dialog.showDialog();
    }

    // MAI visto un modo peggiore di gestire i componenti a schermo. MAI
    public boolean isOpenAsMultiStack() {
        return this.dialog.getNextBoolean();
    }
}
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