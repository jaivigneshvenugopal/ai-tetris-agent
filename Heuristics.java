import java.util.Arrays;
import java.util.logging.Logger;

public class Heuristics {
    private static boolean DEBUG = true;
    private static int index = 0;
    public static final int NUM_FEATURES = 5;

    public static final int INDEX_NUMHOLES = index++;
    public static final int INDEX_HEIGHT_DIFF = index++;
    public static final int INDEX_MAX_HEIGHT = index++;
    public static final int INDEX_ROWS_CLEARED = index++;
    public static final int INDEX_LOST = index++;

    private static Heuristics instance;

    // object variables
    private double[] weights = {
            -5, -2, -3, -4, -100000000
    };

    /// array to store the values for each feature, later to be multiplied by weights
    private int[] features;

    private Heuristics() {
        features = new int[NUM_FEATURES];
    }

    public static Heuristics getInstance() {
        if (instance == null)
            instance = new Heuristics();
        return instance;
    }

    public double getUtility(State s) {
        features[INDEX_NUMHOLES] = feature_getNumHoles(s);
        features[INDEX_HEIGHT_DIFF] = feature_getHeightDiff(s);
        features[INDEX_MAX_HEIGHT] = feature_getMaxHeight(s);
        features[INDEX_ROWS_CLEARED] = s.getRowsCleared();
        features[INDEX_LOST] = s.hasLost() ? 1: 0;

        double utility = 0;
        for (int i = 0; i < NUM_FEATURES; i++){
            utility += features[i] * weights[i];
        }
        return utility;
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
