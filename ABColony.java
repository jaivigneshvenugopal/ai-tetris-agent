import java.lang.Math;

/**
 * Implementation of (Gbest-guided) Artificial Bee Colony algorithm. Modified from ...
 */
public class ABColony {
    /* Control Parameters of ABC algorithm */

    /**
     * A colony consists of
     * 1. employee bees
     * 2. onlook bees
     * 3. scout bee (limited to only 1)
     * 50 is obtained from research
     */
    public static final int COLONY_SIZE = 50;

    /**
     * Since half the colony are employee bees, this is equal to half the colony size.
     */
    public static final int NUM_FOOD_SOURCE = COLONY_SIZE / 2;

    /**
     * The number of trials we allow till a food source can be improved. 50 is obtained from research
     */
    public static final int LIMIT = 50;

    /**
     * The number of cycles for foraging (i.e. till stopping the algorithm). 500 is obtained from research
     */
    public static final int MAX_CYCLE = 500;

    /**
     * File storage related parameters.
     * Set these parameters to desirable values BEFORE running the algorithm should you need to.
     */
    public static boolean READ_FROM_INITIAL_SOLUTIONS = false;

    public static String INITIAL_WEIGHTS_FILE;

    public static boolean WRITE_RESULT_TO_FILE = false;

    public static String WEIGHTS_RESULT_FILE;

    /* Problem specific variables */

    public static final int NUM_PARAMETERS = StateSimulator.NUM_FEATURES;

    private double[][] foods = new double[NUM_FOOD_SOURCE][NUM_PARAMETERS];

    /**
     * stores the fitness value for each of the food sources/locations. Fitness values are calculated via neural net/by playing Tetris.
     */
    private double[] fitness = new double[NUM_FOOD_SOURCE];

    /**
     * stores the number of trials made for each food source/location. Once this exceeds the limit, that food source is abandoned and the employee bee for that food source will become a scout.
     */
    private double[] trialCounts = new double[NUM_FOOD_SOURCE];

    private int foodSourceToAbandon  = -1;

    /**
     * This is the constant that defines the rand() range for the update in g-best algorithm.
     */
    private static final int C = 2;

    private double globalBestFitness = Double.NEGATIVE_INFINITY;
    private double[] globalBestParameters = new double[NUM_PARAMETERS];

    public double[] run() {
        initializeFoodSources();
        memorizeBestSource();
        for (int i = 0; i < MAX_CYCLE; i++) {
            sendEmployedBees();
            sendOnlookerBees();
            memorizeBestSource();
            sendScoutBees();
        }
        return globalBestParameters;
    }

    private double calculateFitness(double[] foodSource) {
        //TODO parallelize this process
        return 0;
    }

    private void memorizeBestSource() {
        for (int i = 0; i < NUM_FOOD_SOURCE; i++) {
            if (fitness[i] > globalBestFitness) {
                globalBestFitness = fitness[i];
                System.arraycopy(foods[i], 0, globalBestParameters, 0, NUM_PARAMETERS);
            }
        }
    }

    private void initializeFoodSource(int foodIndex) {
        // if the global optimum is already set, then use a modification of that

        for (int i = 0; i < NUM_PARAMETERS; i++) {
            //TODO initialize the weights of features here
        }
        fitness[foodIndex] = calculateFitness(foods[foodIndex]);

        // since this food source has just been found, set trialCounts = 0
        trialCounts[foodIndex] = 0;
    }

    /*All food sources are initialized */
    private void initializeFoodSources() {
        if (READ_FROM_INITIAL_SOLUTIONS)
            return; //TODO read from the file
        int i;
        for (i = 0; i < NUM_FOOD_SOURCE; i++) {
            initializeFoodSource(i);
        }
    }

    private void sendEmployedBees() {
        int j;
        for (int i = 0; i < NUM_FOOD_SOURCE; i++) {
            updateWithGBest(i);
        }
    }

    private void sendOnlookerBees() {
        for (int t = 0; t < NUM_FOOD_SOURCE; t++) {
//            int i = selectFoodSource(); // try not to modify the probability distribution during this phase
//            updateWithGBest(i);
        }
    }

    /**
     * determine the food sources whose trialCounts counter exceeds the "LIMIT" value. In Basic ABC, only one scout is allowed to occur in each cycle
     */
    private void sendScoutBees() {
        if (foodSourceToAbandon == -1)
            return;
        initializeFoodSource(foodSourceToAbandon);
        foodSourceToAbandon = -1;
    }

    /**
     * randomly computes a mutated food source from foods[i] and if better, updates x.
     * @param i the index of a food source
     */
    private void updateWithGBest(int i) {
        double[] xi = foods[i];
        int k; // randomly selected neighbour index
        do {
            k = (int) (Math.random() * NUM_FOOD_SOURCE);
        } while (k != i);

        // randomly selected parameter that will be updated
        int j = (int) (Math.random() * NUM_PARAMETERS);
        double old_value = xi[j];
        double[] xk = foods[k];

        double update = xi[j] + 2*(Math.random() - 0.5) * (xi[j] - xk[j]) + (Math.random() * C) * (globalBestParameters[j] - xi[j]);

        xi[j] = update;

        double newFitness = calculateFitness(xi);
        if (newFitness > fitness[i]) {
            fitness[i] = newFitness;
            trialCounts[i] = 0;
        } else {
            xi[j] = old_value; // restore to the old value at x[j]
            trialCounts[i]++;
            // maybe keep an instance variable that tells whether this bee should be turned into scout,
            // rather than scanning through trialCounts every time.
            if (trialCounts[i] >= LIMIT)
                foodSourceToAbandon = i;
        }
    }

    private double getSumFitness() {
        double ans = 0;
        for (double fit: fitness) {
            ans += fit;
        }
        return ans;
    }

}
