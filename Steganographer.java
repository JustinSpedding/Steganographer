import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;

// By: Justin Spedding & Andrew Miller

public class Steganographer {

	public static final String version = "v0.7"; // Version number
	private static String marker = "SECRET"; // The string that marks images that contain an encoded file
	private static int workerCount; // The number of encode/decode operations currently running

	/**
	 * Checks if a stego can be encoded into a host image
	 *
	 * @param hostImage
	 * 			The BufferedImage of the host image
	 * @param stegoPath
	 * 			The path to the stego
	 * @return True if encoding will work, false if not
	 * @throws FileNotFoundException
	 * 			Throws if one of the files was not found
	 * @throws IOException
	 * 			Throws if an IO error occurred
	 */
	public static boolean canEncode(BufferedImage hostImage, RandomAccessFile stego, int fileNameLength) throws IOException {
		if (hostImage != null && stego != null && fileNameLength > 0) { // If both files exist and the file name is valid
			if ((marker.length() * 2) + (8) + (fileNameLength * 2) + (stego.length()) <= getCapacity(hostImage)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if a host image contains a valid stego
	 *
	 * @param hostImage
	 * 			The BufferedImage of the host image
	 * @param password
	 * 			The password used to store the stego
	 * @return True if the host image contains a valid hidden file, false if not
	 * @throws ImageOverflowException
	 * 			Throws if the internal algorithm tries to write to a pixel that does not exist
	 */
	public static boolean canDecode(BufferedImage hostImage, String password) throws ImageOverflowException {
		int capacity = getCapacity(hostImage); // How many bytes can be stored in this image
		if (capacity > (marker.length() * 2) + (8)) { // If the host image is big enough to store a file
			ImageReader imageReader = new ImageReader(hostImage, password); // Initialize the host image manipulator
			String secret = ImageRW.readString(imageReader, marker.length()); // Get the marker
			if (secret.equals(marker)) { // If it is correct
				int fileSize = ImageRW.readInt(imageReader); // Get the file length
				if (fileSize >= 0) {
					int fileNameLength = ImageRW.readInt(imageReader); // Get the file name length
					if (fileNameLength > 0 && fileNameLength < 256) {
						if ((marker.length() * 2) + (8) + (fileNameLength * 2) + (fileSize) <= capacity) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Returns a string containing info about what may be contained within a host image
	 *
	 * @param hostImage
	 * 			The BufferedImage of the host image
	 * @param password
	 * 			The password used to store the stego
	 * @return A string containing info about what may be contained within the host image
	 * @throws ImageOverflowException
	 * 			Throws if the internal algorithm tries to write to a pixel that does not exist
	 */
	public static String getInfo(BufferedImage hostImage, String password) throws ImageOverflowException {
		int capacity = getCapacity(hostImage); // How many bytes can be stored in this image
		if (capacity > (marker.length() * 2) + (8)) { // If the host image is big enough to store a file
			ImageReader image = new ImageReader(hostImage, password); // Initialize the host image manipulator
			String secret = ImageRW.readString(image, marker.length()); // Get the first 6 chars
			if (secret.equals(marker)) { // If they are correct
				int fileSize = ImageRW.readInt(image); // Get the file length
				if (fileSize >= 0) {
					int fileNameLength = ImageRW.readInt(image); // Get the file name length
					if (fileNameLength > 0 && fileNameLength < 256) {
						String fileName = ImageRW.readString(image, fileNameLength); // Get the file name
						if ((marker.length() * 2) + (8) + (fileNameLength * 2) + (fileSize) <= capacity) {
							return "Stego file name: \"" + fileName + "\",  Size: " + fileSizeToString(fileSize);
						}
					}
				}
			}
		}
		return "The host image does not contain a valid stego.";
	}

	private static String fileSizeToString(double fileSize) {
		if (fileSize > 1024) {
			fileSize /= 1024;
			if (fileSize > 1024) {
				fileSize /= 1024;
				if (fileSize > 1024) {
					fileSize /= 1024;
					if (fileSize > 1024) {
						fileSize /= 1024;
						return new DecimalFormat("#.##").format(fileSize) + " TiB";
					}
					return new DecimalFormat("#.##").format(fileSize) + " GiB";
				}
				return new DecimalFormat("#.##").format(fileSize) + " MiB";
			}
			return new DecimalFormat("#.##").format(fileSize) + " KiB";
		}
		return new DecimalFormat("#.##").format(fileSize) + " Bytes";
	}

	/**
	 * Returns the number of bytes that can be stored in a host image
	 *
	 * @param hostImage
	 * 			The BufferedImage of the host image
	 * @return The number of bytes that can be stored in the host image
	 */
	public static int getCapacity(BufferedImage hostImage) {
		return (hostImage.getWidth() * hostImage.getHeight() * 3);
	}
	/**
	 * Checks if an output directory path is valid
	 *
	 * @param outputDirPath
	 * 			The path to the output directory
	 * @return True if the path is valid, false if not
	 */
	public static boolean checkOutputDirPath(String outputDirPath) {
		try {
			File temp = new File(outputDirPath);
			return temp.isDirectory();
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Checks if an output image path is valid
	 *
	 * @param outputImagePath
	 * 			The path to the output image
	 * @return True if the path is valid, false if not
	 */
	public static boolean checkOutputImagePath(String outputImagePath) {
		try {
			File temp = new File(outputImagePath);
			return (!temp.isDirectory()) && temp.getParentFile().isDirectory();
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Returns the string that marks images that contain a stego
	 *
	 * @return The string that marks images that contain a stego
	 */
	public static String getMarker() {
		return marker;
	}

	/**
	 * Sets the string that marks images that contain a stego
	 *
	 * @param marker
	 * 			The string that marks images that contain a stego
	 */
	public static void setMarker(String marker) {
		Steganographer.marker = marker;
	}

	/**
	 * Increments the number of workers currently running
	 */
	public static void addWorker() {
		workerCount++;
	}

	/**
	 * Decrements the number of workers currently running
	 */
	public static void removeWorker() {
		workerCount --;
	}

	/**
	 * Returns the number of workers currently running
	 *
	 * @return The number of workers currently running
	 */
	public static int getWorkerCount() {
		return workerCount;
	}
}
