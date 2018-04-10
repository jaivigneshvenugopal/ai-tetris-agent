import java.util.Scanner;

public class PlayerSkeleton {

	// another State object to simulate the moves before actually playing
	protected StateSimulator sim;
	private double[] weights;
	private static final boolean DEBUG_FEATURES = true;

	private static Scanner sc = new Scanner(System.in);

	public PlayerSkeleton() {
			sim = new StateSimulator();
	}

	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		int moveToPlay = getBestMoveBySimulation(s, legalMoves.length);
		this.sim.makeMove(moveToPlay); // update it since you do end up making this move
		this.sim.markSimulationDoneWithCurrentPiece();
		return moveToPlay;
	}

	/**
	 * @param actualState the actual state with which the game runs
	 * @param moveChoices the legal moves allowed
	 * @return the best move
	 */
	private int getBestMoveBySimulation(State actualState, int moveChoices) {
		int bestMove = 0;
		double bestUtility = Double.NEGATIVE_INFINITY;
		sim.setNextPiece(actualState.nextPiece); // synchronize the next piece
		for (int currentMove = 0; currentMove < moveChoices; currentMove++) {
			double currentUtility = getUtility(sim, currentMove);
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
		double[] sampleWeight = {-0.01, -0.02, -0.03, -0.05, -0.3, -0.1, -0.3, -0.5};
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
		System.out.println("You have completed "+state.getRowsCleared()+" rows.");
		System.exit(0);
	}

	public void setWeights(double[] weights) {
		this.weights = weights;
	}

	private double getUtility(StateSimulator s, int move) {
		if (this.weights == null) {
			System.out.println("PlayerSkeleton: Weight array is not set. Exiting");
			System.exit(1);
		}

		sim.makeMove(move);
		if (sim.hasLost()) {
			sim.resetMove();
			return Double.NEGATIVE_INFINITY;
		}

		int[] features = s.getFeaturesArray();

		double utility = 0;
		for (int i = 0; i < StateSimulator.NUM_FEATURES; i++){
			utility += features[i] * weights[i];
		}

		sim.resetMove();
		return utility;
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

