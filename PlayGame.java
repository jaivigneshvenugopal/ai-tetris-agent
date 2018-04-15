import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * Callable to play the game concurrently in order to reduce the running time. Because we are modelling the game not as a RL problem but rather as an optimization problem, we must evaluate the performance of a candidate solution by playing the game till we *lose.
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

    private static final boolean useStatistics = false;

    private double[] scores = new double[NUM_GAMES_TO_AVERAGE];

    private static final int C = 500;

    //TODO seems like < 50 is not good because the game is so random and also because we make the game intentionally harder than the usual one, we must consider the randomness more seriously
    public static final int NUM_GAMES_TO_AVERAGE = 50;

    private int[] performMeasures;
    private double[] weights;
    private double totalPerformance;

    public PlayGame(double[] weights) {
        this.weights = weights;

        // hole depth, # holes, row transitions, column transtions
        this.performMeasures = new int[4*2+1];
    }
    /**
     * @return the fitness value of the weights assigned
     */
    @Override
    public Double call() {
        for (int i = 0; i < NUM_GAMES_TO_AVERAGE; i++) {
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
            }
            scores[i] = s.getRowsCleared();

            if (useStatistics)
                scores[i] = getComprehensiveUtility();
        }

//        System.out.print("Played with weights: ");
//        for (double weight: weights) {
//            System.out.print(weight + " ");
//        }
//        System.out.println("\n score = " + totalPerformance / NUM_GAMES_TO_AVERAGE);

        Arrays.sort(scores);
        return scores[NUM_GAMES_TO_AVERAGE / 2];
    }

    private void recordPerformancePerMove(int[] featuresAfterMove) {
        performMeasures[INDEX_NUM_MOVES_MADE]++;

        int numRowsWithHoles = featuresAfterMove[StateSimulator.INDEX_NUM_ROWS_WITH_HOLE];
        performMeasures[INDEX_AVG_NUM_HOLES]+= numRowsWithHoles;
        performMeasures[INDEX_MAX_NUM_HOLES] = Math.max(performMeasures[INDEX_MAX_NUM_HOLES], numRowsWithHoles);

        int rowTransitions = featuresAfterMove[StateSimulator.INDEX_ROW_TRANSITIONS];
        performMeasures[INDEX_AVG_ROW_TRANSITIONS] += rowTransitions;
        performMeasures[INDEX_MAX_ROW_TRANSITIONS] = Math.max(performMeasures[INDEX_MAX_ROW_TRANSITIONS], rowTransitions);

        int colTransitions = featuresAfterMove[StateSimulator.INDEX_COL_TRANSITIONS];
        performMeasures[INDEX_AVG_COL_TRANSITIONS] += colTransitions;
        performMeasures[INDEX_MAX_COL_TRANSITIONS] = Math.max(performMeasures[INDEX_MAX_COL_TRANSITIONS], colTransitions);

        int holeDepth = featuresAfterMove[StateSimulator.INDEX_HOLE_DEPTH];
        performMeasures[INDEX_AVG_HOLE_DEPTH] += holeDepth;
        performMeasures[INDEX_MAX_HOLE_DEPTH] = Math.max(performMeasures[INDEX_MAX_HOLE_DEPTH], holeDepth);
    }

    private double getComprehensiveUtility() {
        double utility = 0;

        double numMovesMade = performMeasures[INDEX_NUM_MOVES_MADE];

        for (int i = 1; i <= 4; i++) {
            double max = performMeasures[i+4];
            double avg = performMeasures[i] / numMovesMade;

            utility += ((max-avg) / max) * C;
        }
        return utility;
    }

    /**
     * In order to terminate the game faster, we are going to change the distribution of the
     * randomNextPiece by making it thrice as likely to get S/Z pieces as other pieces
     * @param s state with the nextPiece set according to our probability distribution
     */
    private void changeNextPiece(State s) {
        /*
         piece labels are as follows:
         0. 2 by 2 box
         1. Stick
         2. L
         3. mirror L
         4. T shape
         5. Z
         6. S
         */
        int nextPiece = (int) (Math.random() * 11);
        switch (nextPiece) {
            // make S and Z thrice as more likely to get as other pieces
            case 7:
            case 8:
                nextPiece = 5;
                break;
            case 9:
            case 10:
                nextPiece = 6;
                break;
        }
        if (nextPiece > 6) {
            System.out.println("PlayGame: piece is not set properly");
            System.exit(1);
        }

        s.nextPiece = nextPiece; // we can do this since nextPiece is package-private i.e. protected
    }
}
