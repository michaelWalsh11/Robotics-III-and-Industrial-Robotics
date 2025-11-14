package Robotics_III.MathOperatorsThings;

import java.util.Scanner;

/**
 * This program takes the inputs about the size of the order
 * and types of shirts and uses those in accordance with
 * preset prices to develop the final price of the given order
 * it uses a series of if statements to find the final answer.
 */

public class TShirtPricer
{
    public static void main(String [] args)
    {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Input the amount of shirts: ");
        int shirts = scanner.nextInt();

        System.out.print("Input the number of colors: ");
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
