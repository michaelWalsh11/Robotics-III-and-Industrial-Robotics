package Robotics_III.MathOperatorsThings;

import java.util.Scanner;

public class ComputeParticleMovement
{
    public static void main(String [] args)
    {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Input the mass: ");
        double mass = scanner.nextDouble();

        System.out.println("Input the velocity: ");
        double velocity = scanner.nextDouble();

        double particleMovement = mass * velocity;

        System.out.println("The movement of the particle is: " + particleMovement);
    }
}
