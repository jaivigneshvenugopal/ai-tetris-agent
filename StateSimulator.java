import java.util.Arrays;

class StateSimulator extends State {
	private int[][] copy_field = new int[State.ROWS][State.COLS];
	private int[] copy_top = new int[State.COLS];
	private boolean hasMadeMove = false;

	public StateSimulator() {
		super();
		this.label = null;
	}

	@Override
	public void makeMove(int move) {
		assert(nextPiece != -1); // if this is the case, setNextPiece is not yet called
		/// save all the member variables
		// irrelevant variables: turn, cleared, hasLost
		copy_top = Arrays.copyOf(this.getTop(), State.COLS);
		copyField(this.getField(), copy_field);

		super.makeMove(move);
		hasMadeMove = true;
		nextPiece = -1; // -1 to indicate that it has yet to synchronize this variable with the actual state
	}

	public void setNextPiece(int nextPiece) {
		this.nextPiece = nextPiece;
	}

	/**
	 * This is to make sure that we are clear about the piece we are simulating the move with.
	 * This should be called when we are done with one round of simulation with a piece.
	 */
	public void markSimulationDoneWithCurrentPiece() {
		nextPiece = -1;
	}

	/**
	 * resets 3 variables: nextPiece, field and top.
	 */
	public void resetMove() {
		if (!hasMadeMove)
			return;
		System.arraycopy(copy_top, copy_top.length, this.getTop(), 0, this.getTop().length);
		copyField(copy_field, this.getField());
		hasMadeMove = false;
		this.lost = false;
	}

	/**
	 * @param source the location from which to copy
	 * @param destination the location to copy the source
	 */
	private void copyField(int[][] source, int[][] destination) {
		assert(source.length == destination.length && source[0].length == destination[0].length);

		for (int i = 0; i < source.length; i++) {
			for (int j = 0; j < source[0].length; j++) {
				destination[i][j] = source[i][j];
			}
		}
	}

}
