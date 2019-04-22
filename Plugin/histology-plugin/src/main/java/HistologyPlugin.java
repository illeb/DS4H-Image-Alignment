import histology.BufferedImagesManager;
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
import net.imagej.ops.Op;
import net.imagej.ops.OpEnvironment;
import org.scijava.AbstractContextual;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import net.imagej.ImageJ;

import java.awt.*;
import java.text.MessageFormat;
import java.util.Arrays;

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
			System.exit(0);

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

		if(dialogEvent instanceof DeleteEvent){
		    DeleteEvent event = (DeleteEvent)dialogEvent;
		    image.getManager().select(event.getRoiIndex());
		    image.getManager().runCommand("Delete");
		    mainDialog.updateRoiList(image.getManager());
			if(previewDialog != null && previewDialog.isVisible())
				previewDialog.updateRoisOnScreen();
        }

		if(dialogEvent instanceof AddRoiEvent) {
			AddRoiEvent event = (AddRoiEvent)dialogEvent;
			OvalRoi roi = new OvalRoi (event.getClickCoords().x - 15, event.getClickCoords().y - 15, image.getWindow().getWidth() / 12, image.getWindow().getWidth() / 12);
			roi.setCornerDiameter(30);
			roi.setFillColor(Color.red);
			roi.setStrokeColor(Color.blue);
			roi.setStrokeWidth(3);
			roi.setImage(image);
			image.getManager().add(image, roi, 0);

			mainDialog.updateRoiList(image.getManager());

			image.getManager().runCommand("show all with labels");
			if(previewDialog != null && previewDialog.isVisible())
				previewDialog.updateRoisOnScreen();
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

