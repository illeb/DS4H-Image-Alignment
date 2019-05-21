package histology;

import ij.ImagePlus;
import ij.Macro;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FilePattern;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.gui.BufferedImageReader;
import loci.formats.in.MetadataLevel;
import loci.formats.services.OMEXMLService;
import loci.plugins.BF;
import loci.plugins.in.DisplayHandler;
import loci.plugins.in.ImagePlusReader;
import loci.plugins.in.ImportProcess;
import loci.plugins.in.ImporterOptions;
import loci.plugins.util.LociPrefs;
import ome.xml.meta.IMetadata;
import ome.xml.meta.MetadataStore;
import ome.xml.meta.OMEXMLMetadata;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class BufferedImagesManager implements ListIterator<ImagePlus>{

    private List<BufferedImageReader> imageBuffers;
    private List<RoiManager> roiManagers;
    private int imageIndex;
    private boolean reducedImageMode;
    public BufferedImagesManager(String pathFile) throws ImageOversizeException, FormatException, IOException {
        imageBuffers = new ArrayList<>();
        this.imageIndex = -1;
        this.roiManagers = new ArrayList<>();
        addFile(pathFile);
    }

    public void addFile(String pathFile) throws IOException, FormatException, ImageOversizeException {
        ImportProcess process = null;
        try {
            process = test(pathFile);
        } catch (DependencyException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        DisplayHandler displayHandler = new DisplayHandler(process);
        displayHandler.displayOriginalMetadata();
        displayHandler.displayOMEXML();

        BF.debug("read pixel data");
        process.execute();
        ImagePlusReader reader = new ImagePlusReader(process);
        ImagePlus[] imps = readPixels(reader, process.getOptions(), displayHandler);

        BF.debug("display pixels");
        displayHandler.displayImages(imps);
        /*
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


        BufferedImageReader imageBuffer = BufferedImageReader.makeBufferedImageReader(imageReader);
        for(int i=0; i < imageBuffer.getImageCount(); i++)
            this.roiManagers.add(new RoiManager(false));
        imageBuffers.add(imageBuffer);*/
    }

    public ImagePlus[] readPixels(ImagePlusReader reader, ImporterOptions options,
                                  DisplayHandler displayHandler) throws FormatException, IOException
    {
        if (options.isViewNone()) return null;
        if (!options.isQuiet()) reader.addStatusListener(displayHandler);
        ImagePlus[] imps = reader.openImagePlus();
        return imps;
    }

    private ImportProcess test(String pathFile) throws IOException, FormatException, DependencyException, ServiceException {

        ImporterOptions options = new ImporterOptions();
            options.loadOptions();
            options.setId(pathFile);
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

        BufferedImageReader imageBuffer = BufferedImageReader.makeBufferedImageReader(baseReader);
        for(int i=0; i < imageBuffer.getImageCount(); i++)
            this.roiManagers.add(new RoiManager(false));
        imageBuffers.add(imageBuffer);

        return process;
    }
    private BufferedImage getImage(int index) {
        int progressive = 0;
        BufferedImageReader imageBuffer = null;
        for (int i = 0; i < imageBuffers.size(); i++) {

            if(progressive + imageBuffers.get(i).getImageCount() > index) {
                imageBuffer = imageBuffers.get(i);
                break;
            }
            progressive+=imageBuffers.get(i).getImageCount();
        }

        BufferedImage image = null;
        try {
            image = new BufferedImage(MessageFormat.format("Editor Image {0}/{1}", index + 1, imageBuffer.getImageCount()), imageBuffer.openImage(index- progressive), roiManagers.get(index) );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    public boolean hasNext() {
        return imageIndex < this.getNImages() - 1;
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
        int progressive = 0;
        BufferedImageReader imageBuffer = null;
        for (int i = 0; i < imageBuffers.size(); i++) {
            progressive += imageBuffers.get(i).getImageCount();
        }
        return progressive;
    }

    /**
     * This flag indicates whenever the manger uses a reduced-size image for compatibility
      */
    public boolean isReducedImageMode() {
        return this.reducedImageMode;
    }

    public void dispose() throws IOException {
        this.getRoiManagers().forEach(Window::dispose);
        this.imageBuffers.forEach(buffer -> {
            try {
                buffer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public class BufferedImage extends ImagePlus {
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
