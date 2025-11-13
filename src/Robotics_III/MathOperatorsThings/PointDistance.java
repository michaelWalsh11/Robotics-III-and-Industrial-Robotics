package Robotics_III.MathOperatorsThings;

import java.util.Scanner;

public class PointDistance
{
    public static void main(String [] args)
    {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter coordinates 1 and 2 ex. (1, 2) (3, 2): ");
        String cords = scanner.nextLine();

        String cord1 = cords.substring(0, cords.indexOf(")") + 1);

        int x1 = Integer.parseInt(cord1.substring(1, cord1.indexOf(" ") - 1).strip());
        int y1 = Integer.parseInt(cord1.substring(cord1.indexOf(" "), cord1.indexOf(")")).strip());

        String cord2 = cords.substring(cords.indexOf(")") + 1).strip();

        int x2 = Integer.parseInt(cord2.substring(1, cord2.indexOf(" ") - 1).strip());
        int y2 = Integer.parseInt(cord2.substring(cord2.indexOf(" "), cord2.indexOf(")")).strip());

        double output = Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));

        System.out.println("The distance between the two points is: " + output);
    }
}
