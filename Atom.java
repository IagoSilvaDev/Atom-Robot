package Atom;

import robocode.*;
import java.awt.Color;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import robocode.util.Utils;
import static robocode.util.Utils.normalRelativeAngleDegrees;

public class Atom extends AdvancedRobot {

    // Movement variables
    boolean nearWall; // Boolean to check if we are close to the wall
    int dir = 1; // Dictates the robot's direction
    double fieldWidth; // Battlefield width
    double fieldHeight; // Arena height
    boolean movingForward; // Boolean to indicate if we are moving forward or backward

    // Shooting control variables
    static int headOnShots = 0; // Counter for head-on targeting shots
    static int linearShots = 0; // Counter for linear targeting shots
    static int headOnHits = 0; // Counter for head-on targeting hits
    static int linearHits = 0; // Counter for linear targeting hits
    boolean gunType; // Identifier for gun type (true for linear, false for head-on)
    double energy; // Stores the robot's energy
    static int enemyFireCount = 0; // Counter for enemy shots
    boolean hit = false; // Boolean to know if the shot hit the enemy

    // List to store training data
    private static List<double[]> trainingData = new ArrayList<double[]>();

    public void run() {
        // Get the battlefield dimensions
        fieldWidth = getBattleFieldWidth(); // Arena width
        fieldHeight = getBattleFieldHeight(); // Arena height

        // Set the robot's colors
        setColors(Color.black, Color.black, Color.white);

        // Set parts to move independently
        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        setAhead(30000); // Move straight until told otherwise
        movingForward = true; // We are moving forward
        setTurnRadarRight(360); // Rotate radar

        // Check if the robot is within 50px of the arena edge
        if (getX() <= 50 || getY() <= 50 || fieldWidth - getX() <= 50 || fieldHeight - getY() <= 50) {
            nearWall = true;
        } else {
            nearWall = false;
        }

        // Main loop
        while (true) {
            // Check if the robot is not within 50px of the arena edge
            if (getX() > 50 && getY() > 50 && fieldWidth - getX() > 50 && fieldHeight - getY() > 50 && nearWall == true) {
                nearWall = false;
            }
            // Check if the robot is near the arena edge before advancing
            if (getX() <= 50 || getY() <= 50 || fieldWidth - getX() <= 50 || fieldHeight - getY() <= 50) {
                if (nearWall == false) {
                    // If near the wall, change direction
                    changeDirection();
                    nearWall = true;
                }
            }
            // If the radar stopped, do a full sweep to find a new enemy
            if (getRadarTurnRemaining() == 0.0) {
                setTurnRadarRight(360);
            }
            execute();
        }
    }

    // Method called when the robot collides with a wall
    public void onHitWall(HitWallEvent e) {
        // If it hits the wall, change direction
        changeDirection();
    }

    // Method called when the robot scans another robot
    public void onScannedRobot(ScannedRobotEvent e) {
        double absBearing = e.getBearingRadians() + getHeadingRadians(); // The enemy's location relative to the robot (absolute angle)
        double headOnRating = (double) headOnHits / headOnShots; // Head-on hit rate
        double linearRating = (double) linearHits / linearShots; // Linear hit rate
        double latVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing); // Enemy's lateral velocity
        double radarTurn = absBearing - getRadarHeadingRadians(); // Amount to turn the radar

        // Check if the enemy fired (energy drop)
        if (energy > (energy = e.getEnergy())) {
            enemyFireCount++;
            if (enemyFireCount % 5 == 0) {
                dir = -dir; // Reverse movement direction
            }

            // Add training data only when the enemy loses energy // Comment this part if you don't want to generate data
            double[] dataPoint = new double[6];
            dataPoint[0] = e.getDistance(); // Distance to enemy
            dataPoint[1] = e.getVelocity(); // Enemy's velocity
            dataPoint[2] = e.getEnergy(); // Enemy's energy
            dataPoint[3] = getVelocity(); // Robot's velocity
            dataPoint[4] = absBearing; // Absolute angle
            dataPoint[5] = hit ? 1 : 0; // 1 if the shot hit, 0 if it missed
            trainingData.add(dataPoint);

            // Print collected data to the console
            System.out.println("Training data collected: Distance, Velocity, Enemy Energy, Robot Velocity, Absolute Bearing, HitTarget");
			 
            hit = false; // Reset hit variable after recording the data
        }

