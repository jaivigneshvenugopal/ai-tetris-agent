import java.util.concurrent.Callable;

/**
 * Callable to play the game concurrently in order to reduce the running time. Because we are modelling the game not as a RL problem but rather as an optimization problem, we must evaluate the performance of a candidate solution by playing the game till we *lose.
 */
public class PlayGame implements Callable<Double> {

    private double[] weights;

    public PlayGame(double[] weights) {
        this.weights = weights;
    }

    /**
     * @return the fitness value of the weights assigned
     * @throws Exception
     */
    @Override
    public Double call() throws Exception {
        if (this.weights == null) {
            System.out.println("weights are not set");
            System.exit(1);
        }

        PlayerSkeleton player = new PlayerSkeleton();
        State s = new State();
        player.setWeights(weights);
        while(!s.hasLost()) {
            changeNextPiece(s);
            s.makeMove(player.pickMove(s,s.legalMoves()));
        }

        return (double) s.getRowsCleared();
    }

    /**
     * In order to terminate the game faster, we are going to change the distribution of the
     * randomNextPiece by making it thrice as likely to get S/Z pieces as other pieces
     * @param s state with the nextPiece set according to our probability distribution
     */
    private static void changeNextPiece(State s) {
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
