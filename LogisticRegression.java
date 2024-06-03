package Atom;

public class LogisticRegression {
    // Coefficients obtained from the trained model
    private static final double BETA0 = -1.61429836e-31;
    private static final double BETA1 = -2.45241207e-17;  // Coefficient for Distance
    private static final double BETA2 = 2.88744405e-17;   // Coefficient for Velocity
    private static final double BETA3 = -1.99580462e-17;  // Coefficient for EnemyEnergy
    private static final double BETA4 = -2.16393804e-31;  // Coefficient for RobotVelocity
    private static final double BETA5 = -8.88377758e-18;  // Coefficient for AbsoluteBearing

    // Linear function to calculate the value of z
    private static double linearFunction(double distance, double velocity, double enemyEnergy, double robotVelocity, double absoluteBearing) {
        // The linear function is a linear combination of the coefficients and input variables
        return BETA0 + BETA1 * distance + BETA2 * velocity + BETA3 * enemyEnergy + BETA4 * robotVelocity + BETA5 * absoluteBearing;
    }

    // Logistic function to convert the value of z into a probability
    private static double logisticFunction(double z) {
        // The logistic function transforms the value of z into a probability between 0 and 1
        return 1 / (1 + Math.exp(-z));
    }

    // Public method to get the hit probability
    public static double getHitProbability(double distance, double velocity, double enemyEnergy, double robotVelocity, double absoluteBearing) {
        // Calculate the value of z using the linear function
        double z = linearFunction(distance, Math.abs(velocity), enemyEnergy, robotVelocity, absoluteBearing);
        // Convert the value of z into a probability using the logistic function
        return logisticFunction(z);
    }
}