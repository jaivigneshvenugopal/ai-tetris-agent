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

    int COE_NUM_HOLES = -30;
    int COE_HEIGHT_DIFF= -6;
    int COE_MAX_HEIGHT = -5;
    int COE_ROWS_CLEARED = -6;


    public Cuckoo() {
        N_NESTS = 15;
        N_OPTIMIZATIONS = 10;
        DISCARD_RATIO = 0.1;
        currentBestSolution = new Solution();
        solutions = new ArrayList<>(15);
    }

    public Cuckoo(int N_NESTS, int N_OPTIMIZATIONS, double DISCARD_RATIO) {
        this.N_NESTS = N_NESTS;
        this.N_OPTIMIZATIONS = N_OPTIMIZATIONS;
        this.DISCARD_RATIO = DISCARD_RATIO;
        currentBestSolution = new Solution();
        solutions = new ArrayList<>(N_NESTS);
    }

    public Solution getOptimumSolution() {
        for (int nest = 0; nest < N_NESTS; nest++) {
            Solution newSolution = generateNewSolution();
            solutions.add(nest, newSolution);
            if (newSolution.getUtility() > currentBestSolution.getUtility()) {
                currentBestSolution = newSolution;
                if (DEBUG) {
                    System.out.println("Init 15 new nests. New best solution found!");
                    System.out.println("Current best is " + currentBestSolution);
                }
            }
        }

        for (int iterations = 0; iterations < N_OPTIMIZATIONS; iterations++) {
            if (DEBUG) {
                System.out.println("Init new solution, to replace one of 15");
            }
            newSolution = generateNewSolution();
            randomExistingSolution = getRandomExistingSolution();

            if (newSolution.getUtility() > randomExistingSolution.getUtility()) {
                if (DEBUG) {
                    System.out.println("New solution is better than one of 15");
                }
                replaceSolution(newSolution, randomExistingSolution);
                if (newSolution.getUtility() > currentBestSolution.getUtility()) {
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
            discardSolutions();
        }

        return currentBestSolution;
    }

    public Solution getRandomExistingSolution() {
        Random rand = new Random();
        int random = rand.nextInt(N_NESTS);
        return solutions.get(random);
    }

    public void discardSolutions() {
        Collections.sort(solutions, Collections.reverseOrder());
        int numOfDiscardedSolutions = (int) (DISCARD_RATIO * N_NESTS);
        int numOfExistingSolutions = N_NESTS - numOfDiscardedSolutions;
        for (int nest = numOfExistingSolutions; nest < N_NESTS; nest++) {
            Solution newSolution = generateNewSolution();
            solutions.set(nest, newSolution);
        }
    }

    public Solution generateNewSolution() {

        Random rand = new Random();
        double value = rand.nextDouble();
        if(value < 0.125) {
            COE_NUM_HOLES++;
        }
        else if (value < 0.25) {
            COE_NUM_HOLES--;
        }
        else if (value <0.375) {
            COE_HEIGHT_DIFF++;
        }
        else if (value < 0.5) {
            COE_HEIGHT_DIFF--;
        }
        else if (value < 0.625) {
            COE_MAX_HEIGHT++;
        }
        else if (value < 0.75) {
            COE_MAX_HEIGHT--;
        }
        else if (value < 0.875) {
            COE_ROWS_CLEARED++;
        }
        else {
            COE_ROWS_CLEARED--;
        }

        Solution newSolution = new Solution(COE_NUM_HOLES, COE_HEIGHT_DIFF, COE_MAX_HEIGHT, COE_ROWS_CLEARED);
        newSolution.calculateUtility();

        return newSolution;
    }

    public void replaceSolution(Solution newSolution, Solution randomExistingSolution) {
        solutions.set(solutions.indexOf(randomExistingSolution), newSolution);
    }

}

class Solution implements Comparable {

    private boolean DEBUG = true;

    private int COE_NUM_HOLES;
    private int COE_HEIGHT_DIFF;
    private int COE_MAX_HEIGHT;
    private int COE_ROWS_CLEARED;
    private double UTILITY;

    public Solution() {
        COE_NUM_HOLES = -30;
        COE_HEIGHT_DIFF = -2;
        COE_MAX_HEIGHT = -3;
        COE_ROWS_CLEARED = -4;
        UTILITY = 50;
    }

    public Solution(int COE_NUM_HOLES, int COE_HEIGHT_DIFF, int COE_MAX_HEIGHT, int COE_ROWS_CLEARED) {
        this.COE_NUM_HOLES = COE_NUM_HOLES;
        this.COE_HEIGHT_DIFF = COE_HEIGHT_DIFF;
        this.COE_MAX_HEIGHT = COE_MAX_HEIGHT;
        this.COE_ROWS_CLEARED = COE_ROWS_CLEARED;
    }

    public void calculateUtility() {
        Heuristics.setWeights((double) COE_NUM_HOLES, (double) COE_HEIGHT_DIFF, (double) COE_MAX_HEIGHT, (double) COE_ROWS_CLEARED);
        PlayerSkeleton player = new PlayerSkeleton();
        int sumFitness = 0;
        for (int i = 0; i < 20; i++) {
            sumFitness += player.run();
        }
        if (DEBUG) {
            System.out.println("Calculating total utility...value is : " + sumFitness);
        }
        UTILITY = sumFitness/20.0;
    }

    public double getUtility() {
        return UTILITY;
    }

    public int compareTo(Object solution) {
        return (int) (this.UTILITY - ((Solution)solution).UTILITY);
    }

    @Override
    public String toString() {
        return "COE_NUM_HOLES " + COE_NUM_HOLES + " COE_HEIGHT_DIFF " + COE_HEIGHT_DIFF + " COE_MAX_HEIGHT " + COE_MAX_HEIGHT + " COE_ROW_CLEARED " + COE_ROWS_CLEARED + " UTILITY " + UTILITY;
    }
 }