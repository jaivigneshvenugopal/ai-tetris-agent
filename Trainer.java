/**
 * Created by hyma on 26/3/18.
 */
public class Trainer {
    public static final double STARTING_NEGATIVE = -50;
    public static final double STARTING_POSITIVE = 50;
    public static final double INCREMENT_VALUE = 5;

    private static int index = 0;

    public static final int INDEX_NUMHOLES = index++;
    public static final int INDEX_HEIGHT_DIFF = index++;
    public static final int INDEX_MAX_HEIGHT = index++;
    public static final int INDEX_ROWS_CLEARED = index++;
    public static final int INDEX_LOST = index++;

    static int mostRowsCleared = 0;
    static double[] bestConfiguration = {
            -30, -2, -3, -4, -100000000
    };

    private static int getRowsClearedFromSimulation() {
        PlayerSkeleton player = new PlayerSkeleton();
        return player.run();
    }

    // Within one configuration of weights
    private static void simulateConfiguration(double numHolesWeight, double heightDiffWeight, double maxHeightWeight, double rowsClearedWeight) {
        System.out.print("[" + numHolesWeight + ", " + heightDiffWeight + ", " + maxHeightWeight + ", " + rowsClearedWeight + "]");
        int currentRowsCleared = getRowsClearedFromSimulation();
        System.out.println("Rows cleared = " + currentRowsCleared);

        if (currentRowsCleared > mostRowsCleared) {
            mostRowsCleared = currentRowsCleared;
            bestConfiguration[INDEX_NUMHOLES] = numHolesWeight;
            bestConfiguration[INDEX_HEIGHT_DIFF] = heightDiffWeight;
            bestConfiguration[INDEX_MAX_HEIGHT] = maxHeightWeight;
            bestConfiguration[INDEX_ROWS_CLEARED] = rowsClearedWeight;
            bestConfiguration[INDEX_LOST] = -100000000;
        }
    }


    public static void main(String[] args) {
        System.out.println("Starting Trainer class. Format is: ");
        System.out.println("[numHolesWeight, heightDiffWeight, maxHeightWeight, rowsClearedWeight]");
        System.out.println("---------------------------");
//        simulateConfiguration(-75,-100,-50,100);

//         loops to modify weights
        for (double numHolesWeight = STARTING_NEGATIVE; numHolesWeight <= 0; numHolesWeight += INCREMENT_VALUE) {
            for (double heightDiffWeight = STARTING_NEGATIVE; heightDiffWeight <= 0; heightDiffWeight += INCREMENT_VALUE) {
                for (double maxHeightWeight = STARTING_NEGATIVE; maxHeightWeight <= 0; maxHeightWeight += INCREMENT_VALUE) {
                    for (double rowsClearedWeight = STARTING_POSITIVE; rowsClearedWeight >= 0; rowsClearedWeight -= INCREMENT_VALUE) {
                        simulateConfiguration(numHolesWeight, heightDiffWeight, maxHeightWeight, rowsClearedWeight);
                    }
                }
            }
        }
        System.out.println("---------------------------");
        System.out.println("Best configuration was " + bestConfiguration + " which cleared " + mostRowsCleared + ".");
    }
}