import java.util.Arrays;

class StateSimulator extends State {
	public static final int INDEX_NUMHOLES 				= 0;
	public static final int INDEX_COL_TRANSITIONS 		= 1;
	public static final int INDEX_HOLE_DEPTH 			= 2;
	public static final int INDEX_NUM_ROWS_WITH_HOLE 	= 3;
	public static final int INDEX_ERODED_PIECE_CELLS 	= 4;
	public static final int INDEX_LANDING_HEIGHT 		= 5;
	public static final int INDEX_ROW_TRANSITIONS 		= 6;
	public static final int INDEX_CUMULATIVE_WELLS 		= 7;

	public static final int NUM_FEATURES = 8;

	private int[][] copy_field = new int[State.ROWS][State.COLS];
	private int[] copy_top = new int[State.COLS];
	private int copy_nextPiece;
	private boolean hasMadeMove = false;

	private int[] features;

	private int numRowsHadBeenCleared; // this is used ONLY to calculate the difference, and meaningless in itself since we have no control over cleared attribute which is private

	public StateSimulator() {
		super();
		features = new int[NUM_FEATURES];
		this.label = null;
	}

	@Override
	public void makeMove(int move) {
		if (nextPiece == -1) // assertion must have -ea switch turned on in JVM
			throw new IllegalArgumentException("next piece is set to -1");
		copy_nextPiece = nextPiece;

		/// save all the member variables
		// irrelevant variables: turn, cleared, hasLost
		copy_top = Arrays.copyOf(this.getTop(), State.COLS);
		copyField(this.getField(), copy_field);
		numRowsHadBeenCleared = getRowsCleared();
		if (numRowsHadBeenCleared < 0) {
			System.out.println("Integer overflow");
			System.exit(1);
		}

		super.makeMove(move);
		hasMadeMove = true;
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
		System.arraycopy(copy_top, 0, this.getTop(), 0, this.getTop().length);
		copyField(copy_field, this.getField());
		nextPiece = copy_nextPiece; // to revert back from random.int() called in makeMove()
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

	public int[] getFeaturesArray() {
		computeFeatures();
		return this.features;
	}

	private void computeFeatures() {
		getFeature0to5();
		getRowTransitions();
		getCumulativeWells();
	}

	private void getFeature0to5() {
		// for number of holes
		int numHoles = 0;

		// for sum of hole depths
		int sumHoleDepth = 0;

		// for number of rows with holes
		boolean[] rowHasHoles = new boolean[State.ROWS];

		// for column transition
		int transitionCount = 0;
		boolean isFilled = true; // off-by-one, even if wrong

		// for use in getRowTransitions
		int maxHeight = -1;

		// for use in landingheight
		int maxLandingHeight = 0;
		int thisTurn = getTurnNumber();
		if (thisTurn < 0) {
			System.out.println("Integer overflow");
			System.exit(1);
		}

		// for use in erodedPieceCells
		// all pieces consists of 4 blocks
		int totalSize = 4;
		int numBlocksLeft = 0;

		int[] top = getTop();
		int[][] field = getField();

		for (int col = 0; col < State.COLS; col++) {
			// for each column, start with the top cell
			int topRow = top[col]-1;
			maxHeight = Math.max(maxHeight, topRow);

			if (topRow >= 0 && field[topRow][col] == thisTurn) // field[row][col] = turn, if not empty
				maxLandingHeight = Math.max(maxLandingHeight, topRow);

			int numHolesForColumn = 0;
			for (int row = topRow-1; row >= 0; row--) {
				if (field[row][col] == 0) {
					sumHoleDepth += (topRow - numHolesForColumn - row);
					numHolesForColumn++;
					rowHasHoles[row] = true;
					if (isFilled) {
						transitionCount++;
						isFilled = false;
					}
				} else if (field[row][col] == thisTurn) {
					numBlocksLeft++;
					if (!isFilled) {
						transitionCount++;
						isFilled = true;
					}
				} else {
					if (!isFilled) {
						transitionCount++;
						isFilled = true;
					}
				}
			}
			numHoles += numHolesForColumn;
		}

		features[INDEX_NUMHOLES] = numHoles;
		features[INDEX_HOLE_DEPTH] = sumHoleDepth;
		features[INDEX_COL_TRANSITIONS] = transitionCount;
		features[INDEX_LANDING_HEIGHT] = maxLandingHeight;

		features[INDEX_NUM_ROWS_WITH_HOLE] = 0;
		for (boolean rowHasHole : rowHasHoles) {
			if (rowHasHole)
				features[INDEX_NUM_ROWS_WITH_HOLE] += 1;
		}

		// make sure that this function is called before getRowTransitions
		features[INDEX_ROW_TRANSITIONS] = maxHeight;

		int numRowsCleared = getRowsCleared() - numRowsHadBeenCleared;
		features[INDEX_ERODED_PIECE_CELLS] = numRowsCleared * (totalSize - numBlocksLeft);
	}

	private void getRowTransitions() {
		int maxHeight = features[INDEX_ROW_TRANSITIONS]; // this is just a hack to save a scan of array
		int transitionCount = 0;
		int[][] field = getField();
		boolean isFilled = field[maxHeight][0] == 1;

		for (int col = 0; col < State.COLS; col++) {
			for (int row = 0; row <= maxHeight; row++) {
				if (field[row][col] == 0) {
					if (isFilled)
						transitionCount += 1;
					isFilled = false;
				} else {
					if (!isFilled)
						transitionCount += 1;
					isFilled = true;
				}
			}
		}
		features[INDEX_ROW_TRANSITIONS] = transitionCount;
	}

	private void getCumulativeWells() {
		int sumWellDepths = 0;
		int[] top = getTop();
		for (int col = 1; col < top.length - 1; col++) {
			if (top[col - 1] > top[col] && top[col] < top[col + 1]) {
				int depth = Math.min(top[col-1] - top[col], top[col+1] - top[col]);
				sumWellDepths += depth;
			}
		}

		// check left- and right-most column, treating the wall as block of maxHeight.
		if (top[0] < top[1]) {
			sumWellDepths += top[1] - top[0];
		}

		if (top[top.length - 1] < top[top.length - 2]) {
			sumWellDepths += (top[top.length - 2] - top[top.length - 1]);
		}

		features[INDEX_CUMULATIVE_WELLS] = sumWellDepths;
	}

}
