package IndustrialRobotics.LaucherStuffAndMAGNITUDE;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class LauncherGUI extends JPanel {

    Launcher launcher;
    ArrayList<Point> points;

    public LauncherGUI() {
        launcher = new Launcher(50, 450, Math.toRadians(45), 80);
        points = launcher.getLaunchData();

        JFrame frame = new JFrame("Projectile Launcher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(this);

        // Control panel
        JPanel controls = new JPanel();
        controls.setBackground(Color.BLACK);

        JTextField angleField = new JTextField("45", 5);
        JTextField speedField = new JTextField("80", 5);

        JButton launchBtn = new JButton("LAUNCH!");
        launchBtn.setBackground(Color.PINK);
        launchBtn.setForeground(Color.BLACK);

        controls.add(new JLabel("Angle°:"));
        controls.add(angleField);
        controls.add(new JLabel("Speed:"));
        controls.add(speedField);
        controls.add(launchBtn);

        frame.add(controls, BorderLayout.SOUTH);

        launchBtn.addActionListener(e -> {
            double angle = Math.toRadians(Double.parseDouble(angleField.getText()));
            double speed = Double.parseDouble(speedField.getText());

            launcher.setTheta(angle);
            launcher.setMagnitude(speed);
            points = launcher.getLaunchData();

            repaint();
        });

        frame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Sky background
        Graphics2D g2 = (Graphics2D) g;
        GradientPaint sky = new GradientPaint(0, 0, new Color(100, 180, 255),
                0, getHeight(), new Color(10, 40, 120));
        g2.setPaint(sky);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Draw ground
        g.setColor(new Color(80, 200, 80));
        g.fillRect(0, getHeight() - 40, getWidth(), 40);

        // Draw trajectory
        g.setColor(Color.YELLOW);
        for (Point p : points) {
            int drawX = p.x;
            int drawY = getHeight() - p.y;  // invert y-axis
            g.fillOval(drawX, drawY, 6, 6);
        }

        // Draw launcher point
        g.setColor(Color.RED);
        int lx = (int) launcher.xPos;
        int ly = getHeight() - (int) launcher.yPos;
        g.fillOval(lx - 6, ly - 6, 12, 12);
    }

    public static void main(String[] args) {
        new LauncherGUI();
    }
}
