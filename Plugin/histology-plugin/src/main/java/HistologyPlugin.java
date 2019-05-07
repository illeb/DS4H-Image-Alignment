import histology.BufferedImagesManager;
import histology.LeastSquareImageTransformation;
import histology.maindialog.MainDialog;
import histology.previewdialog.OnPreviewDialogEventListener;
import histology.previewdialog.PreviewDialog;
import histology.maindialog.OnMainDialogEventListener;
import histology.maindialog.event.*;
import histology.previewdialog.event.CloseDialogEvent;
import histology.previewdialog.event.IPreviewDialogEvent;
import ij.*;
import ij.gui.*;

import ij.io.OpenDialog;
import ij.plugin.ImagesToStack;
import net.imagej.ops.Op;
import net.imagej.ops.OpEnvironment;
import org.scijava.AbstractContextual;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import net.imagej.ImageJ;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** Loads and displays a dataset using the ImageJ API. */
@Plugin(type = Command.class, headless = true,
		menuPath = "Plugins>HistologyPlugin")
public class HistologyPlugin extends AbstractContextual implements Op, OnMainDialogEventListener, OnPreviewDialogEventListener {
	private BufferedImagesManager manager;
	private BufferedImagesManager.BufferedImage image = null;
	private PreviewDialog previewDialog;
	private MainDialog mainDialog;
	public static void main(final String... args) {
		ImageJ ij = new ImageJ();
		ij.ui().showUI();
		HistologyPlugin plugin = new HistologyPlugin();
		plugin.setContext(ij.getContext());
		plugin.run();
	}

