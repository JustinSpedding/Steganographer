import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

// By: Justin Spedding & Andrew Miller

public class FileBrowsers {

	/**
	 * Browses for an image that ImageIO is capable of reading
	 *
	 * @return Either the path to the selected image or null if canceled
	 */
	public static String browseHostImage() {
		JFileChooser chooser = new JFileChooser();
	    FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", ImageIO.getReaderFormatNames());
	    chooser.setFileFilter(filter);
	    int returnVal = chooser.showOpenDialog(null);
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
	    	return chooser.getSelectedFile().getPath();
	    }
	    return null;
	}

	/**
	 * Browses for a bitmap image
	 *
	 * @return Either the path to the selected image or null if canceled
	 */
	public static String browseOutputImage() {
		JFileChooser chooser = new JFileChooser();
	    FileNameExtensionFilter filter = new FileNameExtensionFilter("Bitmap Image", "BMP", "bmp");
	    chooser.setFileFilter(filter);
	    int returnVal = chooser.showOpenDialog(null);
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
	    	return chooser.getSelectedFile().getPath();
	    }
	    return null;
	}

	/**
	 * Browses for any file
	 *
	 * @return Either the path to the selected file or null if canceled
	 */
	public static String browseFile() {
		JFileChooser chooser = new JFileChooser();
	    int returnVal = chooser.showOpenDialog(null);
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
	    	return chooser.getSelectedFile().getPath();
	    }
	    return null;
	}

	/**
	 * Browses for a directory
	 *
	 * @return Either the path to the selected directory or null if canceled
	 */
	public static String browseOutputDir() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    int returnVal = chooser.showOpenDialog(null);
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
	    	return chooser.getSelectedFile().getPath();
	    }
	    return null;
	}
}
