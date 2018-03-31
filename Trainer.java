/**
 * This is the main program for this project, as of now.
 */
public class Trainer {

    public static void main(String[] args) {
        System.out.println("Starting Trainer class. Format is: ");
        System.out.println("[numHolesWeight, heightDiffWeight, maxHeightWeight, rowsClearedWeight]");
        System.out.println("---------------------------");

        Cuckoo cuckoo = new Cuckoo();

        System.out.println("Starting Cuckoo Search");
        Solution finalSolution = cuckoo.getOptimumSolution();

        System.out.println(finalSolution);
        System.out.println("---------------------------");
    }
}