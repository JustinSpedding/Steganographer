import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;

//By: Justin Spedding & Andrew Miller

public class SegmentedDecoder extends SegmentedSteganographer {

	private ImageReader imageReader; // The host image manipulator
	protected RandomAccessFile stego; // The stego to decode
	protected int[] segmentSizes; // The number of bytes to encode in each segment

	/**
	 * Constructs a decoder object that breaks the work up into multiple segments.
	 *
	 * @param hostImagePath
	 * 			The path to the host image
	 * @param outputDir
	 * 			The directory to save the hidden file
	 * @param password
	 * 			The password used to store the stego
	 * @param segments
	 * 			The number of segments to break the work up into
	 * @throws FileNotFoundException
	 * 			Throws if the filePath is invalid
	 * @throws IOException
	 * 			Throws if there is an IO error
	 * @throws CannotEncodeException
	 * 			Throws if the file cannot fit in the host image
	 * @throws ImageOverflowException
	 * 			Throws if the internal algorithm tries to write to a pixel that does not exist
	 */
	public SegmentedDecoder(String hostImagePath, String outputDir, String password) throws FileNotFoundException, IOException, IIOException, CannotDecodeException, ImageOverflowException {
		segments = 100;
		imageReader = new ImageReader(ImageIO.read(new File(hostImagePath)), password); // Initialize the host image manipulator
		if (Steganographer.canDecode(imageReader.getHostImage(), password)) { // If the image contains an encoded file
			ImageRW.readString(imageReader, Steganographer.getMarker().length()); // Skip the marker
			int fileLength = ImageRW.readInt(imageReader); // Get the file length
			String fileName = ImageRW.readString(imageReader, ImageRW.readInt(imageReader)); // Get the file name
			stego = new RandomAccessFile(new File(outputDir + File.separator + fileName), "rw"); // Create the file to be created
			stego.setLength(0);
			segmentSizes = new int[segments];
			int remainingBytes = fileLength; // Get the total number of bytes to encode
			int segmentLength = (int) (((double) remainingBytes) / ((double) segments)); // Get the length of each segment
			for (int i = 0; i < segments - 1; i++) { // Set the byte lengths of all but the last segment
				segmentSizes[i] = segmentLength;
				remainingBytes -= segmentLength;
			}
			segmentSizes[segmentSizes.length - 1] = remainingBytes; // Put the remaining bytes in the last segment
			currentSegment = 0; // Start with the first segment
		} else {
			throw new CannotDecodeException();
		}
	}

	/**
	 * Processes the next decoding segment
	 *
	 * @throws IOException
	 * 			Throws if there is an IO error
	 * @throws ImageOverflowException
	 * 			Throws if the internal algorithm tries to read from a pixel that does not exist
	 * @throws NoSuchSegmentException
	 * 			Throws if there are no segments left to process
	 */
	public void nextSegment() throws IOException, IIOException, ImageOverflowException, NoSuchSegmentException {
		if (hasNext()) {
			int remainingBytes = segmentSizes[currentSegment];
			for (; remainingBytes != 0; remainingBytes--) {
				stego.write(ImageRW.readByte(imageReader));
			}
			currentSegment++;
		} else {
			throw new NoSuchSegmentException();
		}
	}
}