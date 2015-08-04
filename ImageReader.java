import java.awt.image.BufferedImage;

//By: Justin Spedding & Andrew Miller

public class ImageReader extends ImageManipulator {

	/**
	 * Constructs an image reader
	 *
	 * @param hostImage
	 * 			The host image to read from
	 * @param password
	 * 			The password used to store the stego
	 * @throws ImageOverflowException
	 * 			Throws if the internal algorithm tries to write to a pixel that does not exist
	 */
	public ImageReader(BufferedImage hostImage, String password) throws ImageOverflowException {
		super(hostImage, password);
	}

	/**
	 *	Reads and returns the next single bit from the image
	 *
	 * @return The next bit from the image
	 * @throws ImageOverflowException
	 * 			Throws if the internal algorithm tries to write to a pixel that does not exist
	 */
	public int readBit() throws ImageOverflowException {
		int bit = (hostImage.getRGB(x, y) >> (currentBit + ((x + y + rgb) % 3) * 8)) & 1; // Get the bit
		nextPixel(); // Advance to the next bit
		return bit;
	}
}
