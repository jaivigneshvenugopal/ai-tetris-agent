
public class PlayerSkeleton {

	// another State object to simulate the moves before actually playing
	private State simulator;
	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {

		// 1. simulate the move
		// 2. for each move, calculate the utility
		// 3. backtrack
		// 3. play the actual move
        System.out.println("next piece: " + s.nextPiece + " # legal moves =" + legalMoves.length);

		return 0;
	}

	public void simulate(State s, int[][] legalMoves) {
	}
	
	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();

		// construct another State object used for simulation
		p.simulator = new State();
		// make it null since we don't need it
		p.simulator.label = null;

		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}

	/// features

	/**
	 *
	 * @param s: represents the state of the game
	 * @return number of holes in the field
	 */
	public static int numHoles(State s) {
		int[] topRow = s.getTop();
		int[][] field = s.getField();
		int numHoles = 0;
		boolean hasHole;
		for (int col = 0; col < State.COLS; col++) {
			// for each column, find if there is a hole
			hasHole = false;
			int topCell = topRow[col] - 1; // since s.getTop() contains topRow + 1
			for (int row = topCell-1; row > 0; row--) {
				if (field[row][col] == 0) {
					hasHole = true;
					break;
				}
			}

			if (hasHole) { // if this column has a hole
				numHoles++;
			}
		}

		return numHoles;
	}

	/**
	 *
	 * @param s: State
	 * @return sum of total height difference between neighbouring columns abs(height(k) - height(k+1))
	 */
	public static int heightDiff(State s) {
		int[] topRow = s.getTop(); // no need to minus 1 since it gets cancelled
		int totalHeightDiff = 0;
		for (int currentCol = 0; currentCol < topRow.length-1; currentCol++) {
			int nextCol = currentCol+1;
			totalHeightDiff += Math.abs(currentCol - nextCol);
		}
		return totalHeightDiff;
	}


	/**
	 *
	 * @param s: State
	 * @return the maximum height among all the columns
	 */
	public static int maxHeight(State s) {
		int maxHeight = -1;
		for (int top: s.getTop()) {
			maxHeight = Math.max(maxHeight, top-1);
		}
		return maxHeight;
	}

}

//class StateSimulator extends State {
//	public StateSimulator() {
//		super();
//		this.label = null;
//	}
//
//	public void MakeMoveWithReset(int move) {
//		this.makeMove(move);
//		// need to backtrack
//
//
//	}
//
//}
