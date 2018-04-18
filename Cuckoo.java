import sun.plugin2.message.CustomSecurityManagerAckMessage;

import javax.swing.plaf.synth.SynthEditorPaneUI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;


/*Version 1 of Cuckoo*/
//Levy steps is only applied to one of the 4 weights

public class Cuckoo {

    private boolean DEBUG = true;


    protected final int N_NESTS;					//number of nests (solutions)
    protected final int N_OPTIMIZATIONS;			//number of generations
    protected final double DISCARD_RATIO;		    //ratio of worst solutions discarded

    int notableSizeofSolutionSet = 0;

    ArrayList<Solution> solutions;

    Solution newSolution;
    Solution randomExistingSolution;
    Solution currentBestSolution;

    LevyDistribution levyDistribution;

    public Cuckoo() {
        N_NESTS = 15;
        N_OPTIMIZATIONS = 1000;
        DISCARD_RATIO = 0.2;
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
        notableSizeofSolutionSet++;

        //Generate rest of the nests using the first default solution
        for (int nest = 1; nest < N_NESTS; nest++) {
            Solution newSolution = generateInitialNewSolution();
            solutions.add(nest, newSolution);
            notableSizeofSolutionSet++;

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

            System.out.println("-------------------ALGO IS AT ITERATION " + iterations + " ---------------------------");

            newSolution = generateNewSolution(notableSizeofSolutionSet);
            randomExistingSolution = getRandomExistingSolution(notableSizeofSolutionSet);

            //If new solution is better than any of the random existing solution in any of the nests, it replaces it
            if (newSolution.getPerformanceMeasure() > randomExistingSolution.getPerformanceMeasure()) {
                if (DEBUG) {
                    System.out.println("New solution is better than one of 15");
                    System.out.println("new sol is " + newSolution.getPerformanceMeasure());
                    System.out.println("old sol is " + randomExistingSolution.getPerformanceMeasure());
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
    public Solution getRandomExistingSolution(int notableSizeofSolutionSet) {
        Random rand = new Random();
        int random = rand.nextInt(notableSizeofSolutionSet);
        return solutions.get(random);
    }

    //Removes the nests with poor performance. It then regenerates new solutions based on the current best solutions in the solution set
    public void discardSolutions() {
        //Sort in descending order
        Collections.sort(solutions);
        Collections.reverse(solutions);

        printCurrentBestPerformanceMeasure();

        int numOfDiscardedSolutions = (int) (DISCARD_RATIO * N_NESTS);
        notableSizeofSolutionSet = N_NESTS - numOfDiscardedSolutions;

        //Regenerates solutions for empty slots
        for (int nest = notableSizeofSolutionSet; nest < N_NESTS; nest++) {
            Solution newSolution = generateNewSolution(notableSizeofSolutionSet);
            solutions.set(nest, newSolution);
        }

        notableSizeofSolutionSet = N_NESTS;
    }

    //Print current solution set, for debugging purpose
    public void printCurrentBestPerformanceMeasure() {
        ArrayList<Double> performanceMeasureValues = new ArrayList<>(15);
        for (int i = 0 ; i < solutions.size(); i++) {
            performanceMeasureValues.add(solutions.get(i).getPerformanceMeasure());
        }
        System.out.println("performance measure values : " + performanceMeasureValues);
    }
    public Solution generateInitialNewSolution() {
        Random randomGenerator = new Random();
        int randNum1 = - randomGenerator.nextInt(1000);
        int randNum2 = - randomGenerator.nextInt(1000);
        int randNum3 = - randomGenerator.nextInt(1000);
        int randNum4 = randomGenerator.nextInt(1000);
        int randNum5 = - randomGenerator.nextInt(1000);
        int randNum6 = - randomGenerator.nextInt(1000);
        int randNum7 = - randomGenerator.nextInt(1000);
        int randNum8 = - randomGenerator.nextInt(1000);
        int randNum9 = - randomGenerator.nextInt(1000);
        int randNum10 = - randomGenerator.nextInt(1000);
        int randNum11 = - randomGenerator.nextInt(1000);

        double newNumHoles = randNum1;
        double newHeightDiff = randNum2;
        double newMaxHeight = randNum3;
        double newRowsCleared = randNum4;
        double newGameLost = -10000000;
        double newColTrans = randNum5;
        double newHoleDepth = randNum6;
        double numRowsWithHole = randNum7;
        double newErodedPieces = randNum8;
        double newLandingHeight = randNum9;
        double newRowTrans = randNum10;
        double newCumalativeWells =randNum11;

        Solution newSolution = new Solution(newNumHoles, newHeightDiff, newMaxHeight, newRowsCleared, newGameLost, newColTrans, newHoleDepth, numRowsWithHole, newErodedPieces, newLandingHeight, newRowTrans, newCumalativeWells);
        newSolution.calculatePerformanceMeasure();

        return newSolution;
    }

    public Solution generateNewSolution(int notableSizeofSolutionSet) {

        double levySteps1 = - levyDistribution.sample_positive(2.0, 10.0);
        double levySteps2 = - levyDistribution.sample_positive(2.0, 10.0);
        double levySteps3 = - levyDistribution.sample_positive(2.0, 10.0);
        double levySteps4 = levyDistribution.sample_positive(2.0, 10.0);
        double levySteps5 = - levyDistribution.sample_positive(2.0, 10.0);
        double levySteps6 = - levyDistribution.sample_positive(2.0, 10.0);
        double levySteps7 = - levyDistribution.sample_positive(2.0, 10.0);
        double levySteps8 = - levyDistribution.sample_positive(2.0, 10.0);
        double levySteps9 = - levyDistribution.sample_positive(2.0, 10.0);
        double levySteps10 = - levyDistribution.sample_positive(2.0, 10.0);
        double levySteps11 = - levyDistribution.sample_positive(2.0, 10.0);

        Solution randomExistingSolution = getRandomExistingSolution(notableSizeofSolutionSet);

        Random randomGenerator = new Random();
        int randNum = randomGenerator.nextInt(11);

        double newNumHoles = randomExistingSolution.COE_NUM_HOLES;
        double newHeightDiff = randomExistingSolution.COE_HEIGHT_DIFF;
        double newMaxHeight = randomExistingSolution.COE_MAX_HEIGHT;
        double newRowsCleared = randomExistingSolution.COE_ROWS_CLEARED;
        double newGameLost = randomExistingSolution.COE_LOST;
        double newColTrans = randomExistingSolution.COL_TRANSITIONS;
        double newHoleDepth = randomExistingSolution.HOLE_DEPTH;
        double numRowsWithHole = randomExistingSolution.NUM_ROWS_WITH_HOLE;
        double newErodedPieces = randomExistingSolution.ERODED_PIECE_CELLS;
        double newLandingHeight = randomExistingSolution.LANDING_HEIGHT;
        double newRowTrans = randomExistingSolution.ROW_TRANSITIONS;
        double newCumalativeWells = randomExistingSolution.CUMULATIVE_WELLS;

        switch (randNum) {
            case 0: newNumHoles += levySteps1;
                    break;
            case 1: newHeightDiff += levySteps2;
                    break;
            case 2: newMaxHeight += levySteps3;
                    break;
            case 3: newRowsCleared += levySteps4;
                    break;
            case 4: newColTrans += levySteps5;
                break;
            case 5: newHoleDepth += levySteps6;
                break;
            case 6: numRowsWithHole += levySteps7;
                break;
            case 7: newErodedPieces += levySteps8;
                break;
            case 8: newLandingHeight += levySteps9;
                break;
            case 9: newRowTrans += levySteps10;
                break;
            case 10: newCumalativeWells += levySteps11;
                break;
        }

        Solution newSolution = new Solution(newNumHoles, newHeightDiff, newMaxHeight, newRowsCleared, newGameLost, newColTrans, newHoleDepth, numRowsWithHole, newErodedPieces, newLandingHeight, newRowTrans, newCumalativeWells);
        newSolution.calculatePerformanceMeasure();

        return newSolution;
    }

    public void replaceSolution(Solution newSolution, Solution randomExistingSolution) {
        solutions.set(solutions.indexOf(randomExistingSolution), newSolution);
    }

}

class Solution implements Comparable {

    private boolean DEBUG = false;

    public double COE_NUM_HOLES;
    public double COE_HEIGHT_DIFF;
    public double COE_MAX_HEIGHT;
    public double COE_ROWS_CLEARED;
    public double COE_LOST;
    public double COL_TRANSITIONS;
    public double HOLE_DEPTH;
    public double NUM_ROWS_WITH_HOLE;
    public double ERODED_PIECE_CELLS;
    public double LANDING_HEIGHT;
    public double ROW_TRANSITIONS;
    public double CUMULATIVE_WELLS;
    private double PERFORMANCE_MEASURE;

    public Solution() {
        COE_NUM_HOLES = -196.59170650618145;
        COE_HEIGHT_DIFF = -24.38282096450111;
        COE_MAX_HEIGHT = 7.384281915256887;
        COE_ROWS_CLEARED = 13.254838114099638;
        COE_LOST = -10000000;
        COL_TRANSITIONS = -31.908886830653845;
        HOLE_DEPTH = -2.24152962603214;
        NUM_ROWS_WITH_HOLE = -73.20954912306729;
        ERODED_PIECE_CELLS = 0;
        LANDING_HEIGHT = -36.74466452169317;
        ROW_TRANSITIONS = -3.9610915186662368;
        CUMULATIVE_WELLS = -51.21263504379763;
        PERFORMANCE_MEASURE = -51.21263504379763;
    }

    public Solution(double COE_NUM_HOLES, double COE_HEIGHT_DIFF, double COE_MAX_HEIGHT, double COE_ROWS_CLEARED, double COE_LOST, double COL_TRANSITIONS,
            double HOLE_DEPTH, double NUM_ROWS_WITH_HOLE, double ERODED_PIECE_CELLS, double LANDING_HEIGHT, double ROW_TRANSITIONS, double CUMULATIVE_WELLS) {
        this.COE_NUM_HOLES = COE_NUM_HOLES;
        this.COE_HEIGHT_DIFF = COE_HEIGHT_DIFF;
        this.COE_MAX_HEIGHT = COE_MAX_HEIGHT;
        this.COE_ROWS_CLEARED = COE_ROWS_CLEARED;
        this.COE_LOST = COE_LOST;
        this.COL_TRANSITIONS = COL_TRANSITIONS;
        this.HOLE_DEPTH = HOLE_DEPTH;
        this.NUM_ROWS_WITH_HOLE = NUM_ROWS_WITH_HOLE;
        this.ERODED_PIECE_CELLS = ERODED_PIECE_CELLS;
        this.LANDING_HEIGHT = LANDING_HEIGHT;
        this.ROW_TRANSITIONS = ROW_TRANSITIONS;
        this.CUMULATIVE_WELLS = CUMULATIVE_WELLS;
    }

    public void calculatePerformanceMeasure() {
        Heuristics.setWeights(COE_NUM_HOLES, COE_HEIGHT_DIFF, COE_MAX_HEIGHT, COE_ROWS_CLEARED, COE_LOST, COL_TRANSITIONS,
                HOLE_DEPTH, NUM_ROWS_WITH_HOLE, ERODED_PIECE_CELLS, LANDING_HEIGHT, ROW_TRANSITIONS, CUMULATIVE_WELLS);
        System.out.println("CURRENT CONFIG IN TEST: " + "[" + COE_NUM_HOLES + ", " + COE_HEIGHT_DIFF + ", " + COE_MAX_HEIGHT + ", " + COE_ROWS_CLEARED + ", "
                + COE_LOST + ", " + COL_TRANSITIONS + ", " + HOLE_DEPTH + ", " + NUM_ROWS_WITH_HOLE + ", " + ERODED_PIECE_CELLS + ", " + LANDING_HEIGHT + ", " + ROW_TRANSITIONS + ", " + CUMULATIVE_WELLS + "]");
        PlayerSkeleton player = new PlayerSkeleton();
        double[] fitnessValues = new double[101];
        for (int i = 0; i < 101; i++) {
            fitnessValues[i] = player.run();
        }

        Arrays.sort(fitnessValues);

        PERFORMANCE_MEASURE = fitnessValues[51];


        System.out.println("Calculating performance value : " + PERFORMANCE_MEASURE);
    }

    public double getPerformanceMeasure() {
        return PERFORMANCE_MEASURE;
    }

    public int compareTo(Object solution) {
        double difference = this.PERFORMANCE_MEASURE - ((Solution)solution).PERFORMANCE_MEASURE;
        if (difference > 0) {
            return 1;
        }
        else if (difference < 0) {
            return -1;
        }
        else {
            return 0;
        }
    }

    @Override
    public String toString() {
        String solution;
        solution = "Num Holes : " + COE_NUM_HOLES + "\n" + "Height Diff : " + COE_HEIGHT_DIFF + "\n" + "Max Height : " + COE_MAX_HEIGHT + "\n" + "Rows Cleared : " + COE_ROWS_CLEARED + "\n" +
                    "Coe Lost : " + COE_LOST + "\n" + "Col Transitions : " + COL_TRANSITIONS + "\n" + "Hole Depth : " + HOLE_DEPTH + "\n" + "Num of Rows with Holes : " +
                    NUM_ROWS_WITH_HOLE + "\n" + "Eroded Piece Cells : " + ERODED_PIECE_CELLS + "\n" + "Landing Height : " + LANDING_HEIGHT + "\n" + "Row Transitions : " +
                    ROW_TRANSITIONS + "\n" + "Cumulative Wells : " + CUMULATIVE_WELLS + "\n";
        return solution;
    }
 }