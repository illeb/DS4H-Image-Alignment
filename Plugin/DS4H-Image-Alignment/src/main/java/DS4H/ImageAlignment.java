package DS4H;

import DS4H.maindialog.MainDialog;
import DS4H.aligndialog.AlignDialog;
import DS4H.aligndialog.OnAlignDialogEventListener;
import DS4H.aligndialog.event.IAlignDialogEvent;
import DS4H.aligndialog.event.ReuseImageEvent;
import DS4H.aligndialog.event.SaveEvent;
import DS4H.previewdialog.OnPreviewDialogEventListener;
import DS4H.previewdialog.PreviewDialog;
import DS4H.maindialog.OnMainDialogEventListener;
import DS4H.maindialog.event.*;
import DS4H.previewdialog.event.CloseDialogEvent;
import DS4H.previewdialog.event.IPreviewDialogEvent;
import ij.*;
import ij.gui.*;

import ij.io.FileSaver;
import ij.io.OpenDialog;
import ij.io.SaveDialog;
import ij.plugin.ImagesToStack;
import ij.plugin.frame.RoiManager;
import loci.formats.UnknownFormatException;
import net.imagej.ops.Op;
import net.imagej.ops.OpEnvironment;
import org.scijava.AbstractContextual;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import net.imagej.ImageJ;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** Loads and displays a dataset using the ImageJ API. */
@Plugin(type = Command.class, headless = true,
		menuPath = "Plugins>DSH4 Image Alignment")
public class ImageAlignment extends AbstractContextual implements Op, OnMainDialogEventListener, OnPreviewDialogEventListener, OnAlignDialogEventListener {
	private BufferedImagesManager manager;
	private BufferedImage image = null;
	private MainDialog mainDialog;
	private PreviewDialog previewDialog;
	private AlignDialog alignDialog;
	private LoadingDialog loadingDialog;
	private AboutDialog aboutDialog;

	private List<String> alignedImagePaths = new ArrayList<>();
	private boolean alignedImageSaved = false;

	static private String IMAGES_CROPPED_MESSAGE = "Image size too large: image has been cropped for compatibility.";
	static private String SINGLE_IMAGE_MESSAGE = "Only one image detected in the stack: align operation will be unavailable.";
	static private String IMAGES_OVERSIZE_MESSAGE = "Cannot open the selected image: image exceed supported dimensions.";
	static private String ALIGNED_IMAGE_NOT_SAVED_MESSAGE = "Aligned images not saved: are you sure you want to exit without saving?";
	static private String IMAGE_SAVED_MESSAGE  = "Image successfully saved";
	static private String ROI_NOT_ADDED_MESSAGE = "One or more corner points not added: they exceed the image bounds";
	static private String INSUFFICIENT_MEMORY_MESSAGE = "Insufficient computer memory (RAM) available. \n\n\t Try to increase the allocated memory by going to \n\n\t                Edit  ▶ Options  ▶ Memory & Threads \n\n\t Change \"Maximum Memory\" to, at most, 1000 MB less than your computer's total RAM.";
	static private String UNKNOWN_FORMAT_MESSAGE = "Error: trying to open a file with a unsupported format.";
	static private long TotalMemory = 0;
	public static void main(final String... args) {
		ImageJ ij = new ImageJ();
		ij.ui().showUI();
		ImageAlignment plugin = new ImageAlignment();
		plugin.setContext(ij.getContext());
		plugin.run();
	}

