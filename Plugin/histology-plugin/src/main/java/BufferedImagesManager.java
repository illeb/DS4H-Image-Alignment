import ij.ImagePlus;
import ij.gui.Overlay;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.gui.BufferedImageReader;

import java.awt.*;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class BufferedImagesManager implements ListIterator<ImagePlus>{

    private BufferedImageReader imageBuffer;
    private List<Overlay> imagesOverlays;
    private int imageIndex;
    public BufferedImagesManager(String pathFile) throws IOException, FormatException {
        this.imageIndex = -1;
        final IFormatReader imageReader = new ImageReader(ImageReader.getDefaultReaderClasses());
        imageReader.setId(pathFile);
        imageBuffer = BufferedImageReader.makeBufferedImageReader(imageReader);

        this.imagesOverlays = new ArrayList<>(imageBuffer.getImageCount());
        for(int i=0; i < imageBuffer.getImageCount(); i++) {
            Overlay imageOverlay = new Overlay();
            imageOverlay.setFillColor(Color.red);
            imageOverlay.setStrokeColor(Color.black);
            this.imagesOverlays.add(new Overlay());
        }
    }

    private ImagePlus buildImage(int index) {
        ImagePlus image = null;
        try {
            image = new ImagePlus(MessageFormat.format("Image {0}/{1}", index + 1, imageBuffer.getImageCount()), imageBuffer.openImage(index));
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
    public ImagePlus next() {
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
    public ImagePlus previous() {
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
}
