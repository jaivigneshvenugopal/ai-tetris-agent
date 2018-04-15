import java.util.Scanner;

public class PlayerSkeleton {

	// another State object to simulate the moves before actually playing
	protected StateSimulator2 sim;
	private double[] weights;
	private static final boolean DEBUG_FEATURES = false;

	private static Scanner sc = new Scanner(System.in);

	public PlayerSkeleton() {
		sim = new StateSimulator2();
	}

	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {

		int bestMove = getBestMoveBySimulation(s, legalMoves.length);
		if (DEBUG_FEATURES) {
			sim.copyState(s);
			sim.makeMove(bestMove);
		}
		return bestMove;
	}

	/**
	 * @param actualState the actual state with which the game runs
	 * @param numMoves the legal moves allowed
	 * @return the best move
	 */
	private int getBestMoveBySimulation(State actualState, int numMoves) {
		int bestMove = 0;
		double bestUtility = Double.NEGATIVE_INFINITY;
		for (int currentMove = 0; currentMove < numMoves; currentMove++) {
			double currentUtility = getUtility(actualState, currentMove);
			if (currentUtility > bestUtility) {
				bestMove = currentMove;
				bestUtility = currentUtility;
			}
		}
		return bestMove;
	}

	public static void main(String[] args) {
		State state = new State();
		new TFrame(state);
		PlayerSkeleton p = new PlayerSkeleton();
		double[] sampleWeight = { -3.06963462 , -8.38935298 , -6.85516551 , -8.72881560 , -4.45039098 , -7.36354452 , -6.63494222 , -9.56366090};
		p.setWeights(sampleWeight);

		while(!state.hasLost()) {
			state.makeMove(p.pickMove(state,state.legalMoves()));
			state.draw();
			state.drawNext(0,0);
			if (DEBUG_FEATURES) {
				// wait for a signal in the terminal to move to the next state
				printFeatures(p.sim.getFeaturesArray());
				System.out.println("Press ENTER to apply next move");
				sc.nextLine();
			} else {
//				try {
//					Thread.sleep(300);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
			}
		}
		System.out.println("You have completed " + state.getRowsCleared() + " rows.");
		System.exit(0);
	}

	public void setWeights(double[] weights) {
		this.weights = weights;
	}

	private double getUtility(State s, int move) {
		if (this.weights == null) {
			System.out.println("PlayerSkeleton: Weight array is not set. Exiting");
			System.exit(1);
		}

		sim.copyState(s);
		sim.makeMove(move);
		if (sim.hasLost()) {
			return Double.NEGATIVE_INFINITY;
		}

		int[] features = sim.getFeaturesArray();

		double utility = 0;
		for (int i = 0; i < StateSimulator.NUM_FEATURES; i++){
			utility += features[i] * weights[i];
		}

		return utility;
//		return Math.tanh(utility) - 1; // NN with no hidden layer
	}

	private static void printFeatures(int[] features) {
		System.out.println("--------------------------------------------------");
		System.out.println("#holes = " + 			features[StateSimulator.INDEX_NUMHOLES]);
		System.out.println("Col Transition = " + 	features[StateSimulator.INDEX_COL_TRANSITIONS]);
		System.out.println("Row Transition = " + 	features[StateSimulator.INDEX_ROW_TRANSITIONS]);
		System.out.println("holes depth = " + 		features[StateSimulator.INDEX_HOLE_DEPTH]);
		System.out.println("Cumulative well = " + 	features[StateSimulator.INDEX_CUMULATIVE_WELLS]);
		System.out.println("Landing height = " + 	features[StateSimulator.INDEX_LANDING_HEIGHT]);
		System.out.println("#rows with hole = " +	features[StateSimulator.INDEX_NUM_ROWS_WITH_HOLE]);
		System.out.println("Eroded piece cells " +  features[StateSimulator.INDEX_ERODED_PIECE_CELLS]);
		System.out.println("--------------------------------------------------");
	}
}

