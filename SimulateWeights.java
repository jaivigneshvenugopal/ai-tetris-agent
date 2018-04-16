//A class for testing purposes
public class SimulateWeights {

    private static int getRowsClearedFromSimulation() {
        PlayerSkeleton player = new PlayerSkeleton();
        return player.run();
    }

    // Within one configuration of weights
    private static int simulateConfiguration(double newNumHoles, double newHeightDiff, double newMaxHeight, double newRowsCleared, double newGameLost, double newColTrans, double newHoleDepth, double numRowsWithHole, double newErodedPieces, double newLandingHeight, double newRowTrans, double newCumalativeWells) {
        System.out.print("[" + newNumHoles + ", " + newHeightDiff + ", " + newMaxHeight + ", " + newRowsCleared + ", " + newGameLost + ", " + newColTrans + ", " + newHoleDepth + ", " + numRowsWithHole + ", " + newErodedPieces + ", " + newLandingHeight + ", " + newRowTrans + ", " + newCumalativeWells +  "]");
        Heuristics.setWeights(newNumHoles, newHeightDiff, newMaxHeight, newRowsCleared, newGameLost, newColTrans, newHoleDepth, numRowsWithHole, newErodedPieces, newLandingHeight, newRowTrans, newCumalativeWells);
        int currentRowsCleared = getRowsClearedFromSimulation();
        System.out.println("Rows cleared = " + currentRowsCleared);
        return currentRowsCleared;
    }


    public static void main(String[] args) {
        System.out.println("Starting Trainer class. Format is: ");
        System.out.println("[numHolesWeight, heightDiffWeight, maxHeightWeight, rowsClearedWeight, newRowsCleared, newGameLost, newColTrans, newHoleDepth, numRowsWithHole, newErodedPieces, newLandingHeight, newRowsTrans, newCumulativeWells]");
        System.out.println("---------------------------");
        Solution newSolution = new Solution();
        int sum = 0;
        for (int i = 0; i < 100; i++) {
            sum += simulateConfiguration(newSolution.COE_NUM_HOLES, newSolution.COE_HEIGHT_DIFF, newSolution.COE_MAX_HEIGHT, newSolution.COE_ROWS_CLEARED, newSolution.COE_LOST, newSolution.COL_TRANSITIONS, newSolution.HOLE_DEPTH, newSolution.NUM_ROWS_WITH_HOLE, newSolution.ERODED_PIECE_CELLS, newSolution.LANDING_HEIGHT, newSolution.ROW_TRANSITIONS, newSolution.CUMULATIVE_WELLS);
        }
        System.out.println("Avr is " + sum/100.0);
        System.out.println("---------------------------");
    }
}