import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Stack;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

// By: Justin Spedding & Andrew Miller

public class SegmentedAnalyzer extends SegmentedSteganographer {

	private BufferedImage hostImage;
	private int[][] values;

	/**
	 * Constructs an analyzer object that breaks the work up into multiple segments.
	 *
	 * @param hostImagePath
	 * 			The path to the host image
	 * @throws FileNotFoundException
	 * 			Throws if the file path is invalid
	 * @throws IOException
	 * 			Throws if there is an IO error
	 */
	public SegmentedAnalyzer(String hostImagePath) throws FileNotFoundException, IOException, IIOException {
		segments = 21;
		hostImage = ImageIO.read(new File(hostImagePath));
		values = new int[3][7];
	}

	/**
	 * Processes the next analysis segment
	 *
	 * @throws IOException
	 * 			Throws if there is an IO error
	 * @throws NoSuchSegmentException
	 * 			Throws if there are no segments left to process
	 */
	public void nextSegment() throws IOException, IIOException, NoSuchSegmentException {
		if (hasNext()) {
			int bit = currentSegment / 3;
			int color = currentSegment % 3;
			int chunkCount = 0;
			boolean[][] checked = new boolean[hostImage.getWidth()][hostImage.getHeight()];
			int[][] map = new int[hostImage.getWidth()][hostImage.getHeight()];
			for (int i = 0; i < hostImage.getWidth(); i++) {
				for (int j = 0; j < hostImage.getHeight(); j++) {
					checked[i][j] = false;
					map[i][j] = (hostImage.getRGB(i, j) >> ((color * 8) + bit)) & 1;
				}
			}
			for (int i = 0; i < hostImage.getWidth(); i++) {
				for (int j = 0; j < hostImage.getHeight(); j++) {
					if (!checked[i][j]) {
						chunkCount++;
						removeChunk(i, j, color, bit, map[i][j], checked, map);
					}
				}
			}
			values[color][bit] = chunkCount;
			currentSegment++;
		} else {
			throw new NoSuchSegmentException();
		}
	}

	private int removeChunk(int i, int j, int color, int bit, int base, boolean[][] checked, int[][] map) {
		int chunkSize = 0;
		Stack<Integer> stack = new Stack<Integer>();
		stack.push(i);
		stack.push(j);
		while (!stack.isEmpty()) {
			j = stack.pop();
			i = stack.pop();
			if (map[i][j] == base && !checked[i][j]) {
				chunkSize++;
				checked[i][j] = true;
				if (i > 0 && !checked[i - 1][j]) {
					stack.push(i - 1);
					stack.push(j);
				}
				if (i < hostImage.getWidth() - 1 && !checked[i + 1][j]) {
					stack.push(i + 1);
					stack.push(j);
				}
				if (j > 0 && !checked[i][j - 1]) {
					stack.push(i);
					stack.push(j - 1);
				}
				if (j < hostImage.getHeight() - 1 && !checked[i][j + 1]) {
					stack.push(i);
					stack.push(j + 1);
				}
			}
		}
		return chunkSize;
	}

	/**
	 * Returns the boolean value of whether the host image contains a stego or not
	 * If not all of the segments have been completed, this will return false by default.
	 *
	 * @return True if the host image contains a stego, false if not
	 */
	public Boolean getValue() {
		if (!hasNext()) {
			int[] total = new int[7];
			for (int i = 0; i < 7; i++) {
				total[i] = values[0][i] + values[1][i] + values[2][i];
			}
			double[] multiplier = new double[6];
			for (int i = 1; i < 7; i++) {
				multiplier[i - 1] = (double) total[i] / (double) total[i - 1];
			}
			double[] finalValues = new double[5];
			for (int i = 1; i < 6; i++) {
				finalValues[i - 1] = multiplier[i - 1] - multiplier[i];
				if (finalValues[i - 1] < -.01 || finalValues[i - 1] > .2 || Math.abs(finalValues[i - 1]) < .001) {
					return true;
				}
			}
		}
		return false;
	}
}