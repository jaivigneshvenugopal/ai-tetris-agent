import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Cuckoo {

    private boolean DEBUG = true;

    protected final int N_NESTS;					//number of nests (solutions)
    protected final int N_OPTIMIZATIONS;			//number of generations
    protected final double DISCARD_RATIO;		    //ratio of worst solutions discarded

    ArrayList<Solution> solutions;

    Solution newSolution;
    Solution randomExistingSolution;
    Solution currentBestSolution;

    LevyDistribution levyDistribution;

    public Cuckoo() {
        N_NESTS = 15;
        N_OPTIMIZATIONS = 1000;
        DISCARD_RATIO = 0.5;
        currentBestSolution = new Solution();
        solutions = new ArrayList<>(15);
        levyDistribution = new LevyDistribution();
    }

    public Cuckoo(int N_NESTS, int N_OPTIMIZATIONS, double DISCARD_RATIO) {
        this.N_NESTS = N_NESTS;
        this.N_OPTIMIZATIONS = N_OPTIMIZATIONS;
        this.DISCARD_RATIO = DISCARD_RATIO;
        currentBestSolution = new Solution();
        solutions = new ArrayList<>(N_NESTS);
    }

    public Solution getOptimumSolution() {
        //Add first default solution defined by programmer
        solutions.add(new Solution());

        //Generate rest of the nests using the first default solution
        for (int nest = 1; nest < N_NESTS; nest++) {
            Solution newSolution = generateNewSolution();
            solutions.add(nest, newSolution);

            //Constantly checking if any of the new solution is better than the current best
            if (newSolution.getPerformanceMeasure() > currentBestSolution.getPerformanceMeasure()) {
                currentBestSolution = newSolution;
                if (DEBUG) {
                    System.out.println("Init 15 new nests. New best solution found!");
                    System.out.println("Current best is " + currentBestSolution);
                }
            }
        }

        //This section of the code deals with generating a new solution for every iteration
        for (int iterations = 0; iterations < N_OPTIMIZATIONS; iterations++) {
            if (DEBUG) {
                System.out.println("Init new solution, to replace one of 15");
            }

            newSolution = generateNewSolution();
            randomExistingSolution = getRandomExistingSolution();

            //If new solution is better than any of the random existing solution in any of the nests, it replaces it
            if (newSolution.getPerformanceMeasure() > randomExistingSolution.getPerformanceMeasure()) {
                if (DEBUG) {
                    System.out.println("New solution is better than one of 15");
                }
                replaceSolution(newSolution, randomExistingSolution);

                //If new solution is better than current best solution, it replaces it
                if (newSolution.getPerformanceMeasure() > currentBestSolution.getPerformanceMeasure()) {
                    if (DEBUG) {
                        System.out.println("New solution is better than current best!");
                        System.out.println("current best is " + currentBestSolution);
                    }
                    currentBestSolution = newSolution;
                }
            }
            if (DEBUG) {
                System.out.println("Discarding solutions now");
            }

            //This step removes the nests with poor performance. It then regenerates new solutions based on the current best solutions in the solution set
            discardSolutions();
        }

        return currentBestSolution;
    }

    //Returns a random solution from the current solution set
    public Solution getRandomExistingSolution() {
        Random rand = new Random();
        int random = rand.nextInt(solutions.size());
        return solutions.get(random);
    }

    //Removes the nests with poor performance. It then regenerates new solutions based on the current best solutions in the solution set
    public void discardSolutions() {
        //Sort in descending order
        Collections.sort(solutions, Collections.reverseOrder());

        printCurrentBestPerformanceMeasure();
        int numOfDiscardedSolutions = (int) (DISCARD_RATIO * N_NESTS);
        int numOfExistingSolutions = N_NESTS - numOfDiscardedSolutions;

        //Regenerates solutions for empty slots
        for (int nest = numOfExistingSolutions; nest < N_NESTS; nest++) {
            Solution newSolution = generateNewSolution();
            solutions.set(nest, newSolution);
        }
    }

    //Print current solution set, for debugging purpose
    public void printCurrentBestPerformanceMeasure() {
        ArrayList<Double> performanceMeasureValues = new ArrayList<>(15);
        for (int i = 0 ; i < solutions.size(); i++) {
            performanceMeasureValues.add(solutions.get(i).getPerformanceMeasure());
        }
        System.out.println(performanceMeasureValues);
    }

    public Solution generateNewSolution() {

        double levySteps = levyDistribution.sample(2.0);

        Solution randomExistingSolution = getRandomExistingSolution();

        double newNumHoles = levySteps + randomExistingSolution.COE_NUM_HOLES;
        double newHeightDiff = levySteps + randomExistingSolution.COE_HEIGHT_DIFF;
        double newMaxHeight = levySteps + randomExistingSolution.COE_MAX_HEIGHT;
        double newRowsCleared = levySteps + randomExistingSolution.COE_ROWS_CLEARED;

        Solution newSolution = new Solution(newNumHoles, newHeightDiff, newMaxHeight, newRowsCleared);
        newSolution.calculatePerformanceMeasure();

        return newSolution;
    }

    public void replaceSolution(Solution newSolution, Solution randomExistingSolution) {
        solutions.set(solutions.indexOf(randomExistingSolution), newSolution);
    }

}

class Solution implements Comparable {

    private boolean DEBUG = true;

    public double COE_NUM_HOLES;
    public double COE_HEIGHT_DIFF;
    public double COE_MAX_HEIGHT;
    public double COE_ROWS_CLEARED;
    private double PERFORMANCE_MEASURE;

    public Solution() {
        COE_NUM_HOLES = -19;
        COE_HEIGHT_DIFF = -4;
        COE_MAX_HEIGHT = 0;
        COE_ROWS_CLEARED = 3;
        PERFORMANCE_MEASURE = 500;
    }

    public Solution(double COE_NUM_HOLES, double COE_HEIGHT_DIFF, double COE_MAX_HEIGHT, double COE_ROWS_CLEARED) {
        this.COE_NUM_HOLES = COE_NUM_HOLES;
        this.COE_HEIGHT_DIFF = COE_HEIGHT_DIFF;
        this.COE_MAX_HEIGHT = COE_MAX_HEIGHT;
        this.COE_ROWS_CLEARED = COE_ROWS_CLEARED;
    }

    public void calculatePerformanceMeasure() {
        Heuristics.setWeights(COE_NUM_HOLES, COE_HEIGHT_DIFF, COE_MAX_HEIGHT, COE_ROWS_CLEARED);
        PlayerSkeleton player = new PlayerSkeleton();
        int sumFitness = 0;
        for (int i = 0; i < 100; i++) {
            sumFitness += player.run();
        }
        if (DEBUG) {
            System.out.println("Calculating total performance value is : " + sumFitness);
        }
        PERFORMANCE_MEASURE = sumFitness/100.0;
    }

    public double getPerformanceMeasure() {
        return PERFORMANCE_MEASURE;
    }

    public int compareTo(Object solution) {
        return (int) (this.PERFORMANCE_MEASURE - ((Solution)solution).PERFORMANCE_MEASURE);
    }

    @Override
    public String toString() {
        return "COE_NUM_HOLES " + COE_NUM_HOLES + " COE_HEIGHT_DIFF " + COE_HEIGHT_DIFF + " COE_MAX_HEIGHT " + COE_MAX_HEIGHT + " COE_ROW_CLEARED " + COE_ROWS_CLEARED + " PERFORMANCE_MEASURE " + PERFORMANCE_MEASURE;
    }
 }