package Robotics_III.chatGUIProject;

import java.util.Scanner;

/**
 * @author Michael Walsh
 * Implement Kinematics equation for position in one dimension
 */
public class ScannerTHinga {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the the starting position:");
        double pos = scanner.nextDouble();

        System.out.println("Enter the intial velocity (if not given, enter '0'):");
        double vel = scanner.nextDouble();

        System.out.println("Enter the time:");
        double time = scanner.nextDouble();

        System.out.println("Enter the acceleration");
        double acc = scanner.nextDouble();

        double x = pos + (vel * time) + (0.5 * (acc * Math.pow(time, 2)));

        String output = "The distance traveled is " + x + " meters.";
        System.out.println(output);


    }

}
