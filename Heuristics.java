import java.util.Arrays;
import java.util.logging.Logger;

public class Heuristics {
    private static boolean DEBUG = false;
    private static int index = 0;
    public static final int NUM_FEATURES = 12;

    public static final int INDEX_NUMHOLES = index++;
    public static final int INDEX_HEIGHT_DIFF = index++;
    public static final int INDEX_MAX_HEIGHT = index++;
    public static final int INDEX_ROWS_CLEARED = index++;
    public static final int INDEX_LOST = index++;
    public static final int INDEX_COL_TRANSITIONS = index++;
    public static final int INDEX_HOLE_DEPTH = index++;
    public static final int INDEX_NUM_ROWS_WITH_HOLE = index++;
    public static final int INDEX_ERODED_PIECE_CELLS = index++;
    public static final int INDEX_LANDING_HEIGHT = index++;
    public static final int INDEX_ROW_TRANSITIONS = index++;
    public static final int INDEX_CUMULATIVE_WELLS = index++;

    private static Heuristics instance;

    // object variables
    private static double[] weights = {
        -30, -2, -3, -4, 100000000, 0, 0, 0, 0, 0, 0, 0
    };

    /// array to store the values for each feature, later to be multiplied by weights
    private int[] features;

    private Heuristics() {
        features = new int[NUM_FEATURES];
    }

    public static void setWeights(double numHolesWeight, double heightDiffWeight, double maxHeightWeight, double rowsClearedWeight, double gameLost, double
                                  colTransitions, double holeDepth, double rowsWithHole, double erodedPieceCells, double landingHeight, double rowTransitions, double cumulativeWells) {
        weights[INDEX_NUMHOLES] = numHolesWeight;
        weights[INDEX_HEIGHT_DIFF] = heightDiffWeight;
        weights[INDEX_MAX_HEIGHT] = maxHeightWeight;
        weights[INDEX_ROWS_CLEARED] = rowsClearedWeight;
        weights[INDEX_LOST] = gameLost;
        weights[INDEX_COL_TRANSITIONS] = colTransitions;
        weights[INDEX_HOLE_DEPTH] = holeDepth;
        weights[INDEX_NUM_ROWS_WITH_HOLE] = rowsWithHole;
        weights[INDEX_ERODED_PIECE_CELLS] = erodedPieceCells;
        weights[INDEX_LANDING_HEIGHT] = landingHeight;
        weights[INDEX_ROW_TRANSITIONS] = rowTransitions;
        weights[INDEX_CUMULATIVE_WELLS] = cumulativeWells;
    }

    public static Heuristics getInstance() {
        if (instance == null)
            instance = new Heuristics();
        return instance;
    }

    public double getUtility(State modifiedState, State actualState) {
        features[INDEX_NUMHOLES] = feature_getNumHoles(modifiedState);
        features[INDEX_HEIGHT_DIFF] = feature_getHeightDiff(modifiedState);
        features[INDEX_MAX_HEIGHT] = feature_getMaxHeight(modifiedState);
        features[INDEX_ROWS_CLEARED] = feature_getRowsCleared(modifiedState, actualState);
        features[INDEX_LOST] = modifiedState.hasLost() ? 1: 0;

        getFeature5to11(modifiedState, actualState);

        double utility = 0;
        for (int i = 0; i < NUM_FEATURES; i++){
            utility += features[i] * weights[i];
        }
        return utility;
    }

    private void getFeature5to11(State modifiedState, State actualState) {
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
        int maxHeight = 0;

        // for use in landingheight
        int maxLandingHeight = 0;
        int thisTurn = modifiedState.getTurnNumber();
        if (thisTurn < 0) {
            System.out.println("Integer overflow");
            System.exit(1);
        }

        // for use in erodedPieceCells
        // all pieces consists of 4 blocks
        int totalSize = 4;
        int numBlocksLeft = 0;

        int[] top = modifiedState.getTop();
        int[][] field = modifiedState.getField();

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

        int numRowsCleared = modifiedState.getRowsCleared() - actualState.getRowsCleared();
        features[INDEX_ERODED_PIECE_CELLS] = numRowsCleared * (totalSize - numBlocksLeft);

        //getRowTransitions()
        maxHeight = features[INDEX_ROW_TRANSITIONS]; // this is just a hack to save a scan of array
        transitionCount = 0;
        field = modifiedState.getField();
        isFilled = field[maxHeight][0] == 1;

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

        //getCumulativeWells
        int sumWellDepths = 0;
        for (int col = 1; col < top.length - 1; col++) {
            if (top[col - 1] > top[col] && top[col] < top[col + 1]) {
                int depth = Math.min(top[col - 1] - top[col], top[col + 1] - top[col]);
                int arithSeries = (depth * (depth+1)) / 2;
                sumWellDepths += arithSeries;
            }
        }

        // check left- and right-most column, treating the wall as block of maxHeight.
        if (top[0] < top[1]) {
            int depth = top[1] - top[0];
            int arithSeries = (depth * (depth+1)) / 2;
            sumWellDepths += arithSeries;
        }

        if (top[top.length - 1] < top[top.length - 2]) {
            int depth = (top[top.length - 2] - top[top.length - 1]);
            int arithSeries = (depth * (depth+1)) / 2;
            sumWellDepths += arithSeries;
        }

        features[INDEX_CUMULATIVE_WELLS] = sumWellDepths;
    }

    private int feature_getRowsCleared(State modifiedState, State actualState) {
        return modifiedState.getRowsCleared() - actualState.getRowsCleared();
    }

    private int feature_getNumHoles(State s) {
        int numHoles = 0;
        int[] top = s.getTop();
        int[][] field = s.getField();
        for (int col = 0; col < State.COLS; col++) {
            int topRow = top[col]-1;
            for (int row = topRow-1; row > 0; row--) {
                if (field[row][col] == 0)
                    numHoles++;
            }
        }
        if (DEBUG)
            System.out.println("#holes = " + numHoles);
        return numHoles;
    }

    private int feature_getHeightDiff(State s) {
        int heightDiff = 0;
        int[] top = s.getTop();
        for (int col = 0; col < top.length-1; col++) {
            heightDiff += Math.abs(top[col] - top[col+1]);
        }
        if (DEBUG)
            System.out.println("height difference = " + heightDiff);
        return heightDiff;
    }

    private int feature_getMaxHeight(State s) {
        int maxHeight = Arrays.stream(s.getTop()).max().getAsInt();
        if (DEBUG)
            System.out.println("maxHeight = " + maxHeight);
        return maxHeight;
    }
}
