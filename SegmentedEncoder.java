import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;

// By: Justin Spedding & Andrew Miller

public class SegmentedEncoder extends SegmentedSteganographer {

	private ImageWriter imageWriter; // The host image manipulator
	protected RandomAccessFile stego; // The stego to encode
	protected int[] segmentSizes; // The number of bytes to encode in each segment

	/**
	 * Constructs an encoder object that breaks the work up into multiple segments.
	 *
	 * @param hostImagePath
	 * 			The path to the host image
	 * @param stegoPath
	 * 			The path to the stego
	 * @param password
	 * 			The password used to store the stego
	 * @param segments
	 * 			The number of segments to break the work up into
	 * @throws FileNotFoundException
	 * 			Throws if the file path is invalid
	 * @throws IOException
	 * 			Throws if there is an IO error
	 * @throws CannotEncodeException
	 * 			Throws if the stego cannot fit in the host image
	 * @throws ImageOverflowException
	 * 			Throws if the internal algorithm tries to write to a pixel that does not exist
	 */
	public SegmentedEncoder(String hostImagePath, String stegoPath, String password) throws FileNotFoundException, IOException, IIOException, CannotEncodeException, ImageOverflowException {
		segments = 100;
		imageWriter = new ImageWriter(ImageIO.read(new File(hostImagePath)), password); // Initialize the host image manipulator
		File stegoFile = new File(stegoPath);
		stego = new RandomAccessFile(stegoFile, "r"); // Initialize the file to be encoded
		String fileName = stegoFile.getName(); // Get the name of the file
		if (Steganographer.canEncode(imageWriter.getHostImage(), stego, fileName.length())) { // If the file will fit in the image
			segmentSizes = new int[segments];
			int remainingBytes = (int) stego.length(); // Get the total number of bytes to encode
			int segmentLength = (int) (((double) remainingBytes) / ((double) segments)); // Get the length of each segment
			for (int i = 0; i < segments - 1; i++) { // Set the byte lengths of all but the last segment
				segmentSizes[i] = segmentLength;
				remainingBytes -= segmentLength;
			}
			segmentSizes[segmentSizes.length - 1] = remainingBytes; // Put the remaining bytes in the last segment
			currentSegment = 0; // Start with the first segment
			ImageRW.writeString(imageWriter, Steganographer.getMarker()); // Mark the image as having a hidden file
			ImageRW.writeInt(imageWriter, (int) stego.length()); // Write the size of the file
			ImageRW.writeInt(imageWriter, fileName.length()); // Write the length of the file name
			ImageRW.writeString(imageWriter, fileName); // Write the file's name
		} else {
			throw new CannotEncodeException();
		}
	}

	/**
	 * Processes the next encoding segment
	 *
	 * @throws IOException
	 * 			Throws if there is an IO error
	 * @throws ImageOverflowException
	 * 			Throws if the internal algorithm tries to write to a pixel that does not exist
	 * @throws NoSuchSegmentException
	 * 			Throws if there are no segments left to process
	 */
	public void nextSegment() throws IOException, IIOException, ImageOverflowException, NoSuchSegmentException {
		if (hasNext()) {
			int remainingBytes = segmentSizes[currentSegment];
			for (; remainingBytes != 0; remainingBytes--) {
				ImageRW.writeByte(imageWriter, (byte) stego.read());
			}
			currentSegment++;
		} else {
			throw new NoSuchSegmentException();
		}
	}

	/**
	 * Returns the encoded image
	 * If not all of the segments have been completed, this will return an incomplete image.
	 *
	 * @return The encoded image, whether it is finished or not
	 */
	public BufferedImage getEncodedImage() {
		return imageWriter.getHostImage();
	}
}