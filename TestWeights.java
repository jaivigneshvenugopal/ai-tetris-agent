import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * A class to test the weights given by playing the game multiple times and retrieve the average score.
 * Runs the games in parallel to reduce running time.
 */
public class TestWeights {
    private static int NUM_THREADS;
    private static ExecutorService es;
    private static ArrayList<Callable<Double>> tasks;
    private static int NUM_GAMES_TO_PLAY = 80;

    private static String WEIGHTS_FILE = "weights.out";

    private static double[] scores = new double[NUM_GAMES_TO_PLAY];

    public static void main (String[] args) throws InterruptedException, ExecutionException, IOException {
        NUM_THREADS = Runtime.getRuntime().availableProcessors();

        System.out.println("# processors available = " + NUM_THREADS);
        es = Executors.newFixedThreadPool(NUM_THREADS);

        PlayGame.NUM_GAMES_TO_AVERAGE = 1;
        PlayGame.setLevel(1);

        ArrayList<double[]> weightsFromFile = getWeightsFromFile();
        for (double[] weight: weightsFromFile) {
            runGameWith(weight);
        }

        System.exit(0);
    }

    private static void runGameWith(double[] weights) throws InterruptedException, ExecutionException {
        tasks = new ArrayList<>(NUM_GAMES_TO_PLAY);
        double currentTime = System.currentTimeMillis();
        for (int i = 0; i < NUM_GAMES_TO_PLAY; i++)
            tasks.add(new PlayGame(weights));

        List<Future<Double>> results = es.invokeAll(tasks);
        int i = 0;
        for (Future<Double> result: results) {
            scores[i] = result.get();
            i++;
        }

        double endTime = System.currentTimeMillis();

        Arrays.sort(scores);
        double average = 0;
        for (double score: scores) {
            System.out.printf("%.3f\n", score);
            average += score;
        }

        double timeTaken = endTime - currentTime;
        System.out.printf("\nTime taken = %.5f seconds", timeTaken / 1000);
        average /= NUM_GAMES_TO_PLAY;
        System.out.println();
        System.out.println("Average = " + average);
    }

    private static ArrayList<double[]> getWeightsFromFile() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(WEIGHTS_FILE));
        String line;
        int count = 0;
        ArrayList<double[]> weights = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            double[] weight = new double[StateSimulator2.NUM_FEATURES];
            String[] elements = line.split(",");
            for (int i = 0; i < StateSimulator2.NUM_FEATURES; i++) {
                weight[i] = Double.parseDouble(elements[i].trim());
                System.out.printf("%.5f ", weight[i]);
            }
            System.out.println();
            weights.add(weight);
            count++;
        }
        System.out.printf("Testing %d weights\n", count);
        return weights;
    }
}
