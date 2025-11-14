package Robotics_III.MathOperatorsThings;

import java.util.Scanner;

/**
 * For this assignment I had to look on the physics formula
 * sheet and program one of the formulas, Initially, I was going
 * to do the Vcm equation, but then i realized that I would have to
 * type arrays to feed to my program, and I did not want to do that,
 * so I decided to go for a slightly less complicated formula in the form
 * of p = mv, but the program itself just asks the user for the mass
 * and velocity and returns the momentum
 */

public class ComputeParticleMovement
{
    public static void main(String [] args)
    {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Input the mass: ");
        double mass = scanner.nextDouble();

        System.out.print("Input the velocity: ");
        double velocity = scanner.nextDouble();

        double particleMovement = mass * velocity;

        System.out.println("The movement of the particle is: " + particleMovement);
    }
}
