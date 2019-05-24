package histology;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.in.MetadataLevel;
import loci.formats.services.OMEXMLService;
import loci.plugins.BF;
import loci.plugins.in.DisplayHandler;
import loci.plugins.in.ImagePlusReader;
import loci.plugins.in.ImportProcess;
import loci.plugins.in.ImporterOptions;
import loci.plugins.util.LociPrefs;
import mpicbg.ij.Mapping;
import mpicbg.ij.TransformMeshMapping;
import mpicbg.models.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LeastSquareImageTransformation {

    /**
     * Performs a least square transformation between two BufferedImages with a series of fixed parameters.
     */
    public static ImagePlus transform(BufferedImage source, BufferedImage template) {
        Mapping<?> mapping;
        final MovingLeastSquaresTransform t = new MovingLeastSquaresTransform();
        try {
            t.setModel( AffineModel2D.class );
        } catch (Exception e) {
            e.printStackTrace();
        }
        t.setAlpha(1.0f);
        int meshResolution = 32;

        final ImagePlus target = template.createImagePlus();
        final ImageProcessor ipSource = source.getProcessor();
        final ImageProcessor ipTarget = source.getProcessor().createProcessor( template.getWidth(), template.getHeight() );
        final List<Point> sourcePoints = Arrays.stream(source.getManager().getRoisAsArray())
                .map(roi -> new Point(new double[]{roi.getXBase(), roi.getYBase()}))
                .collect(Collectors.toList());
        final List<Point> templatePoints = Arrays.stream(template.getManager().getRoisAsArray())
                .map(roi -> new Point(new double[]{roi.getXBase(), roi.getYBase()}))
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
//https://stackoverflow.com/questions/4994690/how-can-i-transform-xy-coordinates-and-height-width-on-a-scaled-image-to-an-orig**strong%20text**
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


    private void  test(String pathFile) throws IOException, FormatException, DependencyException, ServiceException {

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

   /*     BufferedImageReader imageBuffer = BufferedImageReader.makeBufferedImageReader(baseReader);
        for(int i=0; i < imageBuffer.getImageCount(); i++)
            this.roiManagers.add(new RoiManager(false));
        imageBuffers.add(imageBuffer);
*/

        DisplayHandler displayHandler = new DisplayHandler(process);
        displayHandler.displayOriginalMetadata();
        displayHandler.displayOMEXML();
        BF.debug("read pixel data");
        process.execute();
        ImagePlusReader reader = new ImagePlusReader(process);
        ImagePlus[] imps = readPixels(reader, process.getOptions(), displayHandler);
        displayHandler.displayImages(imps);
        //         return process;
    }

    public ImagePlus[] readPixels(ImagePlusReader reader, ImporterOptions options,
                                  DisplayHandler displayHandler) throws FormatException, IOException
    {
        if (options.isViewNone()) return null;
        if (!options.isQuiet()) reader.addStatusListener(displayHandler);
        ImagePlus[] imps = reader.openImagePlus();
        return imps;
    }

}
