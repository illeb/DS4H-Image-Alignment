import ij.ImagePlus;
import ij.gui.Overlay;
import ij.plugin.frame.RoiManager;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.gui.BufferedImageReader;
import loci.formats.meta.MetadataStore;
import loci.formats.services.OMEXMLService;

import java.awt.*;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class BufferedImagesManager implements ListIterator<ImagePlus>{

    private BufferedImageReader imageBuffer;
    private List<Overlay> imagesOverlays;
    // private List<RoiManager> imagesROIs;
    private List<RoiManager> roiManagers;
    private int imageIndex;
    public BufferedImagesManager(String pathFile) throws IOException, FormatException {
        this.imageIndex = -1;
        final IFormatReader imageReader = new ImageReader(ImageReader.getDefaultReaderClasses());

        MetadataStore metadata;

        try {
            ServiceFactory factory = new ServiceFactory();
            OMEXMLService service = factory.getInstance(OMEXMLService.class);
            metadata = service.createOMEXMLMetadata();
        }
        catch (Exception exc) {
            throw new FormatException("Could not create OME-XML store.", exc);
        }
        imageReader.setMetadataStore(metadata);
        imageReader.setId(pathFile);
        imageBuffer = BufferedImageReader.makeBufferedImageReader(imageReader);

        this.imagesOverlays = new ArrayList<>(imageBuffer.getImageCount());
        for(int i=0; i < imageBuffer.getImageCount(); i++) {
            Overlay imageOverlay = new Overlay();
            imageOverlay.setFillColor(Color.red);
            imageOverlay.setStrokeColor(Color.black);
            this.imagesOverlays.add(new Overlay());
        }

        this.roiManagers = new ArrayList<>(imageBuffer.getImageCount());
        for(int i=0; i < imageBuffer.getImageCount(); i++)
            this.roiManagers.add(new RoiManager(false));
    }

    private BufferedImage buildImage(int index) {
        BufferedImage image = null;
        try {
            image = new BufferedImage(MessageFormat.format("Image {0}/{1}", index + 1, imageBuffer.getImageCount()), imageBuffer.openImage(index), roiManagers.get(index));
        } catch (Exception e) {
            e.printStackTrace();
        }
        image.setOverlay(imagesOverlays.get(index));
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
        return buildImage(imageIndex);
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
        return buildImage(imageIndex);
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

    public int currentIndex() {
        return imageIndex;
    }

    public BufferedImageReader getReader() {
        return this.imageBuffer;
    }
}
