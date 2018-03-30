public class Test {
    public static void run() {
        while (true) {
            LevyDistribution levyDistribution = new LevyDistribution();
            double steps = levyDistribution.sample(2.0);
            System.out.println("Current step count is " + steps);
        }
    }

    public static void main(String[] args) {
        run();
    }
}
