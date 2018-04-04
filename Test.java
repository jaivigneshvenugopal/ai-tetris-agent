public class Test {
    public static void run() {
        while (true) {
            LevyDistribution levyDistribution = new LevyDistribution();
            double steps = 0 - levyDistribution.sample_positive(2.0, 5.0);
            System.out.println("Current step count is " + steps);
        }
    }

    public static void main(String[] args) {
        run();
    }
}
