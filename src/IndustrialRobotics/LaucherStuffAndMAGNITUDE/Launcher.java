package IndustrialRobotics.LaucherStuffAndMAGNITUDE;

import java.awt.Point;
import java.util.ArrayList;

public class Launcher
{
    public double magnitude;
    public double theta;
    public double xPos;
    public double yPos;
    public final double gravity = 9.8;

    public Launcher(double xPos, double yPos, double theta, double magnitude)
    {
        this.xPos = xPos;
        this.yPos = yPos;
        this.theta = theta;
        this.magnitude = magnitude;
    }

    public ArrayList<Point> getLaunchData()
    {
        ArrayList<Point> points = new ArrayList<>();

        double timeInAir = (magnitude * Math.sin(theta) + Math.sqrt((magnitude * Math.sin(theta)) *
                (magnitude * Math.sin(theta)) * (magnitude * Math.sin(theta)) + 2 * gravity * yPos)) / gravity;

        for (int i = 0; i < timeInAir; i += 0.1)
        {
            int x = (int) (xPos + magnitude * Math.cos(theta) * i);
            int y = (int) (yPos + (magnitude * Math.sin(theta) * i) - (0.5 * gravity * Math.sqrt(i)));

            points.add(new Point(x, y));
        }

        return points;
    }
git
    public void setMagnitude(double magnitude)
    {
        this.magnitude = magnitude;
    }

    public void setTheta(double theta)
    {
        this.theta = theta;
    }

    public void goTo(int x, int y)
    {
        xPos = x;
        yPos = y;
    }

    @Override
    public String toString()
    {
        return "Point at: " + xPos + ", " + yPos +
                "\nMagnitude: " + magnitude +
                "\nTheta: " + theta;
    }
}
