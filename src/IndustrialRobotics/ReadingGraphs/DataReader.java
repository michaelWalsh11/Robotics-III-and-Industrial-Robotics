import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javax.swing.*;
import java.awt.event.*;

public class DataReader
{
    public static double left = 70;
    public static double bottom = 70;

    public static int width = 600;
    public static int height = 600;

    public static String mode = "pos";
    public static String version = "1";


    public static void main(String[] args) throws FileNotFoundException {
        String path = "positions_v" + version + ".txt";
        double[] data = readData(path);

        setup();

        //Position vs Time
        if (mode.equals("pos"))
        {
            graphSetup(data, "Position", "Time");
            plotPoints(data);
        }

        //Velocity vs Time
        if (mode.equals("vel"))
        {
            double [] velocity = convertDataToVelocity(data);
            graphSetup(velocity, "Velocity", "Time");
            plotPoints(velocity);
        }

        //acceleration vs Time
        if (mode.equals("acc"))
        {
            double[] acceleration = convertDataToAcceleration(data);
            graphSetup(acceleration, "Acceleration", "Time");
            plotPoints(acceleration);
        }
    }

    public static double [] convertDataToAcceleration(double [] arr)
    {
        double [] velocity = convertDataToVelocity(arr);
        double[] acceleration = new double[velocity.length - 1];

        for (int x = 0; x < acceleration.length; x++)
        {
            acceleration[x] = velocity[x +1] - velocity[x];
        }

        return acceleration;
    }

    public static double [] convertDataToVelocity(double [] arr)
    {
        double[] velocity = new double[arr.length - 1];

        for (int x = 0; x < velocity.length; x++)
        {
            velocity[x] = arr[x + 1] - arr[x];
        }

        return velocity;

    }

    public static void graphSetup(double[] arr, String yAxis, String xAxis)
    {

        //setup
        StdDraw.setPenColor(Color.BLACK);
        StdDraw.setPenRadius(0.005);
        StdDraw.line(left, bottom, left, height - 20);
        StdDraw.line(left, bottom, width - 20, bottom);

        StdDraw.setPenRadius(0.04);
        StdDraw.text(left - 35, height - 40, yAxis);
        StdDraw.text(width - 40, bottom - 40, xAxis);

        StdDraw.setPenRadius(0.005);
        double min = arr[0];
        double max = arr[0];

        for (double num : arr)
        {
            if (num < min)
            {
                min = num;
            }
            if (num > max)
            {
                max = num;
            }
        }

        //y-axis shenanigans for labels
        double range = max - min;
        double gap = range / 4.0;

        double drawingHeight = height - bottom - 20;

        for (int x = 0; x < 5; x++)
        {
            double val = min + gap * x;

            val = (double) Math.round(val * 100) / 100;

            double y = bottom + ((val - min) / range) * drawingHeight;

            StdDraw.text(left - 25, y, String.valueOf(val));
            StdDraw.line(left - 5, y, left + 5, y);
        }

        //x-axis labels
        double drawingWidth = width - left - 20;
        double xGap = drawingWidth / 4.0;
        int finalNum = arr.length - 1;
        double numGap = (double) finalNum / 4.0;

        for (int x = 0; x < 5; x++)
        {
            double x1 = left + xGap * x;
            int labelIndex = (int)Math.round(numGap * x);

            StdDraw.text(x1, bottom - 20, String.valueOf(labelIndex));
            StdDraw.line(x1, bottom - 5, x1, bottom + 5);
        }
    }

    public static void plotPoints(double[] arr)
    {
        double min = arr[0];
        double max = arr[0];

        for (double num : arr) {
            if (num < min)
            {
                min = num;
            }
            if (num > max)
            {
                max = num;
            }
        }

        double usableHeight = height - bottom - 20;
        double usableWidth = width - left - 20;

        double xGap = usableWidth / (arr.length - 1);

        StdDraw.setPenColor(Color.BLUE);
        StdDraw.setPenRadius(0.01);

        for (int i = 0; i < arr.length; i++)
        {
            double x = left + i * xGap;
            double y = bottom + ((arr[i] - min) / (max - min)) * usableHeight;

            StdDraw.filledCircle(x, y, 4);
        }
    }


    public static void setup()
    {
        StdDraw.setCanvasSize(width, height);
        StdDraw.setXscale(0, width);
        StdDraw.setYscale(0, height);
        StdDraw.clear(Color.WHITE);
    }

    public static double [] readData(String path) throws FileNotFoundException
    {
        File tFile = new File(path);
        Scanner file = new Scanner(tFile);

        double [] data = new double[Integer.parseInt(file.nextLine())];

        for (int x = 0; x < data.length; x++)
        {
            data[x] = Double.parseDouble(file.nextLine());
        }
        return data;
    }
}
