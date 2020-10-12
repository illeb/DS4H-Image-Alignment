package DS4H;

import DS4H.BufferedImage.BufferedImage;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.process.*;
import mpicbg.ij.Mapping;
import mpicbg.ij.TransformMeshMapping;
import mpicbg.models.*;
import mpicbg.models.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LeastSquareImageTransformation {

    public static int MINIMUM_ROI_NUMBER = 3;
    /**
     * Performs a least square transformation between two BufferedImages with a series of fixed parameters.
     */
    public static ImagePlus transform(BufferedImage source, BufferedImage template, boolean rotate) {
        Mapping<?> mapping;
        final MovingLeastSquaresTransform t = new MovingLeastSquaresTransform();
        try {
            t.setModel( rotate ? AffineModel2D.class : TranslationModel2D.class );
        } catch (Exception e) {
            e.printStackTrace();
        }
        t.setAlpha(1.0f);
        int meshResolution = 32;

        final ImagePlus target = template.createImagePlus();
        final ImageProcessor ipSource = source.getProcessor();
        final ImageProcessor ipTarget = source.getProcessor().createProcessor( template.getWidth(), template.getHeight() );

        final List<Point> sourcePoints = Arrays.stream(source.getManager().getRoisAsArray())
                .map(roi -> {
                    double oldX = roi.getXBase();
                    double oldY = roi.getYBase();
                    double newX = oldX * (source.getWidth() / source.getEditorImageDimension().width);
                    double newY = oldY * (source.getHeight() / source.getEditorImageDimension().height);
                    return new Point(new double[]{newX, newY});
                })
                .collect(Collectors.toList());


        final List<Point> templatePoints = Arrays.stream(template.getManager().getRoisAsArray())
                .map(roi -> {
                    double oldX = roi.getXBase();
                    double oldY = roi.getYBase();
                    double newX = oldX * (template.getWidth() / template.getEditorImageDimension().width);
                    double newY = oldY * (template.getHeight() / template.getEditorImageDimension().height);
                    return new Point(new double[]{newX, newY});
                })
                .collect(Collectors.toList());
        final int numMatches = Math.min( sourcePoints.size(), templatePoints.size() );
        final ArrayList<PointMatch> matches = new ArrayList<>();
        for ( int i = 0; i < numMatches; ++i )
            matches.add( new PointMatch( sourcePoints.get( i ), templatePoints.get( i ) ) );

        try
        {
            t.setMatches( matches );
            mapping = new TransformMeshMapping<>(new CoordinateTransformMesh(t, meshResolution, source.getWidth(), source.getHeight()));
        }
        catch ( final Exception e )
        {
            IJ.showMessage( "Not enough landmarks selected to find a transformation model." );
            return null;
        }
        boolean interpolate = true;
        if ( interpolate )
        {
            ipSource.setInterpolationMethod( ImageProcessor.BICUBIC );
            mapping.mapInterpolated( ipSource, ipTarget );
        }
        else
            mapping.map( ipSource, ipTarget );

        target.setProcessor( "Transformed" + source.getTitle(), ipTarget );
        return target;
    }

    public static ImagePlus convertToStack(ImagePlus[] images, int count, int width, int height) {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        ImageStack stack = new ImageStack(width, height);
        FileInfo fi = images[0].getOriginalFileInfo();
        if (fi!=null && fi.directory==null) fi = null;
        for (int i=0; i<count; i++) {
            ImageProcessor ip = images[i].getProcessor();
            if (ip.getMin() < min) min = ip.getMin();
            if (ip.getMax() > max) max = ip.getMax();
            if (ip.getWidth() != width || ip.getHeight() != height) {
                ImageProcessor ip2 = new ColorProcessor(width, height);
                int xoff = 0, yoff = 0;
                ip2.insert(ip, xoff, yoff);
                ip = ip2;
            }
            stack.addSlice("", ip);
        }
        ImagePlus imp = new ImagePlus("", stack);
        imp.getProcessor().setMinAndMax(min, max);
        return imp;
    }
}
