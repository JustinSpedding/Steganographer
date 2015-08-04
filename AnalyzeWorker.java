import javax.imageio.IIOException;
import javax.swing.SwingWorker;
import java.io.FileNotFoundException;
import java.io.IOException;

// By: Justin Spedding & Andrew Miller

public class AnalyzeWorker extends SwingWorker<Boolean, Void> {

	private SegmentedAnalyzer analyzer;
	private int currentSegment;

	public AnalyzeWorker(String hostImagePath) throws FileNotFoundException, IOException, IIOException {
		this.analyzer = new SegmentedAnalyzer(hostImagePath);
		currentSegment = 0;
	}

	public Boolean doInBackground() throws IOException, IIOException, NoSuchSegmentException {
		while (analyzer.hasNext() && !isCancelled()) {
			setProgress((currentSegment * 100) / analyzer.getTotalSegments());
			analyzer.nextSegment();
			currentSegment++;
		}
		setProgress(100);
		return analyzer.getValue();
	}

	public void done() {

	}
}