import java.awt.image.BufferedImage;
import java.util.Random;

// By: Justin Spedding & Andrew Miller

public abstract class ImageManipulator {

	protected BufferedImage hostImage; // The host file to write to
	protected int x; // The x coordinate of the current pixel
	protected int y; // The y coordinate of the current pixel
	protected int rgb; // The color to access: 2 = red, 1 = green, 0 = blue
	protected int currentBit; // The least significant bit to access, 0 = last bit in byte, 7 = first bit in byte
	protected int index; // The index of the current pixel in the pixel array
	protected int[] pixelArray; // The order in which to access pixels

	protected ImageManipulator(BufferedImage hostImage, String password) {
		this.hostImage = hostImage;
		generatePixelArray(password.hashCode());
		index = 0;
		rgb = 0;
		currentBit = 0;
		x = pixelArray[index] % hostImage.getWidth();
		y = pixelArray[index] / hostImage.getWidth();
	}

	private void generatePixelArray(int seed) {
		pixelArray = new int[hostImage.getHeight() * hostImage.getWidth()]; // Create an array of indexes to all the pixels
		for (int i = pixelArray.length - 1; i >= 0; i--) {
			pixelArray[i] = i; // Add all of the indexes in order
		}
		Random random = new Random(seed);
		for (int i = pixelArray.length - 1; i != 0; i--) { // Shuffle the array
			int randomIndex = random.nextInt(i + 1);
			int temp = pixelArray[i];
			pixelArray[i] = pixelArray[randomIndex];
			pixelArray[randomIndex] = temp;
		}
	}

	protected void nextPixel() throws ImageOverflowException {
		index++;
		if (index >= pixelArray.length) { // Go to next rgb offset if necessary
			index = 0;
			rgb++;
			if (rgb > 2) { // Go to next least significant pixel if necessary
				rgb = 0;
				currentBit++;
				if (currentBit > 7) { // Throw exception if there is no next pixel
					throw new ImageOverflowException();
				}
			}
		}
		x = pixelArray[index] % hostImage.getWidth();
		y = pixelArray[index] / hostImage.getWidth();
	}

	/**
	 * Returns the host image
	 *
	 * @return The host image
	 */
	public BufferedImage getHostImage() {
		return hostImage;
	}
}
