package DS4H;

import ij.ImagePlus;
import ij.plugin.frame.RoiManager;
import ij.process.ImageConverter;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.gui.BufferedImageReader;
import loci.formats.in.MetadataLevel;
import loci.formats.services.OMEXMLService;
import loci.plugins.in.DisplayHandler;
import loci.plugins.in.ImagePlusReader;
import loci.plugins.in.ImportProcess;
import loci.plugins.in.ImporterOptions;
import loci.plugins.util.LociPrefs;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ImageFile {
    private String pathFile;
    private boolean reducedImageMode;
    private List<RoiManager> roiManagers;

    private Dimension editorImageDimension;
    private BufferedImageReader bufferedEditorImageReader;
    private ImportProcess importProcess;
    public ImageFile(String pathFile) {
        this.pathFile = pathFile;
        this.roiManagers = new ArrayList<>();
    }

    public void generateImageReader() throws FormatException, IOException, BufferedImagesManager.ImageOversizeException {
        this.importProcess = getImageImportingProcess(pathFile);
        final IFormatReader imageReader = new ImageReader(ImageReader.getDefaultReaderClasses());
        imageReader.setId(pathFile);
        // final IFormatReader imageReader = new ImageReader(ImageReader.getDefaultReaderClasses());
        boolean over2GBLimit = (long)imageReader.getSizeX() * (long)imageReader.getSizeY() * imageReader.getRGBChannelCount() > Integer.MAX_VALUE / 3;
        if(over2GBLimit) {
            /*if(imageReader.getSeriesCount() <= 1)
                throw new BufferedImagesManager.ImageOversizeException();*/

            // Cycles all the avaiable series in search of an image with sustainable size
            for (int i = 0; i < imageReader.getSeriesCount() && !this.reducedImageMode; i++) {
                imageReader.setSeries(i);
                over2GBLimit = (long)imageReader.getSizeX() * (long)imageReader.getSizeY() * imageReader.getRGBChannelCount() > Integer.MAX_VALUE / 3;

                if(!over2GBLimit)
                    this.reducedImageMode = true;
            }

            // after all cycles, if we did not found an alternative series of sustainable size, throw an error
            /*if(!this.reducedImageMode)
                throw new BufferedImagesManager.ImageOversizeException();*/
        }

        this.editorImageDimension = new Dimension(imageReader.getSizeX(),imageReader.getSizeY());
        this.bufferedEditorImageReader = BufferedImageReader.makeBufferedImageReader(imageReader);
        for(int i=0; i < bufferedEditorImageReader.getImageCount(); i++)
            this.roiManagers.add(new RoiManager(false));
    }

    public int getNImages() {
        return this.bufferedEditorImageReader.getImageCount();
    }

    public BufferedImage getImage(int index, boolean wholeSlide) throws IOException, FormatException {
        if(!wholeSlide)
            return new BufferedImage("", bufferedEditorImageReader.openImage(index), roiManagers.get(index), reducedImageMode);
        else{
            if(virtualStack == null) {
                try {
                    getWholeSlideImage();
                } catch (DependencyException e) {
                    e.printStackTrace();
                } catch (ServiceException e) {
                    e.printStackTrace();
                }
            }
            virtualStack.setZ(index + 1);
            return new BufferedImage("", new ImagePlus("", virtualStack.getProcessor()).getImage(), roiManagers.get(index),  this.editorImageDimension);
        }
    }

    public void dispose() throws IOException {
        bufferedEditorImageReader.close();
        roiManagers.forEach(Window::dispose);
    }

    ImagePlus virtualStack = null;
    private void getWholeSlideImage() throws IOException, FormatException, DependencyException, ServiceException {

        ImporterOptions options = new ImporterOptions();
        options.loadOptions();
        options.setVirtual(true);
        options.setId(pathFile);
        options.setSplitChannels(false);
        options.setSeriesOn(0, true);
        ImportProcess process = new ImportProcess(options);
        ImageReader imageReader = LociPrefs.makeImageReader();
        IFormatReader baseReader = imageReader.getReader(pathFile);
        ServiceFactory factory = new ServiceFactory();
        OMEXMLService service = factory.getInstance(OMEXMLService.class);
        loci.formats.meta.MetadataStore meta = service.createOMEXMLMetadata();

        baseReader.setMetadataStore(meta);

        baseReader.setMetadataFiltered(true);
        baseReader.setGroupFiles(false);
        baseReader.getMetadataOptions().setMetadataLevel(
                MetadataLevel.ALL);
        baseReader.setId(pathFile);
        DisplayHandler displayHandler = new DisplayHandler(process);
        displayHandler.displayOriginalMetadata();
        displayHandler.displayOMEXML();
        process.execute();
        ImagePlusReader reader = new ImagePlusReader(process);
        virtualStack = readPixels(reader, process.getOptions(), displayHandler)[0];
        new ImageConverter(virtualStack).convertToRGB();
    }

    public ImagePlus[] readPixels(ImagePlusReader reader, ImporterOptions options,
                                  DisplayHandler displayHandler) throws FormatException, IOException
    {
        if (options.isViewNone()) return null;
        if (!options.isQuiet()) reader.addStatusListener(displayHandler);
        ImagePlus[] imps = reader.openImagePlus();
        return imps;
    }

    public List<RoiManager> getRoiManagers() {
        return this.roiManagers;
    }

    public static long estimateMemoryUsage(String pathFile) throws IOException, FormatException {
        ImporterOptions options = new ImporterOptions();
        options.loadOptions();
        options.setVirtual(false);
        options.setId(pathFile);
        options.setSplitChannels(false);
        options.setSeriesOn(0, true);
        ImportProcess process = new ImportProcess(options);
        process.execute();
        return process.getMemoryUsage() * 3;
    }

    private ImportProcess getImageImportingProcess(String pathFile) throws IOException, FormatException {
        ImporterOptions options = new ImporterOptions();
        options.loadOptions();
        options.setVirtual(false);
        options.setId(pathFile);
        options.setSplitChannels(false);
        options.setColorMode(ImporterOptions.COLOR_MODE_COMPOSITE);
        options.setSeriesOn(0, true);
        ImportProcess process = new ImportProcess(options);
        process.execute();
        return process;
    }

    public Dimension getMaximumSize() {
        Dimension maximumSize = new Dimension();
        for (int i = 0; i < importProcess.getReader().getSeriesCount(); i++) {
            importProcess.getReader().setSeries(i);
            maximumSize.width = importProcess.getReader().getSizeX() > maximumSize.width ? importProcess.getReader().getSizeX() : maximumSize.width;
            maximumSize.height = importProcess.getReader().getSizeY() > maximumSize.height ? importProcess.getReader().getSizeY() : maximumSize.height;
        }
        return maximumSize;
    }
}
