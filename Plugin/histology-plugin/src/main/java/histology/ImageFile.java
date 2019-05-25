package histology;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
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
        return new BufferedImage("", bufferedImageReader.openImage(index), roiManagers.get(index), reducedImageMode);
    }

    public String getPathFile() {
        return pathFile;
    }

    public void dispose() throws IOException {
        bufferedImageReader.close();
        roiManagers.forEach(Window::dispose);
    }

    private void  getWholeSlideImage(String pathFile) throws IOException, FormatException, DependencyException, ServiceException {

        ImporterOptions options = new ImporterOptions();
        options.loadOptions();
        options.setVirtual(true);
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

        DisplayHandler displayHandler = new DisplayHandler(process);
        displayHandler.displayOriginalMetadata();
        displayHandler.displayOMEXML();
        BF.debug("read pixel data");
        process.execute();
        ImagePlusReader reader = new ImagePlusReader(process);
        ImagePlus[] imps = readPixels(reader, process.getOptions(), displayHandler);
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
}
