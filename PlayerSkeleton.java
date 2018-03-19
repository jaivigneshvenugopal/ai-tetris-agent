import java.util.Arrays;

public class PlayerSkeleton {

	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {

		return 0;
	}

	public static final int COLS = 10;
	public static final int ROWS = 21;
	public static final int ORIENT = 0;
	public static final int SLOT = 1;
	public static final int N_PIECES = 7;

	protected static int[][][] legalMoves = new int[N_PIECES][][];

	private int[][] field = new int[ROWS][COLS];
	private int[] top = new int[COLS];
	//private int[] pOrients = new int[];
	private int[][] pWidth = State.getpWidth();
	private int[][] pHeight = State.getpHeight();
	private int[][][] pBottom = State.getpBottom();
	private int[][][] pTop = State.getpTop();
	private int nextPiece;
	private int turn = 0;
	private boolean lost;
	private int cleared = 0;

	public int[][] getField() {
		return field;
	}

	public int[] getTop() {
		return top;
	}

	//public static int[] getpOrients() { return pOrients; }

	/* public static int[][] getpWidth() {
		return pWidth;
	} */

	/* public static int[][] getpHeight() {
		return pHeight;
	} */

	/* public static int[][][] getpBottom() {
		return pBottom;
	} */

	public int[] getpTop() {
		return top;
	}

	public boolean hasLost() {
		return lost;
	}

	public int getRowsCleared() {
		return cleared;
	}

	public int getTurnNumber() {
		return turn;
	}

	public int getNextPiece() {
		return nextPiece;
	}

	public void setNextPiece(int nextPiece) { this.nextPiece = nextPiece; }

	public void resetState(State state) {
		int[][] field = state.getField();
		int stateNextPiece = state.getNextPiece();
		boolean stateLost = state.hasLost();
		int stateCleared = state.getRowsCleared();
		int stateTurn = state.getTurnNumber();
		Arrays.fill(this.top,0);
	}

	public boolean makeMove(int orient, int slot) {
		turn++;
		//height if the first column makes contact
		int height = top[slot]-pBottom[nextPiece][orient][0];
		//for each column beyond the first in the piece
		for(int c = 1; c < pWidth[nextPiece][orient];c++) {
			height = Math.max(height,top[slot+c]-pBottom[nextPiece][orient][c]);
		}

		//check if game ended
		if(height+pHeight[nextPiece][orient] >= ROWS) {
			lost = true;
			return false;
		}


		//for each column in the piece - fill in the appropriate blocks
		for(int i = 0; i < pWidth[nextPiece][orient]; i++) {

			//from bottom to top of brick
			for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
				field[h][i+slot] = turn;
			}
		}

		//adjust top
		for(int c = 0; c < pWidth[nextPiece][orient]; c++) {
			top[slot+c]=height+pTop[nextPiece][orient][c];
		}

		int rowsCleared = 0;

		//check for full rows - starting at the top
		for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--) {
			//check all columns in the row
			boolean full = true;
			for(int c = 0; c < COLS; c++) {
				if(field[r][c] == 0) {
					full = false;
					break;
				}
			}
			//if the row was full - remove it and slide above stuff down
			if(full) {
				rowsCleared++;
				cleared++;
				//for each column
				for(int c = 0; c < COLS; c++) {

					//slide down all bricks
					for(int i = r; i < top[c]; i++) {
						field[i][c] = field[i+1][c];
					}
					//lower the top
					top[c]--;
					while(top[c]>=1 && field[top[c]-1][c]==0)	top[c]--;
				}
			}
		}

		//pick a new piece
		//nextPiece = randomPiece();

		return true;

		//return super.makeMove(orient, slot);
	}

	//gives legal moves for
	public int[][] legalMoves() {
		return legalMoves[nextPiece];
	}

	//make a move based on the move index - its order in the legalMoves list
	public void makeMove(int move) {
		makeMove(legalMoves[nextPiece][move]);
	}

	//make a move based on an array of orient and slot
	public void makeMove(int[] move) {
		makeMove(move[ORIENT],move[SLOT]);
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