	@Override
	public void run() {

		// Chiediamo come prima cosa il file all'utente
		String pathFile = promptForFile();
		if (pathFile.equals("nullnull"))
			return;

		try {
			manager = new BufferedImagesManager(pathFile);
			image = manager.next();
			mainDialog = new MainDialog(image, this);
			mainDialog.setPrevImageButtonEnabled(manager.hasPrevious());
			mainDialog.setNextImageButtonEnabled(manager.hasNext());
			mainDialog.setTitle(MessageFormat.format("Editor Image {0}/{1}", manager.getCurrentIndex() + 1, manager.getNImages()));

			mainDialog.setVisible(true);
			mainDialog.pack();
			this.mainDialog.setVisible(true);

			if(manager.isReducedImageMode())
				JOptionPane.showMessageDialog(null, "Image size too large: image has been cropped for compatibility.");
		}
		catch (BufferedImagesManager.ImageOversizeException e) {
			JOptionPane.showMessageDialog(null, "Cannot open the selected image: image exceed supported dimensions.");
		}
		catch (Exception e) {
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
	public void onMainDialogEvent(IMainDialogEvent dialogEvent) {
		WindowManager.setCurrentWindow(image.getWindow());
		if(dialogEvent instanceof PreviewImageEvent) {
			PreviewImageEvent event = (PreviewImageEvent)dialogEvent;
			if(!event.getValue()) {
				previewDialog.close();
				return;
			}

			try {
				previewDialog = new PreviewDialog( manager.get(manager.getCurrentIndex()), this, manager.getCurrentIndex(), manager.getNImages());
				previewDialog.setVisible(true);
			} catch (Exception e) { }
		}

		if(dialogEvent instanceof ChangeImageEvent) {
		    ChangeImageEvent event = (ChangeImageEvent)dialogEvent;
			// per evitare memory leaks, invochiamo manualmente il garbage collector ad ogni cambio di immagine
			image = event.getChangeDirection() == ChangeImageEvent.ChangeDirection.NEXT ? this.manager.next() : this.manager.previous();
			mainDialog.changeImage(image);
			IJ.freeMemory();
			mainDialog.setPrevImageButtonEnabled(manager.hasPrevious());
			mainDialog.setNextImageButtonEnabled(manager.hasNext());
			mainDialog.setTitle(MessageFormat.format("Editor Image {0}/{1}", manager.getCurrentIndex() + 1, manager.getNImages()));
		}

		if(dialogEvent instanceof DeleteRoiEvent){
		    DeleteRoiEvent event = (DeleteRoiEvent)dialogEvent;
		    image.getManager().select(event.getRoiIndex());
		    image.getManager().runCommand("Delete");
		    mainDialog.updateRoiList(image.getManager());
			if(previewDialog != null && previewDialog.isVisible())
				previewDialog.updateRoisOnScreen();

			// Get the number of rois added in each image. If they are all the same (and at least one is added), we can enable the "merge" functionality
			List<Integer> roisNumber = manager.getRoiManagers().stream().map(roiManager -> roiManager.getRoisAsArray().length).collect(Collectors.toList());
			mainDialog.setMergeButtonEnabled(roisNumber.get(0) != 0 && roisNumber.stream().distinct().count() == 1);
        }

		if(dialogEvent instanceof AddRoiEvent) {
			AddRoiEvent event = (AddRoiEvent)dialogEvent;

			int roiWidth = Toolkit.getDefaultToolkit().getScreenSize().width > image.getWidth() ? image.getWidth() : Toolkit.getDefaultToolkit().getScreenSize().width;
			roiWidth /= 12;
			OvalRoi roi = new OvalRoi (event.getClickCoords().x - (roiWidth / 2), event.getClickCoords().y - (roiWidth/2), roiWidth, roiWidth);
			roi.setCornerDiameter(10);
			roi.setFillColor(Color.red);
			roi.setStrokeColor(Color.blue);

			// get roughly the 0,25% of the width of the image as stroke width of th rois added.
			// If the resultant value is too small, set it as the minimum value
			int strokeWidth = (int) (image.getWidth() * 0.0025) > 3 ? (int) (image.getWidth() * 0.0025) : 3;
			roi.setStrokeWidth(strokeWidth);

			roi.setImage(image);
			image.getManager().add(image, roi, 0);

			mainDialog.updateRoiList(image.getManager());

			image.getManager().runCommand("show all with labels");
			if(previewDialog != null && previewDialog.isVisible())
				previewDialog.updateRoisOnScreen();

			// Get the number of rois added in each image. If they are all the same (and at least one is added), we can enable the "merge" functionality
			List<Integer> roisNumber = manager.getRoiManagers().stream().map(roiManager -> roiManager.getRoisAsArray().length).collect(Collectors.toList());
			mainDialog.setMergeButtonEnabled(roisNumber.get(0) != 0 && roisNumber.stream().distinct().count() == 1);
		}

		if(dialogEvent instanceof SelectedRoiEvent) {
			SelectedRoiEvent event = (SelectedRoiEvent)dialogEvent;
			Arrays.stream(image.getManager().getSelectedRoisAsArray()).forEach(roi -> roi.setStrokeColor(Color.BLUE));
			image.getManager().select(event.getRoiIndex());

			image.getRoi().setStrokeColor(Color.yellow);
			image.updateAndDraw();
			if(previewDialog != null && previewDialog.isVisible())
				previewDialog.updateRoisOnScreen();
		}

		if(dialogEvent instanceof DeselectedRoiEvent) {
			DeselectedRoiEvent event = (DeselectedRoiEvent)dialogEvent;
			Arrays.stream(image.getManager().getSelectedRoisAsArray()).forEach(roi -> roi.setStrokeColor(Color.BLUE));
			image.getManager().select(event.getRoiIndex());
		}

		if(dialogEvent instanceof MergeEvent) {
			ArrayList<ImagePlus> images = new ArrayList<>();
			images.add(manager.get(0));
			for(int i=1; i < manager.getNImages(); i++)
				images.add(LeastSquareImageTransformation.transform(manager.get(i),manager.get(0)));
			ImagePlus stack = ImagesToStack.run(images.toArray(new ImagePlus[images.size()]));
			stack.show("Merged images");
		}
	}

	@Override
	public void onPreviewDialogEvent(IPreviewDialogEvent dialogEvent) {
		if(dialogEvent instanceof histology.previewdialog.event.ChangeImageEvent) {
			WindowManager.setCurrentWindow(image.getWindow());
			histology.previewdialog.event.ChangeImageEvent event = (histology.previewdialog.event.ChangeImageEvent)dialogEvent;
			BufferedImagesManager.BufferedImage image = manager.get(event.getIndex());
			previewDialog.changeImage(image);
			IJ.freeMemory();
		}

		if(dialogEvent instanceof CloseDialogEvent) {
			mainDialog.setPreviewWindowCheckBox(false);
		}
	}

	private String promptForFile() {
		OpenDialog od = new OpenDialog("Selezionare un'immagine");
		String dir = od.getDirectory();
		String name = od.getFileName();
		return (dir + name);
	}
}

