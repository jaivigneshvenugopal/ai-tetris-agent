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
 * Implementation of (Gbest-guided) Artificial Bee Colony algorithm.
 */
public class ABColony {
    /* Control Parameters of ABC algorithm */

    /**
     * A colony consists of
     * 1. employee bees
     * 2. onlooker bees
     * 3. scout bee (limited to only 1)
     * 50 is obtained from research
     */
    public static final int COLONY_SIZE = 80;

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

    private static final int NUM_PARAMETERS = StateSimulator2.NUM_FEATURES;

    private double[][] foods          = new double[NUM_FOOD_SOURCE][NUM_PARAMETERS];
    private double[][] foods_modified = new double[NUM_FOOD_SOURCE][NUM_PARAMETERS];

    /**
     * stores the fitness value for each of the food sources/locations. Fitness values are average/median scores of games played using with a particular set of weights.
     */
    private double[] fitness = new double[NUM_FOOD_SOURCE];

    /**
     * stores the number of trials made for each food source/location. Once this exceeds LIMIT, that food source is abandoned and the employee bee for that food source will become a scout to initialize a random food source.
     */
    private double[] trialCounts = new double[NUM_FOOD_SOURCE];

    private int foodSourceToAbandon  = -1;

    private double localBestFitness = Double.NEGATIVE_INFINITY;
    private double[] localBestParameters = new double[NUM_PARAMETERS];

    /**
     * This is the constant that defines the rand() range for the update in g-best algorithm.
     */
    private static final int C = 2;

    private double globalBestFitness = Double.NEGATIVE_INFINITY;
    private double[] globalBestParameters = new double[NUM_PARAMETERS];

    /* For parallel computing */
    private static final boolean RUN_CONCURRENT = true;
    private final ExecutorService es;
    private final int NUM_THREADS;
    private ArrayList<PlayGame> tasks;

    /* stores the index of parameter being changed for playing a game for each food source. */
    private int[] parameterChanged = new int[NUM_FOOD_SOURCE];
    private double[] parameterOldValue = new double[NUM_FOOD_SOURCE];
    private Random rand = new Random();

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
        ABColony colony = new ABColony();
        colony.initializeFoodSources();
        colony.memorizeBestSource();
        printResult(colony);
        PlayGame.setLevel(3);
        for (int i = 1; i < MAX_CYCLE; i++) {
            colony.sendEmployedBees();
            colony.sendOnlookerBees();
            colony.memorizeBestSource();
            colony.sendScoutBees();
            if (i % 10 == 0) {
                System.out.println("Iteration #" + i);
                printResult(colony);
            }
        }

        colony.es.shutdown();

