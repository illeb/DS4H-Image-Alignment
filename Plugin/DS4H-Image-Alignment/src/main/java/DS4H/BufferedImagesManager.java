package DS4H;

import ij.ImagePlus;
import ij.plugin.frame.RoiManager;
import loci.formats.FormatException;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class BufferedImagesManager implements ListIterator<ImagePlus>{
    private List<ImageFile> imageFiles;
    private int imageIndex;
    public BufferedImagesManager(String pathFile) throws ImageOversizeException, FormatException, IOException {
        this.imageFiles = new  ArrayList<>();
        this.imageIndex = -1;
        addFile(pathFile);
    }

    public void addFile(String pathFile) throws IOException, FormatException, ImageOversizeException {
        ImageFile imageFile = new ImageFile(pathFile);
        imageFile.generateImageReader();
        this.imageFiles.add(imageFile);
    }

    private BufferedImage getImage(int index, boolean wholeSlide) {
        int progressive = 0;
        ImageFile imageFile = null;
        for (int i = 0; i < imageFiles.size(); i++) {

            if(progressive + imageFiles.get(i).getNImages() > index) {
                imageFile = imageFiles.get(i);
                break;
            }
            progressive+=imageFiles.get(i).getNImages();
        }

        BufferedImage image = null;
        try {
            image = imageFile.getImage(index - progressive,  wholeSlide);
            image.setTitle(MessageFormat.format("Editor Image {0}/{1}", index + 1, this.getNImages()));
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
        return getImage(imageIndex, false);
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
        return getImage(imageIndex, false);
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
        return imageFiles.stream().mapToInt(ImageFile::getNImages).sum();
    }

    /**
     * This flag indicates whenever the manger uses a reduced-size image for compatibility
     */
    public void dispose() {
        this.imageFiles.forEach(imageFile -> {
            try {
                imageFile.dispose();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public BufferedImage get(int index) {
        return this.getImage(index, false);
    }

    public BufferedImage get(int index, boolean wholeSlide) {
        return this.getImage(index, wholeSlide);
    }

    public List<RoiManager> getRoiManagers() {
        List<RoiManager> result = new ArrayList<>();

        this.imageFiles.forEach(imageFile -> result.addAll(imageFile.getRoiManagers()));
        return  result;
    }

    public static class ImageOversizeException extends Exception { }
}
