import javax.imageio.IIOException;
import javax.swing.SwingWorker;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;

// By: Justin Spedding & Andrew Miller

public class EncodeWorker extends SwingWorker<BufferedImage, Void> {

	private SegmentedEncoder encoder;
	private int currentSegment;

	public EncodeWorker(String hostImagePath, String stegoPath, String password) throws FileNotFoundException, IOException, IIOException, CannotEncodeException, ImageOverflowException {
		this.encoder = new SegmentedEncoder(hostImagePath, stegoPath, password);
		currentSegment = 0;
	}

	public BufferedImage doInBackground() throws IOException, IIOException, ImageOverflowException, NoSuchSegmentException {
		while (encoder.hasNext() && !isCancelled()) {
			setProgress((currentSegment * 100) / encoder.getTotalSegments());
			encoder.nextSegment();
			currentSegment++;
		}
		setProgress(100);
		return encoder.getEncodedImage();
	}

	public void done() {

	}
}