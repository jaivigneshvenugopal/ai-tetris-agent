public class StateSimulator2 {
    public static final int COLS = 10;
    public static final int ROWS = 21;
    public static final int N_PIECES = 7;

    public boolean lost = false;

    //current turn
    public int turn = 0;
    public int cleared = 0;

    //each square in the grid - 0 means empty - other values mean the turn it was placed
    private int[][] field = new int[ROWS][COLS];
    //top row+1 of each column
    //0 means empty
    private int[] top = new int[COLS];

    //number of next piece
    public int nextPiece;

    //all legal moves - first index is piece type - then a list of 2-length arrays
    protected static int[][][] legalMoves = new int[N_PIECES][][];

    //indices for legalMoves
    public static final int ORIENT = 0;
    public static final int SLOT = 1;

    //possible orientations for a given piece type
    protected static int[] pOrients = {1, 2, 4, 4, 4, 2, 2};

    //the next several arrays define the piece vocabulary in detail
    //width of the pieces [piece ID][orientation]
    protected static int[][] pWidth = {
            {2},
            {1, 4},
            {2, 3, 2, 3},
            {2, 3, 2, 3},
            {2, 3, 2, 3},
            {3, 2},
            {3, 2}
    };
    //height of the pieces [piece ID][orientation]
    private static int[][] pHeight = {
            {2},
            {4, 1},
            {3, 2, 3, 2},
            {3, 2, 3, 2},
            {3, 2, 3, 2},
            {2, 3},
            {2, 3}
    };
    private static int[][][] pBottom = {
            {{0, 0}},
            {{0}, {0, 0, 0, 0}},
            {{0, 0}, {0, 1, 1}, {2, 0}, {0, 0, 0}},
            {{0, 0}, {0, 0, 0}, {0, 2}, {1, 1, 0}},
            {{0, 1}, {1, 0, 1}, {1, 0}, {0, 0, 0}},
            {{0, 0, 1}, {1, 0}},
            {{1, 0, 0}, {0, 1}}
    };
    private static int[][][] pTop = {
            {{2, 2}},
            {{4}, {1, 1, 1, 1}},
            {{3, 1}, {2, 2, 2}, {3, 3}, {1, 1, 2}},
            {{1, 3}, {2, 1, 1}, {3, 3}, {2, 2, 2}},
            {{3, 2}, {2, 2, 2}, {2, 3}, {1, 2, 1}},
            {{1, 2, 2}, {3, 2}},
            {{2, 2, 1}, {2, 3}}
    };

    //initialize legalMoves
    {
        //for each piece type
        for (int i = 0; i < N_PIECES; i++) {
            //figure number of legal moves
            int n = 0;
            for (int j = 0; j < pOrients[i]; j++) {
                //number of locations in this orientation
                n += COLS + 1 - pWidth[i][j];
            }
            //allocate space
            legalMoves[i] = new int[n][2];
            //for each orientation
            n = 0;
            for (int j = 0; j < pOrients[i]; j++) {
                //for each slot
                for (int k = 0; k < COLS + 1 - pWidth[i][j]; k++) {
                    legalMoves[i][n][ORIENT] = j;
                    legalMoves[i][n][SLOT] = k;
                    n++;
                }
            }
        }
    }

    public static final int INDEX_NUMHOLES = 0;
    public static final int INDEX_COL_TRANSITIONS = 1;
    public static final int INDEX_HOLE_DEPTH = 2;
    public static final int INDEX_NUM_ROWS_WITH_HOLE = 3;
    public static final int INDEX_ERODED_PIECE_CELLS = 4; // the only one to maximize.
    public static final int INDEX_LANDING_HEIGHT = 5;
    public static final int INDEX_ROW_TRANSITIONS = 6;
    public static final int INDEX_CUMULATIVE_WELLS = 7;

    public static final int NUM_FEATURES = 8;

    private int numRowsHadBeenCleared;
    private int[] features;

    public int[][] getField() {
        return field;
    }

    public int[] getTop() {
        return top;
    }

    public boolean hasLost() {
        return lost;
    }

    public int getRowsCleared() {
        return cleared;
    }

    public int getTurnNumber() {
        return turn;
    }

    public StateSimulator2() {
        this.features = new int[NUM_FEATURES];
    }

    public void copyState(State startState) {
        copyField(startState.getField(), field);
        this.nextPiece = startState.nextPiece;
        System.arraycopy(startState.getTop(), 0, top, 0, startState.getTop().length);
        this.turn = startState.getTurnNumber();
        this.lost = false;
        this.numRowsHadBeenCleared = this.cleared = startState.getRowsCleared();
    }

    /**
     * @param source      the location from which to copy
     * @param destination the location to copy the source
     */
    private void copyField(int[][] source, int[][] destination) {
        for (int i = 0; i < source.length; i++) {
            System.arraycopy(source[i], 0, destination[i], 0, source[0].length);
        }
    }

    //make a move based on the move index - its order in the legalMoves list
    public void makeMove(int move) {
        makeMove(legalMoves[nextPiece][move]);
    }

    //make a move based on an array of orient and slot
    public void makeMove(int[] move) {
        makeMove(move[ORIENT], move[SLOT]);
    }

    //returns false if you lose - true otherwise
    public boolean makeMove(int orient, int slot) {
        turn++;
        //height if the first column makes contact
        int height = top[slot] - pBottom[nextPiece][orient][0];
        //for each column beyond the first in the piece
        for (int c = 1; c < pWidth[nextPiece][orient]; c++) {
            try {
                height = Math.max(height, top[slot + c] - pBottom[nextPiece][orient][c]);
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("slot " + slot);
                System.out.println("nextPiece " + nextPiece);
                System.out.println("orient " + orient);
                System.out.println("c " + c);
            }
        }

        //check if game ended
        if (height + pHeight[nextPiece][orient] >= ROWS) {
            lost = true;
            return false;
        }

        //for each column in the piece - fill in the appropriate blocks
        for (int i = 0; i < pWidth[nextPiece][orient]; i++) {

            //from bottom to top of brick
            for (int h = height + pBottom[nextPiece][orient][i]; h < height + pTop[nextPiece][orient][i]; h++) {
                field[h][i + slot] = turn;
            }
        }

        //adjust top
        for (int c = 0; c < pWidth[nextPiece][orient]; c++) {
            top[slot + c] = height + pTop[nextPiece][orient][c];
        }

        int rowsCleared = 0;

        //check for full rows - starting at the top
        for (int r = height + pHeight[nextPiece][orient] - 1; r >= height; r--) {
            //check all columns in the row
            boolean full = true;
            for (int c = 0; c < COLS; c++) {
                if (field[r][c] == 0) {
                    full = false;
                    break;
                }
            }
            //if the row was full - remove it and slide above stuff down
            if (full) {
                rowsCleared++;
                cleared++;
                //for each column
                for (int c = 0; c < COLS; c++) {

                    //slide down all bricks
                    for (int i = r; i < top[c]; i++) {
                        field[i][c] = field[i + 1][c];
                    }
                    //lower the top
                    top[c]--;
                    while (top[c] >= 1 && field[top[c] - 1][c] == 0) top[c]--;
                }
            }
        }

        return true;
    }

    public int[] getFeaturesArray() {
        computeFeatures();
        return this.features;
    }

    private void computeFeatures() {
        getFeature0to5();
        getColTransitions();
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

        for (int col = 0; col < State.COLS; col++) {
            // for each column, start with the top cell
            int topRow = top[col] - 1;

            if (topRow >= 0 && field[topRow][col] == thisTurn) // field[row][col] = turn, if not empty
                maxLandingHeight = Math.max(maxLandingHeight, topRow);

            int numHolesForColumn = 0;
            for (int row = topRow - 1; row >= 0; row--) {
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

        int numRowsCleared = getRowsCleared() - numRowsHadBeenCleared;
        features[INDEX_ERODED_PIECE_CELLS] = numRowsCleared * (totalSize - numBlocksLeft);
    }

    private void getColTransitions() {
//        int maxHeight = features[INDEX_ROW_TRANSITIONS]; // this is just a hack to save a scan of array
//        int transitionCount = 0;
//        boolean isFilled = field[maxHeight][0] == 1;
//
//        for (int col = 0; col < State.COLS; col++) {
//            for (int row = 0; row <= maxHeight; row++) {
//                if (field[row][col] == 0) {
//                    if (isFilled)
//                        transitionCount += 1;
//                    isFilled = false;
//                } else {
//                    if (!isFilled)
//                        transitionCount += 1;
//                    isFilled = true;
//                }
//            }
//        }
//        features[INDEX_ROW_TRANSITIONS] = transitionCount;
        boolean isFilled = field[0][0] > 0;
        int transitionCount = 0;
        for (int col = 0; col < COLS; col++) {
            for (int row = 0; row < ROWS; row++) {
                if (field[row][col] == 0) {
                    if (isFilled) {
                        transitionCount += 1;
                        isFilled = false;
                    }
                } else {
                    if (!isFilled) {
                        transitionCount += 1;
                        isFilled = true;
                    }
                }
            }
        }

        features[INDEX_COL_TRANSITIONS] = transitionCount;
    }

    private void getRowTransitions() {
        boolean isFilled = field[0][0] > 0;
        int transitionCount = 0;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
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
        for (int col = 1; col < top.length - 1; col++) {
            if (top[col - 1] > top[col] && top[col] < top[col + 1]) {
                int depth = Math.min(top[col - 1] - top[col], top[col + 1] - top[col]);
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

    private static void getNumHoles() {

    }

}
