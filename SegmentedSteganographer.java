// By: Justin Spedding & Andrew Miller

public abstract class SegmentedSteganographer {

	protected int segments;
	protected int currentSegment; // The next segment to encode

	/**
	 * Are there any unprocessed segments left?
	 *
	 * @return True if at least one segment remains, false otherwise
	 */
	public boolean hasNext() {
		return currentSegment < segments;
	}

	/**
	 * How many segments have been completed?
	 *
	 * @return The number of completed segments
	 */
	public int getCompletedSegments() {
		return currentSegment;
	}

	/**
	 * How many total segments are there?
	 *
	 * @return The total number of segments
	 */
	public int getTotalSegments() {
		return segments;
	}
}