	@Override
	public void run() {
		// Chiediamo come prima cosa il file all'utente
		String pathFile = promptForFile();
		if (pathFile.equals("nullnull"))
			return;
		this.initialize(pathFile);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			this.alignedImagePaths.forEach(imagePath -> {
				try {
					Files.deleteIfExists(Paths.get(imagePath));
				} catch (IOException e) { }
			});
		}));
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
			new Thread(() -> {
				PreviewImageEvent event = (PreviewImageEvent)dialogEvent;
				if(!event.getValue()) {
					previewDialog.close();
					return;
				}

				try {
					this.loadingDialog.showDialog();
					previewDialog = new PreviewDialog(manager.get(manager.getCurrentIndex()), this, manager.getCurrentIndex(), manager.getNImages(), "Preview Image " + (manager.getCurrentIndex()+1) + "/" + manager.getNImages());
				} catch (Exception e) { }
				this.loadingDialog.hideDialog();
				previewDialog.pack();
				previewDialog.setVisible(true);
				previewDialog.drawRois();
			}).start();
		}

		if(dialogEvent instanceof ChangeImageEvent) {
			new Thread(() -> {
				ChangeImageEvent event = (ChangeImageEvent)dialogEvent;
				// per evitare memory leaks, invochiamo manualmente il garbage collector ad ogni cambio di immagine
				image = event.getChangeDirection() == ChangeImageEvent.ChangeDirection.NEXT ? this.manager.next() : this.manager.previous();

				mainDialog.changeImage(image);
				IJ.freeMemory();
				mainDialog.setPrevImageButtonEnabled(manager.hasPrevious());
				mainDialog.setNextImageButtonEnabled(manager.hasNext());
				mainDialog.setTitle(MessageFormat.format("Editor Image {0}/{1}", manager.getCurrentIndex() + 1, manager.getNImages()));
				this.loadingDialog.hideDialog();

				refreshRoiGUI();
			}).start();
			this.loadingDialog.showDialog();
		}

		if(dialogEvent instanceof DeleteRoiEvent){
			DeleteRoiEvent event = (DeleteRoiEvent)dialogEvent;
			image.getManager().select(event.getRoiIndex());
			image.getManager().runCommand("Delete");

			refreshRoiGUI();
		}

		if(dialogEvent instanceof AddRoiEvent) {
			AddRoiEvent event = (AddRoiEvent)dialogEvent;

			int roiWidth = Toolkit.getDefaultToolkit().getScreenSize().width > image.getWidth() ? Toolkit.getDefaultToolkit().getScreenSize().width : image.getWidth() ;
			roiWidth = (int)(roiWidth * 0.03);
			OvalRoi roi = new OvalRoi (event.getClickCoords().x - (roiWidth / 2), event.getClickCoords().y - (roiWidth/2), roiWidth, roiWidth);
			roi.setCornerDiameter(10);
			roi.setFillColor(Color.red);
			roi.setStrokeColor(Color.blue);

			// get roughly the 0,25% of the width of the image as stroke width of th rois added.
			// If the resultant value is too small, set it as the minimum value
			int strokeWidth = (int) (image.getWidth() * 0.0025) > 3 ? (int) (image.getWidth() * 0.0025) : 3;
			roi.setStrokeWidth(strokeWidth);

			roi.setImage(image);
			image.getManager().add(image, roi, image.getManager().getRoisAsArray().length + 1);

			refreshRoiGUI();
		}

		if(dialogEvent instanceof SelectedRoiEvent) {
			SelectedRoiEvent event = (SelectedRoiEvent)dialogEvent;
			Arrays.stream(image.getManager().getSelectedRoisAsArray()).forEach(roi -> roi.setStrokeColor(Color.BLUE));
			image.getManager().select(event.getRoiIndex());

			image.getRoi().setStrokeColor(Color.yellow);
			image.updateAndDraw();
			if(previewDialog != null && previewDialog.isVisible())
				previewDialog.drawRois();
			Dimension max = manager.getMaximumSize();
		}

		if(dialogEvent instanceof DeselectedRoiEvent) {
			DeselectedRoiEvent event = (DeselectedRoiEvent)dialogEvent;
			Arrays.stream(image.getManager().getSelectedRoisAsArray()).forEach(roi -> roi.setStrokeColor(Color.BLUE));
			image.getManager().select(event.getRoiIndex());
			previewDialog.drawRois();
		}

		if(dialogEvent instanceof AlignEvent) {
			AlignEvent event = (AlignEvent)dialogEvent;
			this.loadingDialog.showDialog();

			// Timeout is necessary to ensure that the loadingDialog is shwon
			Utilities.setTimeout(() -> {
				ArrayList<ImagePlus> images = new ArrayList<>();
				BufferedImage sourceImg = manager.get(0, true);
				images.add(sourceImg);
				for(int i=1; i < manager.getNImages(); i++)
					images.add(LeastSquareImageTransformation.transform(manager.get(i, true), sourceImg, event.isRotate()));
				ImagePlus stack = ImagesToStack.run(images.toArray(new ImagePlus[images.size()]));
				String filePath = IJ.getDir("temp") + stack.hashCode();
				alignedImagePaths.add(filePath);
				new FileSaver(stack).saveAsTiff(filePath);
				this.loadingDialog.hideDialog();
				JOptionPane.showMessageDialog(null, "Operation complete. Image has been temporarily saved to " + filePath);
				alignDialog = new AlignDialog(stack, this);
				alignDialog.pack();
				alignDialog.setVisible(true);
			}, 10);
		}

		if(dialogEvent instanceof OpenFileEvent || dialogEvent instanceof ExitEvent) {
			boolean roisPresent = manager.getRoiManagers().stream().filter(manager -> manager.getRoisAsArray().length != 0).count() > 0;
			if(roisPresent){
				String[] buttons = { "Yes", "No"};
				String message = dialogEvent instanceof OpenFileEvent ? "This will replace the existing image. Proceed anyway?" : "You will lose the existing added landmarks. Proceed anyway?";
				int answer = JOptionPane.showOptionDialog(null, message, "Careful now",
						JOptionPane.WARNING_MESSAGE, 0, null, buttons, buttons[1]);

				if(answer == 1)
					return;
			}

			if(dialogEvent instanceof OpenFileEvent){
				String pathFile = promptForFile();
				if (pathFile.equals("nullnull"))
					return;
				this.disposeAll();
				this.initialize(pathFile);
			}
			else {
				disposeAll();
				System.exit(0);
			}
		}

		if(dialogEvent instanceof OpenAboutEvent) {
			this.aboutDialog.setVisible(true);
		}

		if(dialogEvent instanceof MovedRoiEvent) {
			this.mainDialog.refreshROIList(image.getManager());
			if(previewDialog != null)
				this.previewDialog.drawRois();
		}

		if(dialogEvent instanceof AddFileEvent) {
			String pathFile = ((AddFileEvent) dialogEvent).getFilePath();
			try {
				long memory = ImageFile.estimateMemoryUsage(pathFile);
				TotalMemory += memory;
				if(TotalMemory >= Runtime.getRuntime().maxMemory()) {
					JOptionPane.showMessageDialog(null, INSUFFICIENT_MEMORY_MESSAGE, "Error: insufficient memory", JOptionPane.ERROR_MESSAGE);
					return;
				}
				manager.addFile(pathFile);
			}
			catch(UnknownFormatException e){
				loadingDialog.hideDialog();
				JOptionPane.showMessageDialog(null, UNKNOWN_FORMAT_MESSAGE, "Error: unknow format", JOptionPane.ERROR_MESSAGE);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			mainDialog.setPrevImageButtonEnabled(manager.hasPrevious());
			mainDialog.setNextImageButtonEnabled(manager.hasNext());
			mainDialog.setTitle(MessageFormat.format("Editor Image {0}/{1}", manager.getCurrentIndex() + 1, manager.getNImages()));
			refreshRoiGUI();
		}

		if(dialogEvent instanceof CopyCornersEvent) {
			// get the indexes of all roi managers with at least a roi added
			List<Integer> imageIndexes =  manager.getRoiManagers().stream()
					.filter(roiManager -> roiManager.getRoisAsArray().length != 0)
					.map(roiManager -> manager.getRoiManagers().indexOf(roiManager))
					.filter(index -> index != manager.getCurrentIndex())// remove the index of the current image, if present.
					.collect(Collectors.toList());

			Object[] options = imageIndexes.stream().map(imageIndex -> "Image " + (imageIndex + 1)).toArray();
			JComboBox optionList = new JComboBox(options);
			optionList.setSelectedIndex(0);

			int n = JOptionPane.showOptionDialog(new JFrame(), optionList,
					"Copy from", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
					null, new Object[] {"Copy", "Cancel"}, JOptionPane.YES_OPTION);

			if (n == JOptionPane.YES_OPTION) {
				RoiManager selectedManager = manager.getRoiManagers().get(imageIndexes.get(optionList.getSelectedIndex()));
				List<Point> roiPoints = Arrays.stream(selectedManager.getRoisAsArray()).map(roi -> new Point((int)roi.getRotationCenter().xpoints[0], (int)roi.getRotationCenter().ypoints[0]))
						.collect(Collectors.toList());
				roiPoints.stream().filter(roiCoords-> roiCoords.x < image.getWidth() && roiCoords.y < image.getHeight())
						.forEach(roiCoords ->this.onMainDialogEvent(new AddRoiEvent(roiCoords)));

				if(roiPoints.stream().anyMatch(roiCoords-> roiCoords.x > image.getWidth() || roiCoords.y > image.getHeight()))
					JOptionPane.showMessageDialog(null, ROI_NOT_ADDED_MESSAGE, "Warning", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	@Override
	public void onPreviewDialogEvent(IPreviewDialogEvent dialogEvent) {
		if(dialogEvent instanceof DS4H.previewdialog.event.ChangeImageEvent) {
			DS4H.previewdialog.event.ChangeImageEvent event = (DS4H.previewdialog.event.ChangeImageEvent)dialogEvent;
			new Thread(() -> {
				WindowManager.setCurrentWindow(image.getWindow());
				BufferedImage image = manager.get(event.getIndex());
				IJ.freeMemory();
				previewDialog.changeImage(image, "Preview Image " + (event.getIndex()+1) + "/" + manager.getNImages());
				this.loadingDialog.hideDialog();
			}).start();
			this.loadingDialog.showDialog();
		}

		if(dialogEvent instanceof CloseDialogEvent) {
			mainDialog.setPreviewWindowCheckBox(false);
		}
	}

	@Override
	public void onAlignDialogEventListener(IAlignDialogEvent dialogEvent) {

		if(dialogEvent instanceof SaveEvent) {
            SaveDialog saveDialog = new SaveDialog("Save as", "aligned", ".tiff");
            if (saveDialog.getFileName()==null) {
                loadingDialog.hideDialog();
                return;
            }
            String path = saveDialog.getDirectory()+saveDialog.getFileName();
            loadingDialog.showDialog();
            new FileSaver(alignDialog.getImagePlus()).saveAsTiff(path);
            loadingDialog.hideDialog();
            JOptionPane.showMessageDialog(null, IMAGE_SAVED_MESSAGE, "Save complete", JOptionPane.INFORMATION_MESSAGE);
            this.alignedImageSaved = true;
		}

		if(dialogEvent instanceof ReuseImageEvent) {
			this.disposeAll();
			this.initialize(alignedImagePaths.get(alignedImagePaths.size()-1));
		}

		if(dialogEvent instanceof DS4H.aligndialog.event.ExitEvent) {
			if(!alignedImageSaved) {
				String[] buttons = { "Yes", "No"};
				int answer = JOptionPane.showOptionDialog(null, ALIGNED_IMAGE_NOT_SAVED_MESSAGE, "Careful now",
						JOptionPane.WARNING_MESSAGE, 0, null, buttons, buttons[1]);
				if(answer == 1)
					return;
			}
			alignDialog.setVisible(false);
			alignDialog.dispose();
		}
	}

	/**
	 * Refresh all the Roi-based guis in the maindialog
	 */
	private void refreshRoiGUI() {

		mainDialog.drawRois(image.getManager());
		if(previewDialog != null && previewDialog.isVisible())
			previewDialog.drawRois();

		// Get the number of rois added in each image. If they are all the same (and at least one is added), we can enable the "align" functionality
		List<Integer> roisNumber = manager.getRoiManagers().stream().map(roiManager -> roiManager.getRoisAsArray().length).collect(Collectors.toList());
		boolean alignButtonEnabled = roisNumber.get(0) >= LeastSquareImageTransformation.MINIMUM_ROI_NUMBER && manager.getNImages() > 1 && roisNumber.stream().distinct().count() == 1;
		// check if: the number of images is more than 1, ALL the images has the same number of rois added and the ROI numbers are more than 3
		mainDialog.setAlignButtonEnabled(alignButtonEnabled);

		boolean copyCornersEnabled = manager.getRoiManagers().stream()
				.filter(roiManager -> roiManager.getRoisAsArray().length != 0)
				.map(roiManager -> manager.getRoiManagers().indexOf(roiManager))
				.filter(index -> index != manager.getCurrentIndex()).count() != 0;
		mainDialog.setCopyCornersEnabled(copyCornersEnabled);
	}
	/**
	 * Initialize the plugin opening the file specified in the mandatory param
	 */
	public void initialize(String pathFile) {

		Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
			if(e instanceof  OutOfMemoryError){
				this.loadingDialog.hideDialog();
				JOptionPane.showMessageDialog(null, INSUFFICIENT_MEMORY_MESSAGE, "Error: insufficient memory", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		});
		this.aboutDialog = new AboutDialog();
		this.loadingDialog = new LoadingDialog();
		this.loadingDialog.showDialog();
		alignedImageSaved = false;
		boolean complete = false;
		try {

			try {
				long memory = ImageFile.estimateMemoryUsage(pathFile);
				TotalMemory += memory;
				if(TotalMemory >= Runtime.getRuntime().maxMemory()) {
					JOptionPane.showMessageDialog(null, INSUFFICIENT_MEMORY_MESSAGE, "Error: insufficient memory", JOptionPane.ERROR_MESSAGE);
					this.run();
				}
			}
			catch(UnknownFormatException e){
				loadingDialog.hideDialog();
				JOptionPane.showMessageDialog(null, UNKNOWN_FORMAT_MESSAGE, "Error: unknown format", JOptionPane.ERROR_MESSAGE);
			}
			manager = new BufferedImagesManager(pathFile);
			image = manager.next();
			mainDialog = new MainDialog(image, this);
			mainDialog.setPrevImageButtonEnabled(manager.hasPrevious());
			mainDialog.setNextImageButtonEnabled(manager.hasNext());
			mainDialog.setTitle(MessageFormat.format("Editor Image {0}/{1}", manager.getCurrentIndex() + 1, manager.getNImages()));

			mainDialog.pack();
			mainDialog.setVisible(true);

			this.loadingDialog.hideDialog();
			if(image.isReduced())
				JOptionPane.showMessageDialog(null, IMAGES_CROPPED_MESSAGE, "Info", JOptionPane.INFORMATION_MESSAGE);
			if(manager.getNImages() == 1)
				JOptionPane.showMessageDialog(null, SINGLE_IMAGE_MESSAGE, "Warning", JOptionPane.WARNING_MESSAGE);
			complete = true;
		}
		catch (BufferedImagesManager.ImageOversizeException e) {
			JOptionPane.showMessageDialog(null, IMAGES_OVERSIZE_MESSAGE);
		}
		catch(UnknownFormatException e){
			JOptionPane.showMessageDialog(null, UNKNOWN_FORMAT_MESSAGE, "Error: unknown format", JOptionPane.ERROR_MESSAGE);
		}
		catch(loci.common.enumeration.EnumException e) {
			JOptionPane.showMessageDialog(null, UNKNOWN_FORMAT_MESSAGE, "Error: unknown format", JOptionPane.ERROR_MESSAGE);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(!complete) {
				this.loadingDialog.hideDialog();
				this.run();
			}

		}
	}

	private String promptForFile() {
		OpenDialog od = new OpenDialog("Select an image");
		String dir = od.getDirectory();
		String name = od.getFileName();
		return (dir + name);
	}

	/**
	 * Dispose all the opened workload objects.
	 */
	private void disposeAll() {
		this.mainDialog.dispose();
		this.loadingDialog.hideDialog();
		this.loadingDialog.dispose();
		if(this.previewDialog != null)
			this.previewDialog.dispose();
		if(this.alignDialog != null)
			this.alignDialog.dispose();
		this.manager.dispose();
		IJ.freeMemory();
		TotalMemory = 0;
	}

}

