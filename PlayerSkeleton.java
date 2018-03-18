
public class PlayerSkeleton {

	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
	    int move = 0;

	    move = selectState(s, legalMoves);

	    return move;
	}

	public int selectState(State state, int[][] legalMoves) {

	    int minValue = 100;
	    int bestState = 0;

	    for (int stateNo = 0; stateNo < legalMoves.length; stateNo++) {

	        try {
                State simulateState = (State)state.clone();

                int heuristicValue = findHeuristic(simulateState, stateNo, legalMoves);

                if (heuristicValue < minValue) {
                    minValue = heuristicValue;
                    bestState = stateNo;
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        return bestState;
    }

    public int findHeuristic(State simulateState, int stateNo, int[][] legalMoves) {

	    simulateState.makeMove(legalMoves[stateNo]);
        int heuristicValue = calculateAbsHeightDiff(simulateState);
        return heuristicValue;
    }

    public int calculateAbsHeightDiff(State simulateState) {

	    int sum = 0;
	    int top[] = simulateState.getTop();
	    for(int i = 0; i < 9; i++) {
            sum += Math.abs(top[i]-top[i+1]);
	    }

	    return sum;
    }


	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
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
	
}
