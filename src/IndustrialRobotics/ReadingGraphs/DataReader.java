package IndustrialRobotics.ReadingGraphs;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class DataReader
{
    public static void main(String [] args) throws FileNotFoundException {
        String path = "testFile1.txt";
        double data [] = readData(path);

        printArray(data);
    }

    public static void setup(int width, int length)
    {

    }

    public static void printArray(double [] array)
    {
        for (int x = 0; x < array.length; x++)
        {
            System.out.println(array[x]);
        }
    }

    public static double [] readData(String path) throws FileNotFoundException
    {
        File tFile = new File(path);
        Scanner file = new Scanner(tFile);

        double [] data = new double[Integer.parseInt(file.nextLine())];

        for (int x = 0; x < data.length; x++)
        {
            data[x] = Double.valueOf(file.nextLine());
        }

        return data;
    }
}