        // Calculate hit probability using logistic regression
        double hitProbability = LogisticRegression.getHitProbability(e.getDistance(), e.getVelocity(), e.getEnergy(), getVelocity(), absBearing);

        // Select shooting strategy based on hit rates and probability
        double bulletPower;
        if ((getRoundNum() == 0 || linearRating > headOnRating) && getRoundNum() != 1) {
            bulletPower = Math.min(3, e.getEnergy() / 4); // Set bullet power
            if (hitProbability > 0.5) {
                bulletPower = Math.max(bulletPower, 2); // Increase bullet power if hit probability is high
            } else {
                bulletPower = Math.max(0.1, bulletPower - 1); // Decrease bullet power if hit probability is low
            }
            setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + Math.asin(latVel / (20 - 3 * bulletPower))));
            linearShots++; // Increment linear shots counter
            gunType = true; // Identify that the gun is linear
            setFire(bulletPower); // Fire with calculated power
        } else {
            bulletPower = Math.min(3, e.getEnergy() / 4); // Set bullet power
            if (hitProbability > 0.5) {
                bulletPower = Math.max(bulletPower, 2); // Increase bullet power if hit probability is high
            } else {
                bulletPower = Math.max(0.1, bulletPower - 1); // Decrease bullet power if hit probability is low
            }
            setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - getGunHeadingRadians())); // Adjust the gun to the absolute angle (predict movement)
            headOnShots++; // Increment head-on shots counter
            gunType = false; // Identify that the gun is head-on
            setFire(bulletPower); // Fire with calculated power
        }

        // Adjust radar to keep the enemy in sight
        setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn) * 2);

        // Circle around the enemy, moving at 90 degrees
        double angle = (movingForward ? 80 : 100) * dir; // Adjust the rotation angle based on direction
        setTurnRight(normalRelativeAngleDegrees(e.getBearing() + angle));
    }

    // Method called when the robot hits a bullet
    public void onBulletHit(BulletHitEvent e) {	
        hit = true;
        // Increment hit counter based on the gun type used
        if (gunType) {
            linearHits++; // Increment linear hits counter
        } else {
            headOnHits++; // Increment head-on hits counter
        }
        System.out.println("Hit!"); // Print hit message
    }

    // Method to change the robot's direction
    public void changeDirection() {
        if (movingForward) {
            setBack(30000); // Switch to moving backward
            movingForward = false;
        } else {
            setAhead(30000); // Switch to moving forward
            movingForward = true;
        }
    }

    public void onHitRobot(HitRobotEvent e) {
        // Method to avoid colliding with the enemy robot
        if (e.isMyFault()) {
            changeDirection(); // Change direction if colliding with another robot
        }
    }

    // Method to save training data at the end of the battle
    public void onBattleEnded(BattleEndedEvent e) {
        try {
            PrintStream writer = new PrintStream(new RobocodeFileOutputStream(getDataFile("trainingData.csv"))); // Create stream to write data
            
            // Add header to CSV
            writer.println("Distance,Velocity,EnemyEnergy,RobotVelocity,AbsoluteBearing,HitTarget"); 

            for (double[] dataPoint : trainingData) {
                for (int i = 0; i < dataPoint.length; i++) {
                    writer.print(dataPoint[i]);
                    if (i < dataPoint.length - 1) {
                        writer.print(","); // Add comma between values
                    }
                }
                writer.println(); // New line for the next record
            }
            writer.close(); // Close the output stream after writing the data to the file
        } catch (IOException ex) {
            ex.printStackTrace(); // If an I/O exception occurs, print the stack trace
        }
    }
}