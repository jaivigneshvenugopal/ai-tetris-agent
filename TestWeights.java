import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private static int NUM_GAMES_TO_PLAY = 50;

    // set the weights to test here!
    private static double[] weights = {-0.55745977 , -0.15183443 , -0.00066615 , -0.73491890 , -0.12130472 , -0.23872920 , -0.07200741 , -0.22178176};

    public static void main (String[] args) throws InterruptedException, ExecutionException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();

        NUM_THREADS = Runtime.getRuntime().availableProcessors();
        System.out.println("# processors available = " + NUM_THREADS);
        System.out.println("Start Time: " + formatter.format(date));
        es = Executors.newFixedThreadPool(NUM_THREADS);

        PlayGame.NUM_GAMES_TO_AVERAGE = 1; // only run one game per thread since we are running each game concurrently
        PlayGame.setLevel(2); // usual difficulty

        tasks = new ArrayList<>(NUM_GAMES_TO_PLAY);

        for (int i = 0; i < NUM_GAMES_TO_PLAY; i++)
            tasks.add(new PlayGame(weights));

        double average = 0;
        List<Future<Double>> results = es.invokeAll(tasks);
        for (Future<Double> result: results) {
            average += result.get();
        }

        Debug.printRed("Average score = " + average / NUM_GAMES_TO_PLAY + " over " + NUM_GAMES_TO_PLAY + " games");
        System.out.println("End Time: " + formatter.format(date));
        System.exit(0);
    }
}
