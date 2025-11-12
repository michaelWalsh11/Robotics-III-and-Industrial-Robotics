package Robotics_III.MathOperatorsThings;

import java.util.Scanner;

public class QuadraticEquationSolver
{
    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter value for a: ");
        int a = scanner.nextInt();

        System.out.print("Enter value for b: ");
        int b = scanner.nextInt();

        System.out.print("Enter value for c: ");
        int c = scanner.nextInt();

        double discriminant = Math.pow(b, 2) - 4 * a * c;

        if (discriminant < 0) {
            System.out.println("NO SOLUTION :(");
        } else {
            double output1 = (-b + Math.sqrt(discriminant)) / (2 * a);
            double output2 = (-b - Math.sqrt(discriminant)) / (2 * a);

            System.out.println("The first output is: " + output1);
            System.out.println("The second output is: " + output2);
        }
    }
}
