package histology;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.gui.BufferedImageReader;
import loci.formats.services.OMEXMLService;

import java.awt.*;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class BufferedImagesManager implements ListIterator<ImagePlus>{

    private BufferedImageReader imageBuffer;
    private List<RoiManager> roiManagers;
    private int imageIndex;
    private boolean reducedImageMode;
    public String pathFile;
    public BufferedImagesManager(String pathFile) throws IOException, FormatException, ImageOversizeException {
        this.pathFile = pathFile;
        this.imageIndex = -1;
        final IFormatReader imageReader = new ImageReader(ImageReader.getDefaultReaderClasses());

        try {
            ServiceFactory factory = new ServiceFactory();
            OMEXMLService service = factory.getInstance(OMEXMLService.class);
            service.createOMEXMLMetadata();
        }
        catch (Exception exc) {
            throw new FormatException("Could not create OME-XML store.", exc);
        }
        imageReader.setId(pathFile);
        boolean over2GBLimit = (long)imageReader.getSizeX() * (long)imageReader.getSizeY() * imageReader.getRGBChannelCount() > Integer.MAX_VALUE;
        if(over2GBLimit) {
            if(imageReader.getSeriesCount() <= 1)
                throw new ImageOversizeException();

            // Cycles all the avaiable series in search of an image with sustainable size
            for (int i = 0; i < imageReader.getSeriesCount() && !this.reducedImageMode; i++) {
                imageReader.setSeries(i);
                over2GBLimit = (long)imageReader.getSizeX() * (long)imageReader.getSizeY() * imageReader.getRGBChannelCount() > Integer.MAX_VALUE;

                if(!over2GBLimit)
                    this.reducedImageMode = true;
            }

            // after all cycles, if we did not found an alternative series of sustainable size, throw an error
            if(!this.reducedImageMode)
                throw new ImageOversizeException();
        }


        imageBuffer = BufferedImageReader.makeBufferedImageReader(imageReader);
        this.roiManagers = new ArrayList<>(imageBuffer.getImageCount());
        for(int i=0; i < imageBuffer.getImageCount(); i++)
            this.roiManagers.add(new RoiManager(false));
    }

    private BufferedImage getImage(int index) {
        BufferedImage image = null;
        try {
            image = new BufferedImage(MessageFormat.format("Editor Image {0}/{1}", index + 1, imageBuffer.getImageCount()), imageBuffer.openImage(index), roiManagers.get(index));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    public boolean hasNext() {
        return imageIndex < imageBuffer.getImageCount() - 1;
    }

    @Override
    public BufferedImage next() {
        if(!hasNext())
            return null;
        imageIndex++;
        return getImage(imageIndex);
    }

    @Override
    public boolean hasPrevious() {
        return imageIndex > 0;
    }

    @Override
    public BufferedImage previous() {
        if(!hasPrevious())
            return null;
        imageIndex--;
        return getImage(imageIndex);
    }

    @Override
    public int nextIndex() {
        return imageIndex + 1;
    }

    @Override
    public int previousIndex() {
        return imageIndex - 1;
    }

    @Override
    public void remove() { }

    @Override
    public void set(ImagePlus imagePlus) { }

    @Override
    public void add(ImagePlus imagePlus) { }

    public int getCurrentIndex() {
        return this.imageIndex;
    }

    public int getNImages() {
        return imageBuffer.getImageCount();
    }

    /**
     * This flag indicates whenever the manger uses a reduced-size image for compatibility
      */
    public boolean isReducedImageMode() {
        return this.reducedImageMode;
    }

    public void dispose() throws IOException {
        this.getRoiManagers().forEach(Window::dispose);
        this.imageBuffer.close();
    }

    public static class BufferedImage extends ImagePlus {
        private RoiManager  manager;
        private Roi[] roisBackup;
        public BufferedImage(String text, Image image, RoiManager manager) {
            super(text, image);
            this.manager = manager;
        }

        public RoiManager getManager() {
            return this.manager;
        }

        public void restoreRois() {
            Arrays.stream(this.roisBackup).forEach(roi -> manager.add(this, roi, 0));
        }

        public void backupRois() {
            this.roisBackup = this.manager.getRoisAsArray();
        }
    }

    public BufferedImage get(int index) {
        return this.getImage(index);
    }

    public List<RoiManager> getRoiManagers() {
        return roiManagers;
    }

    public class ImageOversizeException extends Exception { }
}