        if (WEIGHTS_RESULT_FILE != null) {
            writeOptimalWeightsToFile(colony);
        }
        System.exit(0);
    }

    private static void writeOptimalWeightsToFile(ABColony colony) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(System.currentTimeMillis() + WEIGHTS_RESULT_FILE));
        bw.write(colony.globalBestFitness + "\n");
        for (double weight: colony.globalBestParameters) {
            bw.write(Double.toString(weight) + " ");
        }

        bw.flush();
        bw.close();
    }

    private static void printResult(ABColony bee) {
        double[] weights = bee.globalBestParameters;
        Debug.printBold("optimum = " + bee.globalBestFitness + "\n");
        for (double weight : weights) {
            System.out.printf(" %.8f ,", weight);
        }
        System.out.println("\n-----------------------------------------");
    }

    /**
     * @param foodSource food source to run the game with
     * @return the average/median score of games played with foodSource
     */
    private double calculateFitness(double[] foodSource) {
        tasks.clear();
        tasks.add(new PlayGame(foodSource, 0));
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
        normalizeVector(foods[foodIndex]);
        // since we only have 1 scout bee, it is ok to use single thread
        fitness[foodIndex] = calculateFitness(foods[foodIndex]);

        // since this food source has just been found, reset trialCounts
        trialCounts[foodIndex] = 0;
    }

    /*initializes all the food sources before optimization */
    private void initializeFoodSources() {
        if (INITIAL_WEIGHTS_FILE != null)
            return; //TODO read from the file
        for (int i = 0; i < NUM_FOOD_SOURCE; i++) {
            initializeFoodSource(i);
        }
    }

    private void sendEmployedBees() {
        if (RUN_CONCURRENT) {
            int foodIndex = 0;
            while (foodIndex < NUM_FOOD_SOURCE) {
                tasks.clear();
                for (int count = 0; count < NUM_THREADS && foodIndex < NUM_FOOD_SOURCE; count++) {
                    updateWithGBest(foodIndex, foodIndex);
                    //TODO
                    normalizeVector(foods_modified[foodIndex]);
                    tasks.add(new PlayGame(foods_modified[foodIndex], foodIndex, foodIndex));
                    foodIndex++;
                }
                try {
                    List<Future<Double>> results = es.invokeAll(tasks);
                    for (int i = 0; i < results.size(); i++) {
                        double newFitness = results.get(i).get();
                        int index = tasks.get(i).foodIndex;

                        if (newFitness > fitness[index]) {
                            fitness[index] = newFitness;
                            trialCounts[index] = 0;
                            // update the entire array
                            System.arraycopy(foods_modified[index], 0, foods[index], 0, NUM_PARAMETERS);
                        } else {
                            trialCounts[index]++;
                            if (trialCounts[index] >= LIMIT)
                                foodSourceToAbandon = index;
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            } // end while
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
        if (RUN_CONCURRENT) {
            int bee = 0;
            while (bee < NUM_FOOD_SOURCE) {
                tasks.clear();
                for (int count = 0; count < NUM_THREADS && bee < NUM_FOOD_SOURCE; count++) {
                    int foodIndex = selectFoodSource();
                    updateWithGBest(foodIndex, bee);
                    //TODO
                    normalizeVector(foods_modified[bee]);
                    tasks.add(new PlayGame(foods_modified[bee], foodIndex, bee));
                    bee++;
                }
                try {
                    List<Future<Double>> results = es.invokeAll(tasks);
                    for (int i = 0; i < results.size(); i++) {
                        double newFitness = results.get(i).get();
                        int foodIndex = tasks.get(i).foodIndex;
                        int beeIndex = tasks.get(i).beeIndex;

                        if (newFitness > fitness[foodIndex]) {
                            fitness[foodIndex] = newFitness;
                            trialCounts[foodIndex] = 0;
                            // update the entire array
                            System.arraycopy(foods_modified[beeIndex], 0, foods[foodIndex], 0, NUM_PARAMETERS);
                        } else {
                            trialCounts[foodIndex]++;
                            if (trialCounts[foodIndex] >= LIMIT)
                                foodSourceToAbandon = foodIndex;
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            } // end while
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
    private void updateWithGBest(int i, int t) {
        boolean isOnlookerPhase = t != -1;
        double[] xi;
        if (isOnlookerPhase) { // if this is called during onlooker phase
            xi = foods_modified[t];
            System.arraycopy(foods[i], 0, xi, 0, NUM_PARAMETERS);
        } else {
            xi = foods[i];
        }

        int k; // randomly selected neighbour index
        int j; // randomly selected parameter index
        double old_value, update;
        double upper, lower;

        if (LIMIT_RANGE) {
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

            // if not limit the range
        } else {
            do {
                k = (int) (rand.nextDouble() * NUM_FOOD_SOURCE);
            } while (k != i);

            // randomly selected parameter that will be updated
            j = (int) (rand.nextDouble() * NUM_PARAMETERS);
            old_value = xi[j];
            double[] xk = foods[k];

            // the amount of change is too small?
            double rand1 = 2*(rand.nextDouble() - 0.5); // [-1, 1]
            while (Math.abs(rand1) < 0.1) {
                rand1 *= 10;
            }

            double rand2 = rand.nextDouble();
            while (Math.abs(rand1) < 0.1) {
                rand1 *= 10;
            }

            update = xi[j] + rand1 * (xi[j] - xk[j]) + (rand2 * C) * (globalBestParameters[j] - xi[j]);
        }

        if (!isOnlookerPhase) {
            // don't copy the entire array but only change one parameter and save the old value
            parameterChanged[i] = j;
            parameterOldValue[i] = old_value;
        }
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

    /**
     * normalizes a given vector to make it of unit length
     * @param vector vector to be normalized
     */
    private void normalizeVector(double[] vector) {
        double sum = 0;
        for(double elem: vector) {
            sum += Math.pow(elem, 2);
        }

        sum = Math.sqrt(sum);

        for (int i = 0; i < vector.length; i++) {
            vector[i] /= sum;
        }
    }

}
