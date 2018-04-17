import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * Callable used to play the game concurrently in order to reduce the running time. Because we are modelling the game not as a RL problem but rather as an optimization problem, we must evaluate the performance of a candidate solution by playing the game till we lose.
 */
public class PlayGame implements Callable<Double> {
    private static final int INDEX_NUM_MOVES_MADE 		= 0;

    private static final int INDEX_AVG_COL_TRANSITIONS  = 1;
    private static final int INDEX_AVG_HOLE_DEPTH       = 2;
    private static final int INDEX_AVG_NUM_HOLES        = 3;
    private static final int INDEX_AVG_ROW_TRANSITIONS  = 4;

    private static final int INDEX_MAX_COL_TRANSITIONS  = 5;
    private static final int INDEX_MAX_HOLE_DEPTH       = 6;
    private static final int INDEX_MAX_NUM_HOLES        = 7;
    private static final int INDEX_MAX_ROW_TRANSITIONS  = 8;

    private static final int C = 100;

    private int[] performanceMeasures;
    private static final boolean useStatistics = false;

    /**
     * stores the score/performance for each game so that we can later sort and retrieve the median, min, max etc.
     */
    private double[] scores = new double[NUM_GAMES_TO_AVERAGE];
    public static int NUM_GAMES_TO_AVERAGE = 30;

    private Random rand = new Random();

    private double[] weights;

    /**
     * saves the food index that was modified to create the weights used for the games
     */
    public int foodIndex;

    /**
     * saves the bee index during onlooker phase.
     */
    public int beeIndex;

    /**
     * sets the bound for random.nextDouble() in order to modify the probability of pieces.
     * Call setLevel() to adjust this bound
     */
    public static int bound = 9; // default set to 9 for training

    public PlayGame(double[] weights) {
        this.weights = weights;
        if (useStatistics)
            this.performanceMeasures = new int[4*2+1];
    }

    public PlayGame(double[] weights, int foodIndex) {
        this.weights = weights;
        this.foodIndex = foodIndex;
        if (useStatistics)
            this.performanceMeasures = new int[4*2+1];
    }

    public PlayGame(double[] weights, int foodIndex, int beeIndex) {
        this.weights = weights;
        this.foodIndex = foodIndex;
        this.beeIndex = beeIndex;

        if (useStatistics)
            this.performanceMeasures = new int[4*2+1];
    }

    /**
     * @return the fitness value (median score of games played) of the weights assigned
     */
    @Override
    public Double call() {
        for (int i = 0; i < NUM_GAMES_TO_AVERAGE; i++) {
            if (useStatistics)
                resetPerformanceMeasures();

            PlayerSkeleton player = new PlayerSkeleton();
            player.setWeights(this.weights);
            State s = new State();
            while (!s.hasLost()) {
                changeNextPiece(s);
                int bestMove = player.pickMove(s, s.legalMoves());
                if (useStatistics) {
                    player.sim.copyState(s);
                    player.sim.makeMove(bestMove);
                    recordPerformancePerMove(player.sim.getFeaturesArray());
                }
                s.makeMove(bestMove);
            } // end loop
            if (useStatistics)
                scores[i] = getComprehensiveUtility();
            else
                scores[i] = s.getRowsCleared();
        }

        Arrays.sort(scores);
        return scores[NUM_GAMES_TO_AVERAGE / 2]; // the median
    }

    private void recordPerformancePerMove(int[] featuresAfterMove) {
        performanceMeasures[INDEX_NUM_MOVES_MADE]++;

        int numRowsWithHoles = featuresAfterMove[StateSimulator2.INDEX_NUM_ROWS_WITH_HOLE];
        performanceMeasures[INDEX_AVG_NUM_HOLES]+= numRowsWithHoles;
        performanceMeasures[INDEX_MAX_NUM_HOLES] = Math.max(performanceMeasures[INDEX_MAX_NUM_HOLES], numRowsWithHoles);

        int rowTransitions = featuresAfterMove[StateSimulator2.INDEX_ROW_TRANSITIONS];
        performanceMeasures[INDEX_AVG_ROW_TRANSITIONS] += rowTransitions;
        performanceMeasures[INDEX_MAX_ROW_TRANSITIONS] = Math.max(performanceMeasures[INDEX_MAX_ROW_TRANSITIONS], rowTransitions);

        int colTransitions = featuresAfterMove[StateSimulator2.INDEX_COL_TRANSITIONS];
        performanceMeasures[INDEX_AVG_COL_TRANSITIONS] += colTransitions;
        performanceMeasures[INDEX_MAX_COL_TRANSITIONS] = Math.max(performanceMeasures[INDEX_MAX_COL_TRANSITIONS], colTransitions);

        int holeDepth = featuresAfterMove[StateSimulator2.INDEX_HOLE_DEPTH];
        performanceMeasures[INDEX_AVG_HOLE_DEPTH] += holeDepth;
        performanceMeasures[INDEX_MAX_HOLE_DEPTH] = Math.max(performanceMeasures[INDEX_MAX_HOLE_DEPTH], holeDepth);
    }

    private double getComprehensiveUtility() {
        double utility = 0;
        double numMovesMade = performanceMeasures[INDEX_NUM_MOVES_MADE];

        for (int i = 1; i <= 4; i++) {
            double max = performanceMeasures[i+4];
            double avg = performanceMeasures[i] / numMovesMade;
            utility += ((max-avg) / max) * C;
        }
        return utility;
    }

    private void resetPerformanceMeasures() {
        for (int i = 0; i < performanceMeasures.length; i++) {
            performanceMeasures[i] = 0;
        }
    }

    /**
     * In order to terminate the game faster, we are going to change the distribution of the
     * randomNextPiece by making it more likely to get S/Z pieces compared to other pieces
     *
     * <br> level = 1 => uniform distribution
     * <br> level = 2 => Z and S pieces twice as likely
     * <br> level = 3 => Z and S pieces thrice as likely
     * @param s state to mutate the nextPiece set according to our probability distribution
     */
    private void changeNextPiece(State s) {
        /*
             piece labels are as follows:
             0. 2 by 2 box
             1. Stick
             2. L
             3. mirror L
             4. T
             5. Z
             6. S
         */
        int nextPiece = (int) (rand.nextDouble() * bound);
        switch (nextPiece) {
            case 8:
            case 10:
                nextPiece = 5;
                break;
            case 7:
            case 9:
                nextPiece = 6;
                break;
            default:
                break;
        }
        if (nextPiece > 6) {
            System.out.println("PlayGame: piece is not set properly");
            System.exit(1);
        }

        s.nextPiece = nextPiece; // we can do this since nextPiece is package-private i.e. protected
    }

    public static void setLevel(int level) {
        switch (level) {
            // bound == 9  => double
            // bound == 11 => triple
            case 1:
                bound = 7;
                break;
            case 2:
                bound = 9;
                break;
            case 3:
                bound = 11;
                break;
            default:
                System.exit(-1);
        }
    }
}
