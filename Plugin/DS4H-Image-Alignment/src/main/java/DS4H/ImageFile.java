package DS4H;

import ij.ImagePlus;
import ij.plugin.frame.RoiManager;
import ij.process.ImageConverter;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.gui.BufferedImageReader;
import loci.plugins.in.DisplayHandler;
import loci.plugins.in.ImagePlusReader;
import loci.plugins.in.ImportProcess;
import loci.plugins.in.ImporterOptions;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageFile {
    private String pathFile;
    private boolean reducedImageMode;
    private List<RoiManager> roiManagers;

    private Dimension editorImageDimension;
    private BufferedImageReader bufferedEditorImageReader;
    private BufferedImageReader bufferedEditorImageReaderWholeSlide;
    private ImportProcess importProcess;
    private boolean wholeSlideInitialized = false;
    public ImageFile(String pathFile) throws IOException, FormatException {
        this.pathFile = pathFile;
        this.roiManagers = new ArrayList<>();
        generateImageReader();
    }

    private void generateImageReader() throws FormatException, IOException {
        this.importProcess = getImageImportingProcess(pathFile);
        final IFormatReader imageReader = new ImageReader(ImageReader.getDefaultReaderClasses());
        imageReader.setId(pathFile);
        boolean over2GBLimit = (long)imageReader.getSizeX() * (long)imageReader.getSizeY() * imageReader.getRGBChannelCount() > Integer.MAX_VALUE / 3;
        if(over2GBLimit) {

            // Cycles all the available series in search of an image with sustainable size
            for (int i = 0; i < imageReader.getSeriesCount() && !this.reducedImageMode; i++) {
                imageReader.setSeries(i);
                over2GBLimit = (long)imageReader.getSizeX() * (long)imageReader.getSizeY() * imageReader.getRGBChannelCount() > Integer.MAX_VALUE / 3;

                if(!over2GBLimit)
                    this.reducedImageMode = true;
            }
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
            if(!wholeSlideInitialized) {
                try {
                    getWholeSlideImage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return new BufferedImage("", bufferedEditorImageReaderWholeSlide.openImage(index), roiManagers.get(index),  this.editorImageDimension);
        }
    }

    public void dispose() throws IOException {
        bufferedEditorImageReader.close();
        roiManagers.forEach(Window::dispose);
    }

    private void getWholeSlideImage() throws IOException, FormatException {
        this.wholeSlideInitialized = true;
        // If the bufferedImageReader is already using the first series (thus the images with the biggest sizes) there is no need to initialize a new bufferedImageReader. We can just reuse it as it is
        if (bufferedEditorImageReader.getSeries() == 0) {
            this.bufferedEditorImageReaderWholeSlide = bufferedEditorImageReader;
            return;
        }
        DisplayHandler displayHandler = new DisplayHandler(importProcess);
        displayHandler.displayOriginalMetadata();
        displayHandler.displayOMEXML();
        this.bufferedEditorImageReaderWholeSlide = BufferedImageReader.makeBufferedImageReader(importProcess.getReader());
        this.bufferedEditorImageReaderWholeSlide.setSeries(0);
    }

    public List<RoiManager> getRoiManagers() {
        return this.roiManagers;
    }

    public static long estimateMemoryUsage(String pathFile) throws IOException, FormatException {
        return getImageImportingProcess(pathFile).getMemoryUsage();
    }

    private static ImportProcess getImageImportingProcess(String pathFile) throws IOException, FormatException {
        ImporterOptions options = new ImporterOptions();
        options.loadOptions();
        options.setVirtual(true);
        options.setId(pathFile);
        options.setSplitChannels(false);
        options.setColorMode(ImporterOptions.COLOR_MODE_DEFAULT);
        options.setSeriesOn(0, true);
        ImportProcess process = new ImportProcess(options);
        process.execute();
        return process;
    }

    /**
     * Returns the maximum image size obtainable by the current ImageFile
     * @return
     */
    public Dimension getMaximumSize() {
        Dimension maximumSize = new Dimension();
        for (int i = 0; i < importProcess.getReader().getSeriesCount(); i++) {
            importProcess.getReader().setSeries(i);
            maximumSize.width = importProcess.getReader().getSizeX() > maximumSize.width ? importProcess.getReader().getSizeX() : maximumSize.width;
            maximumSize.height = importProcess.getReader().getSizeY() > maximumSize.height ? importProcess.getReader().getSizeY() : maximumSize.height;
        }
        return maximumSize;
    }

    public ArrayList<Dimension> getImagesDimensions() {
        ArrayList<Dimension> dimensions = new ArrayList<>();
        for (int i = 0; i < importProcess.getReader().getSeriesCount(); i++) {
            importProcess.getReader().setSeries(i);
            dimensions.add(new Dimension(importProcess.getReader().getSizeX(), importProcess.getReader().getSizeY()));
        }
        return dimensions;
    }

    private List<java.awt.image.BufferedImage> cached_thumbs;
    public List<java.awt.image.BufferedImage> getThumbs() {
        try {
            // lazy initialization
            if(this.cached_thumbs == null) {
                this.cached_thumbs = new ArrayList<>();
                for (int i = 0; i < bufferedEditorImageReader.getImageCount(); i++)
                    cached_thumbs.add(this.bufferedEditorImageReader.openThumbImage(i));
            }
        } catch (FormatException | IOException e) {
            e.printStackTrace();
        }
        return cached_thumbs;
    }

    public String getPathFile() {
        return pathFile;
    }
}
