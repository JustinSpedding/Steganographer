import java.awt.image.BufferedImage;

// By: Justin Spedding & Andrew Miller

public class ImageWriter extends ImageManipulator {

	/**
	 * Constructs an image writer
	 *
	 * @param hostImage
	 * 			The host image to write to
	 * @param password
	 * 			The password used to store the stego
	 * @throws ImageOverflowException
	 * 			Throws if the internal algorithm tries to write to a pixel that does not exist
	 */
	public ImageWriter(BufferedImage hostImage, String password) throws ImageOverflowException {
		super(hostImage, password);
	}

	/**
	 *	Writes a single bit to the next bit in the image
	 *
	 * @throws ImageOverflowException
	 * 			Throws if the internal algorithm tries to write to a pixel that does not exist
	 */
	public void writeBit(int bit) throws ImageOverflowException {
		if (bit == 0) {
			hostImage.setRGB(x, y, (hostImage.getRGB(x, y) & ~(1 << (currentBit + ((x + y + rgb) % 3) * 8)))); // Set the bit to 0
		} else {
			hostImage.setRGB(x, y, (hostImage.getRGB(x, y) | (1 << (currentBit + ((x + y + rgb) % 3) * 8)))); // set the bit to 1
		}
		nextPixel();
	}
}
