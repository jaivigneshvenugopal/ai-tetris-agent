import java.util.Arrays;

public class PlayerSkeleton {

	// another State object to simulate the moves before actually playing
	private StateSimulator simulator;

	public PlayerSkeleton() {
			simulator = new StateSimulator();
	}

	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		int moveToPlay = getBestMoveBySimulation(s, legalMoves.length);
		simulator.makeMove(moveToPlay);
		simulator.markSimulationDoneWithCurrentPiece();
		return moveToPlay;
	}

	/**
	 * @param actualState the actual state with which the game runs
	 * @param moveChoices the legal moves allowed
	 * @return the best move
	 */
	public int getBestMoveBySimulation(State actualState, int moveChoices) {
		int bestMove = 0;
		double bestUtility = Double.NEGATIVE_INFINITY;
		simulator.setNextPiece(actualState.nextPiece); // synchronize the next piece
		for (int currentMove = 0; currentMove < moveChoices; currentMove++) {
			simulator.makeMove(currentMove);
			double currentUtility = Heuristics.getInstance().getUtility(simulator, actualState);
			if (currentUtility > bestUtility) {
				bestMove = currentMove;
				bestUtility = currentUtility;
			}
			simulator.resetMove();
		}
		return bestMove;
	}

	public static int run() {
		State s = new State();
		//new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();

		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
//			s.draw();
//			s.drawNext(0,0);
//			try {
//				Thread.sleep(1);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		}
		//System.out.println("You have completed "+s.getRowsCleared()+" rows.");
		return s.getRowsCleared();
	}

}

