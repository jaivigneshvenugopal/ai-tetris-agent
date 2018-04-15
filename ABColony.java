import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
    public static final int COLONY_SIZE = 100;

    /**
     * Since half the colony are employee bees, this is equal to half the colony size.
     */
    public static final int NUM_FOOD_SOURCE = COLONY_SIZE / 2;

    /**
     * The number of trials we allow till a food source can be improved. 50 is obtained from research
     */
    public static final int LIMIT = 20;

    /**
     * The number of cycles for foraging (i.e. till stopping the algorithm). 500 is obtained from research
     */
    public static final int MAX_CYCLE = 500;

    /**
     * File storage related parameters.
     * Set these parameters to desirable values BEFORE running the algorithm should you need to.
     */
    public static String INITIAL_WEIGHTS_FILE;

    public static String WEIGHTS_RESULT_FILE = "weights.out";

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

    private double localBestFitness = Double.NEGATIVE_INFINITY;
    private double[] localBestParameters = new double[NUM_PARAMETERS];

    /**
     * This is the constant that defines the rand() range for the update in g-best algorithm.
     */
    private static final int C = 2;

    public double globalBestFitness = Double.NEGATIVE_INFINITY;
    public double[] globalBestParameters = new double[NUM_PARAMETERS];

    /* For parallel computing */
    private static final boolean RUN_CONCURRENT = true;
    private final ExecutorService es;
    private final int NUM_THREADS;
    private ArrayList<PlayGame> tasks;

    /* stores the index of parameter being changed for playing a game for each food source. */
    private int[] parameterChanged = new int[NUM_FOOD_SOURCE];
    private double[] parameterOldValue = new double[NUM_FOOD_SOURCE];
    private int[] foodSourceChanged = new int[NUM_FOOD_SOURCE]; // used for keeping track of which food source an onlooker bee modified.
    Random rand = new Random();

    private double UPPER = 1.0;
    private double LOWER = -1.0;
    private boolean LIMIT_RANGE = false;

    private ABColony() {
        if (RUN_CONCURRENT) {
            NUM_THREADS = Runtime.getRuntime().availableProcessors();
            System.out.println("# processors available = " + NUM_THREADS);
            es = Executors.newFixedThreadPool(NUM_THREADS);
            tasks = new ArrayList<>(NUM_FOOD_SOURCE);
        } else {
            NUM_THREADS = 1;
            es = Executors.newSingleThreadExecutor();
            tasks = new ArrayList<>(1);
        }
    }

    public static void main(String[] args) throws IOException {
        ABColony bee = new ABColony();
        bee.initializeFoodSources();
        bee.memorizeBestSource();
        for (int i = 0; i < MAX_CYCLE; i++) {
            bee.sendEmployedBees();
            bee.sendOnlookerBees();
            bee.memorizeBestSource();
            bee.sendScoutBees();
            if (i % 10 == 0) {
                System.out.println("Iteration #" + i);
                printResult(bee);
            }
        }

        bee.es.shutdown();

        if (WEIGHTS_RESULT_FILE != null) {
            writeOptimalWeightToFile(bee.globalBestParameters);
        }
    }

    private static void writeOptimalWeightToFile(double[] weights) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(WEIGHTS_RESULT_FILE));
        for (double weight: weights) {
            bw.write(Double.toString(weight) + " ");
        }
    }

    private static void printResult(ABColony bee) {
        double[] weights = bee.globalBestParameters;
        Debug.printBold("optimum = " + bee.globalBestFitness + "\n");
        for (double weight : weights) {
            System.out.printf(" %.8f ,", weight);
        }
        System.out.println("\n-----------------------------------------");
    }

    private double calculateFitness(double[] foodSource) {
        tasks.clear();
        tasks.add(new PlayGame(foodSource));
        try {
            for (Future<Double> future: es.invokeAll(tasks)) return future.get(); // just one thread
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void memorizeBestSource() {
        for (int i = 0; i < NUM_FOOD_SOURCE; i++) {
            if (fitness[i] > globalBestFitness) {
                globalBestFitness = fitness[i];
                System.arraycopy(foods[i], 0, globalBestParameters, 0, NUM_PARAMETERS);
            }

//            localBestFitness = 0;
//            if (fitness[i] > localBestFitness) {
//                localBestFitness = fitness[i];
//                System.arraycopy(foods[i], 0, localBestParameters, 0, NUM_PARAMETERS);
//            }
        }
    }

    private void initializeFoodSource(int foodIndex) {
        // if the global optimum is already set, then use a modification of that
        for (int j = 0; j < NUM_PARAMETERS; j++) {
            foods[foodIndex][j] = LOWER + rand.nextDouble() * (UPPER - LOWER);
        }
        fitness[foodIndex] = calculateFitness(foods[foodIndex]); // since we only have 1 scout, it is ok to use single thread

        // since this food source has just been found, set trialCounts = 0
        trialCounts[foodIndex] = 0;
    }

    /*All food sources are initialized */
    private void initializeFoodSources() {
        if (INITIAL_WEIGHTS_FILE != null)
            return; //TODO read from the file
        for (int i = 0; i < NUM_FOOD_SOURCE; i++) {
            initializeFoodSource(i);
        }
    }

    private void sendEmployedBees() {
        for (int i = 0; i < NUM_FOOD_SOURCE; i++) {
            updateWithGBest(i);
        }

        if (RUN_CONCURRENT) {
            tasks.clear();
            for (double[] food: foods) {
                tasks.add(new PlayGame(food));
            }
            try {
                List<Future<Double>> results = es.invokeAll(tasks);
                for (int i = 0; i < results.size(); i++) {
                    double newFitness = results.get(i).get();
                    compareFitness(newFitness, i);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        } else { // single-threaded
            for (int i = 0; i < NUM_FOOD_SOURCE; i++) {
                double newFitness = calculateFitness(foods[i]);
                compareFitness(newFitness, i);
            }
        }
    }

    private void compareFitness(double newFitness, int i) {
        if (newFitness > fitness[i]) {
            fitness[i] = newFitness;
            trialCounts[i] = 0;
        } else {
            int j = parameterChanged[i];
            double old_value = parameterOldValue[i];
            foods[i][j] = old_value; // restore to the old value at x[j]
            trialCounts[i]++;
            if (trialCounts[i] >= LIMIT)
                foodSourceToAbandon = i;
        }
    }

    private void sendOnlookerBees() {
        for (int t = 0; t < NUM_FOOD_SOURCE; t++) {
            int i = selectFoodSource();
            updateWithGBest(i);
            foodSourceChanged[t] = i;
        }

        if (RUN_CONCURRENT) {
            tasks.clear();
            for (double[] food: foods) {
                tasks.add(new PlayGame(food));
            }
            try {
                List<Future<Double>> results = es.invokeAll(tasks);
                for (int t = 0; t < results.size(); t++) {
                    double newFitness = results.get(t).get();
                    compareFitness(newFitness, foodSourceChanged[t]);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            for (int t = 0; t < NUM_FOOD_SOURCE; t++) {
                int i = foodSourceChanged[t];
                double newFitness = calculateFitness(foods[i]);
                compareFitness(newFitness, i);
            }
        }
    }

    private int selectFoodSource() {
        double totalFitness = getSumFitness();
        double value = rand.nextDouble() * totalFitness;

        for (int i = 0; i < fitness.length; i++) {
            value -= fitness[i];
            if (value < 0)
                return i;
        }
        return fitness.length - 1;
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
     * randomly computes a mutated food source from foods[i]. Stores the index to the changed parameter and its old value
     * in the two arrays so that if the performance deteriorates, we can revert back
     * @param i the index of a food source
     */
    private void updateWithGBest(int i) {
        double[] xi = foods[i];
        int k; // randomly selected neighbour index
        int j; // randomly selected parameter index
        double old_value, update;
        double upper, lower;

        if (LIMIT_RANGE) {
            //TODO limit the search space to vectors of unit length, since c*W yields the same set of actions for all c. In the experiment, they run the optimization for roughly 13 times. Optimally, we would like to normalize the weight vectors => so just normalize every time?
            do {
                do {
                    k = (int) (rand.nextDouble() * NUM_FOOD_SOURCE);
                } while (k != i);

                j = (int) (rand.nextDouble() * NUM_PARAMETERS);
                old_value = xi[j];
                double[] xk = foods[k];
                update = xi[j] + 2*(rand.nextDouble() - 0.5) * (xi[j] - xk[j]) + (rand.nextDouble() * C) * (globalBestParameters[j] - xi[j]);
                upper = j == StateSimulator2.INDEX_ERODED_PIECE_CELLS ? 1:  UPPER;
                lower = j == StateSimulator2.INDEX_ERODED_PIECE_CELLS ? 0:  LOWER;
            } while (update > upper || update < lower);
        } else {
            do {
                k = (int) (rand.nextDouble() * NUM_FOOD_SOURCE);
            } while (k != i);

            // randomly selected parameter that will be updated
            j = (int) (rand.nextDouble() * NUM_PARAMETERS);
            old_value = xi[j];
            double[] xk = foods[k];

            update = xi[j] + 2*(rand.nextDouble() - 0.5) * (xi[j] - xk[j]) + (rand.nextDouble() * C) * (globalBestParameters[j] - xi[j]);

//            update = xi[j] + 2*(rand.nextDouble() - 0.5) * (xi[j] - xk[j]);
        }

        parameterChanged[i] = j;
        parameterOldValue[i] = old_value;

        // now try the game with updated weight vector.
        xi[j] = update;
    }

    private double getSumFitness() {
        double ans = 0;
        for (double fit: fitness) {
            ans += fit;
        }
        return ans;
    }

    private double[] normalizeVector(double[] vector) {
        double[] normalized = new double[vector.length];
        double sum = 0;
        for(double elem: vector) {
            sum += elem;
        }

        for (int i = 0; i < vector.length; i++) {
            normalized[i] = vector[i] / sum;
        }

        return normalized;

        // if we do this, we will need another array that stores ALL the elements
    }

}
