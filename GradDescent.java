import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

public class GradDescent {

    private static final double epsilon = Double.MIN_VALUE;

    public static void main(String[] args) {
        new GradDescent().run();
    }

    private void run() {
        List<Point2D> data = loadData();
        double alpha = 0.01;
        int maxIterations = 10_000;
        Point2D finalTheta = singleVarGradientDescent(data, 0.1, 0.1, alpha, maxIterations);
        System.out.printf("theta0 = %f, theta1 = %f", finalTheta.getX(), finalTheta.getY());
    }

    private Point2D singleVarGradientDescent(List<Point2D> data, double initialTheta0, double initialTheta1, double alpha, int maxIterations) {
        double theta0 = initialTheta0, theta1 = initialTheta1;
        double oldTheta0 = 0, oldTheta1 = 0;

        for (int i = 0 ; i < maxIterations; i++) {
            if (hasConverged(oldTheta0, theta0) && hasConverged(oldTheta1, theta1))
                break;

            oldTheta0 = theta0;
            oldTheta1 = theta1;

            theta0 = theta0 - (alpha * gradientofThetaN(theta0, theta1, data, x -> 1.0));
            theta1 = theta1 - (alpha * gradientofThetaN(theta0, theta1, data, x -> x));
        }
        return new Point2D.Double(theta0, theta1);
    }

    private boolean hasConverged(double old, double current) {
        return (current - old) < epsilon;
    }

    private double gradientofThetaN(double theta0, double theta1, List<Point2D> data, DoubleUnaryOperator factor) {
        double m = data.size();
        return (1.0 / m) * sigma(data, (x, y) ->  (hypothesis(theta0, theta1, x) - y) * factor.applyAsDouble(x));
    }

    private double hypothesis(double theta0, double theta1, double x) {
        return theta0 + (theta1 * x);
    }

    private double sigma(List<Point2D> data, DoubleBinaryOperator inner) {
        return data.stream()
                .mapToDouble(theta -> {
                    double x = theta.getX(), y = theta.getY();
                    return inner.applyAsDouble(x, y);
                })
                .sum();
    }

    private List<Point2D> loadData() {
        List<Point2D> data = new ArrayList<>();
        data.add(new Point2D.Double(1, 2));
        data.add(new Point2D.Double(2, 3));
        data.add(new Point2D.Double(3, 4));
        data.add(new Point2D.Double(4, 5));
        data.add(new Point2D.Double(5, 6));
        data.add(new Point2D.Double(6, 7));
        return data;
    }

}
