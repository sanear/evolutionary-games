import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class PhaseDiagramDriver {
    private final static int PIXELS_PER_GRIDPOINT = 36;// must be a perfect square
    private final static String IMAGE_FILE_EXTENSION = "png";
    public static void main(String[] args) {
	boolean doDisplay = false;
	if (Arrays.asList(args).contains("-h") || Arrays.asList(args).contains("-?")) {
	    System.out.println("Usage: $ PhaseDiagramsExe [-d] [-h][-?] [a12 a21 resolution stop_time update_method]");
	    System.exit(0);
	}
	if (Arrays.asList(args).contains("-d")) {
	    doDisplay = true;
	    String[] tempArgs = new String[args.length-1];
	    int offset = 0;
	    for (int i=0; i<args.length; i++) {
		if (args[i].equals("-d"))
		    offset++;
		else
		    tempArgs[i-offset] = args[i];
	    }
	    args = tempArgs;
	}
	PhaseDiagramIterator iterator = null;
	if (args.length >= 0 && args.length < 5) {
	    iterator = new PhaseDiagramIterator();
	    System.out.println("Iterator created... starting...");
	    iterator.iterate();
	}
	else if (args.length >= 5) {
	    iterator = new PhaseDiagramIterator(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
	    System.out.println("Iterator created... starting...");
	    System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(new Date()));
	    iterator.iterate();
	    System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(new Date()));
	}
	else {
	    System.err.println("Requires four arguments.");
	    System.exit(1);
	}
	String partialPath = "/Users/andy/Desktop/imageTests/img";
	String path = partialPath;
	boolean ok = false;
	for (int i = 1; !ok; i++) {
	    if ((new File(path+"."+IMAGE_FILE_EXTENSION)).exists())
		path = partialPath + "" + i;
	    else
		ok = true;
	}
	BufferedImage image = generateAndSaveImage(iterator.getProportions(), new File(path+"."+IMAGE_FILE_EXTENSION));
	if (doDisplay) {
	    for(int i=0; i<args.length; i++)
		System.out.print(args[i] + " ");
	    displayImage(image);
	}
	System.out.println("\nFile saved successfully. Exiting...");
    }

    public static void displayImage(BufferedImage image) {
	ImageIcon icon = new ImageIcon(image);
        JLabel label = new JLabel(icon, JLabel.CENTER);
        JOptionPane.showMessageDialog(null, label, "icon", -1);
    }

    public static BufferedImage generateAndSaveImage(double[][] proportions, File file) {
	BufferedImage image = new BufferedImage(PIXELS_PER_GRIDPOINT*proportions.length, PIXELS_PER_GRIDPOINT*proportions.length, BufferedImage.TYPE_BYTE_GRAY);
	WritableRaster raster = image.getRaster();
	raster.setPixels(0, 0, image.getWidth(), image.getHeight(), getRasterArray(proportions));
	try {
	    ImageIO.write(image, IMAGE_FILE_EXTENSION, file);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return image;
    }

    public static double[] getRasterArray(double[][] proportions) {
	double[][] interimArray = new double[proportions.length*PIXELS_PER_GRIDPOINT][proportions.length*PIXELS_PER_GRIDPOINT];
	for (int i=0; i<proportions.length; i++) {
	    for (int j=0; j<proportions.length; j++) {
		for(int k=0; k<PIXELS_PER_GRIDPOINT; k++) {
		    for (int l=0; l<PIXELS_PER_GRIDPOINT; l++) {
			interimArray[i*PIXELS_PER_GRIDPOINT+k][j*PIXELS_PER_GRIDPOINT+l] = proportions[i][j];
		    }
		}
	    }
	}
	double[] array = new double[interimArray.length*interimArray.length];
	for (int i=0; i<interimArray.length; i++) {
	    for (int j=0; j<interimArray.length; j++) {
		array[i*interimArray.length + j] = (255-interimArray[i][j]*255.0);
	    }
	}
	return array;
    }
}
