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
		double[] sampleWeight = { -0.66815299 , -0.18275129 , -0.00313774 , -0.61375989 , -0.10106387 , -0.26484914 , -0.09336810 , -0.23320790 };
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
		for (int i = 0; i < StateSimulator2.NUM_FEATURES; i++){
			// get linear weighted sum
			utility += features[i] * weights[i];
		}

		return utility;
	}

	private static void printFeatures(int[] features) {
		System.out.println("--------------------------------------------------");
		System.out.println("#holes = " + 			features[StateSimulator2.INDEX_NUMHOLES]);
		System.out.println("Col Transition = " + 	features[StateSimulator2.INDEX_COL_TRANSITIONS]);
		System.out.println("Row Transition = " + 	features[StateSimulator2.INDEX_ROW_TRANSITIONS]);
		System.out.println("holes depth = " + 		features[StateSimulator2.INDEX_HOLE_DEPTH]);
		System.out.println("Cumulative well = " + 	features[StateSimulator2.INDEX_CUMULATIVE_WELLS]);
		System.out.println("Landing height = " + 	features[StateSimulator2.INDEX_LANDING_HEIGHT]);
		System.out.println("#rows with hole = " +	features[StateSimulator2.INDEX_NUM_ROWS_WITH_HOLE]);
		System.out.println("Eroded piece cells " +  features[StateSimulator2.INDEX_ERODED_PIECE_CELLS]);
		System.out.println("--------------------------------------------------");
	}
}

