import javax.imageio.IIOException;
import javax.swing.SwingWorker;
import java.io.FileNotFoundException;
import java.io.IOException;

// By: Justin Spedding & Andrew Miller

public class DecodeWorker extends SwingWorker<Void, Void> {

	private SegmentedDecoder decoder;
	private int currentSegment;

	public DecodeWorker(String hostImagePath, String outputDirPath, String password) throws FileNotFoundException, IOException, IIOException, CannotDecodeException, ImageOverflowException {
		this.decoder = new SegmentedDecoder(hostImagePath, outputDirPath, password);
		currentSegment = 0;
	}

	public Void doInBackground() throws IOException, IIOException, ImageOverflowException, NoSuchSegmentException {
		while (decoder.hasNext() && !isCancelled()) {
			setProgress((currentSegment * 100) / decoder.getTotalSegments());
			decoder.nextSegment();
			currentSegment++;
		}
		setProgress(100);
		return null;
	}

	public void done() {

	}
}