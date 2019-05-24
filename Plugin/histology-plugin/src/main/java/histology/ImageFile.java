package histology;

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
import java.util.ArrayList;
import java.util.List;

public class ImageFile {
    private String pathFile;
    private BufferedImageReader bufferedImageReader;
    private boolean reducedImageMode;
    private List<RoiManager> roiManagers;

    public ImageFile(String pathFile) {
        this.pathFile = pathFile;
        this.roiManagers = new ArrayList<>();
    }

    public void generateImageReader() throws FormatException, IOException, BufferedImagesManager.ImageOversizeException {
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
                throw new BufferedImagesManager.ImageOversizeException();

            // Cycles all the avaiable series in search of an image with sustainable size
            for (int i = 0; i < imageReader.getSeriesCount() && !this.reducedImageMode; i++) {
                imageReader.setSeries(i);
                over2GBLimit = (long)imageReader.getSizeX() * (long)imageReader.getSizeY() * imageReader.getRGBChannelCount() > Integer.MAX_VALUE;

                if(!over2GBLimit)
                    this.reducedImageMode = true;
            }

            // after all cycles, if we did not found an alternative series of sustainable size, throw an error
            if(!this.reducedImageMode)
                throw new BufferedImagesManager.ImageOversizeException();
        }

        this.bufferedImageReader = BufferedImageReader.makeBufferedImageReader(imageReader);
        for(int i=0; i < bufferedImageReader.getImageCount(); i++)
            this.roiManagers.add(new RoiManager(false));
    }

    public int getNImages() {
        return this.bufferedImageReader.getImageCount();
    }

    public BufferedImage getImage(int index) throws IOException, FormatException {
        return new BufferedImage("", bufferedImageReader.openImage(index), roiManagers.get(index) );
    }

    public String getPathFile() {
        return pathFile;
    }

    public void setPathFile(String pathFile) {
        this.pathFile = pathFile;
    }

    public BufferedImageReader getBufferedImageReader() {
        return bufferedImageReader;
    }

    public void dispose() throws IOException {
        bufferedImageReader.close();
        roiManagers.forEach(Window::dispose);
    }

    public List<RoiManager> getRoiManagers() {
        return this.roiManagers;
    }
}
