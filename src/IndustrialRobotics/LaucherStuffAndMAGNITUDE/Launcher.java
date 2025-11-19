package IndustrialRobotics.LaucherStuffAndMAGNITUDE;

import java.awt.Point;
import java.util.ArrayList;

public class Launcher {

    public double magnitude;
    public double theta;
    public double xPos;
    public double yPos;
    public final double gravity = 9.8;

    public Launcher(double xPos, double yPos, double theta, double magnitude) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.theta = theta;
        this.magnitude = magnitude;
    }

    public ArrayList<Point> getLaunchData() {
        ArrayList<Point> points = new ArrayList<>();

        double vx = magnitude * Math.cos(theta);
        double vy = magnitude * Math.sin(theta);

        double timeInAir = (vy + Math.sqrt(vy * vy + 2 * gravity * yPos)) / gravity;

        for (double t = 0; t <= timeInAir; t += 0.05) {
            int x = (int) (xPos + vx * t);
            int y = (int) (yPos + vy * t - 0.5 * gravity * t * t);
            points.add(new Point(x, y));
        }

        return points;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }

    public void setTheta(double theta) {
        this.theta = theta;
    }

    public void goTo(int x, int y) {
        xPos = x;
        yPos = y;
    }

    @Override
    public String toString() {
        return "Point at: " + xPos + ", " + yPos +
                "\nMagnitude: " + magnitude +
                "\nTheta: " + theta;
    }
}
