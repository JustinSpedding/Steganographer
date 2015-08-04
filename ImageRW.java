// By: Justin Spedding & Andrew Miller

public class ImageRW {

	/**
	 * Writes a byte to an image
	 *
	 * @param imageWriter
	 * 			The image writer to use
	 * @param byteToWrite
	 * 			The byte to write
	 * @throws ImageOverflowException
	 * 			Throws if the internal algorithm tries to write to a pixel that does not exist
	 */
	public static void writeByte(ImageWriter imageWriter, byte byteToWrite) throws ImageOverflowException {
		for (int i = 7; i >= 0; i--) {
			imageWriter.writeBit((byteToWrite >> i) & 1);
		}
	}

	/**
	 * Writes a char to an image
	 *
	 * @param imageWriter
	 * 			The image writer to use
	 * @param charToWrite
	 * 			The char to write
	 * @throws ImageOverflowException
	 * 			Throws if the internal algorithm tries to write to a pixel that does not exist
	 */
	public static void writeChar(ImageWriter image, char charToWrite) throws ImageOverflowException {
		for (int i = 15; i >= 0; i--) {
			image.writeBit((charToWrite >> i) & 1);
		}
	}

	/**
	 * Writes an int to an image
	 *
	 * @param imageWriter
	 * 			The image writer to use
	 * @param intToWrite
	 * 			The int to write
	 * @throws ImageOverflowException
	 * 			Throws if the internal algorithm tries to write to a pixel that does not exist
	 */
	public static void writeInt(ImageWriter imageWriter, int intToWrite) throws ImageOverflowException {
		for (int i = 31; i >= 0; i--) {
			imageWriter.writeBit((intToWrite >> i) & 1);
		}
	}

	/**
	 * Writes a long to an image
	 *
	 * @param imageWriter
	 * 			The image writer to use
	 * @param longToWrite
	 * 			The long to write
	 * @throws ImageOverflowException
	 * 			Throws if the internal algorithm tries to write to a pixel that does not exist
	 */
	public static void writeLong(ImageWriter imageWriter, long longToWrite) throws ImageOverflowException {
		for (int i = 63; i >= 0; i--) {
			imageWriter.writeBit((int) (longToWrite >> i) & 1);
		}
	}

	/**
	 * Writes a string to an image
	 *
	 * @param imageWriter
	 * 			The image writer to use
	 * @param stringToWrite
	 * 			The string to write
	 * @throws ImageOverflowException
	 * 			Throws if the internal algorithm tries to write to a pixel that does not exist
	 */
	public static void writeString(ImageWriter imageWriter, String stringToWrite) throws ImageOverflowException {
		char[] c = stringToWrite.toCharArray();
		for (int i = 0; i < c.length; i++) {
			writeChar(imageWriter, c[i]);
		}
	}

	/**
	 * Reads a byte from an image
	 *
	 * @param imageReader
	 * 			The image reader to use
	 * @throws ImageOverflowException
	 * 			Throws if the internal algorithm tries to write to a pixel that does not exist
	 */
	public static byte readByte(ImageReader imageReader) throws ImageOverflowException {
		int output = 0;
		for (int i = 0; i < 8; i++) {
			output = output * 2 + imageReader.readBit();
		}
		return (byte) output;
	}

	/**
	 * Reads a char from an image
	 *
	 * @param imageReader
	 * 			The image reader to use
	 * @throws ImageOverflowException
	 * 			Throws if the internal algorithm tries to write to a pixel that does not exist
	 */
	public static char readChar(ImageReader imageReader) throws ImageOverflowException {
		int output = 0;
		for (int i = 0; i < 16; i++) {
			output = output * 2 + imageReader.readBit();
		}
		return (char) output;
	}

	/**
	 * Reads an int from an image
	 *
	 * @param imageReader
	 * 			The image reader to use
	 * @throws ImageOverflowException
	 * 			Throws if the internal algorithm tries to write to a pixel that does not exist
	 */
	public static int readInt(ImageReader imageReader) throws ImageOverflowException {
		int output = 0;
		for (int i = 0; i < 32; i++) {
			output = output * 2 + imageReader.readBit();
		}
		return output;
	}

	/**
	 * Reads a long from an image
	 *
	 * @param imageReader
	 * 			The image reader to use
	 * @throws ImageOverflowException
	 * 			Throws if the internal algorithm tries to write to a pixel that does not exist
	 */
	public static long readLong(ImageReader imageReader) throws ImageOverflowException {
		long output = 0;
		for (int i = 0; i < 64; i++) {
			output = output * 2 + imageReader.readBit();
		}
		return output;
	}

	/**
	 * Reads a string from an image
	 *
	 * @param imageReader
	 * 			The image reader to use
	 * @throws ImageOverflowException
	 * 			Throws if the internal algorithm tries to write to a pixel that does not exist
	 */
	public static String readString(ImageReader imageReader, int length) throws ImageOverflowException {
		String output = "";
		for (int i = 0; i < length; i++) {
			output = output + readChar(imageReader);
		}
		return output;
	}
}
