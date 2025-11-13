package Robotics_III.MathOperatorsThings;

import java.util.Scanner;

public class TShirtPricer
{
    public static void main(String [] args)
    {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Input the amount of shirts: ");
        int shirts = scanner.nextInt();

        System.out.println("Input the number of colors: ");
        int colors = scanner.nextInt();

        double price = 8.50;
        double baseFee = 25.0;

        if (shirts > 50 && shirts <= 200)
        {
            price = price * .95;
        }
        else if (shirts <= 500 && shirts > 200)
        {
            price = price * .90;
        }
        else if (shirts > 500)
        {
            price = price * .80;
        }

        if (colors == 2)
        {
            baseFee = 50.00;
        }
        else if (colors == 3)
        {
            baseFee = 75.00;
        }
        else if (colors > 3)
        {
            baseFee = 125.00;
        }

        System.out.println("The Final Fee is: " + ((price * shirts) + baseFee));


    }
}
