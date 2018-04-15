////A class for testing purposes
//public class SimulateWeights {
//
//    private static int getRowsClearedFromSimulation() {
//        PlayerSkeleton player = new PlayerSkeleton();
//        return player.run();
//    }
//
//    // Within one configuration of weights
//    private static int simulateConfiguration(double numHolesWeight, double heightDiffWeight, double maxHeightWeight, double rowsClearedWeight, double gameLost) {
//        System.out.print("[" + numHolesWeight + ", " + heightDiffWeight + ", " + maxHeightWeight + ", " + rowsClearedWeight + "]");
//        Heuristics.setWeights(numHolesWeight, heightDiffWeight, maxHeightWeight, rowsClearedWeight, gameLost);
//        int currentRowsCleared = getRowsClearedFromSimulation();
//        System.out.println("Rows cleared = " + currentRowsCleared);
//        return currentRowsCleared;
//    }
//
//
//    public static void main(String[] args) {
//        System.out.println("Starting Trainer class. Format is: ");
//        System.out.println("[numHolesWeight, heightDiffWeight, maxHeightWeight, rowsClearedWeight]");
//        System.out.println("---------------------------");
//        int sum = 0;
//        for (int i = 0; i < 100; i++) {
//            sum += simulateConfiguration(-31.256801396633847, -7.2588432253635276, 0.6260697566906241, -5.468881453087734, -1.0E7);
//        }
//        System.out.println("Avr is " + sum/100.0);
//        System.out.println("---------------------------");
//    }
//}