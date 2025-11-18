package Robotics_III.chatGUIProject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class FlappyGraphGame extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlappyGraphGame g = new FlappyGraphGame();
            g.setVisible(true);
        });
    }

    public FlappyGraphGame() {
        setTitle("Flappy Bird + Graph Obstacles — Rainbow Vomit Edition");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel panel = new GamePanel();
        add(panel);
    }

    // =====================================================
    // GAME PANEL
    // =====================================================

    class GamePanel extends JPanel implements ActionListener, KeyListener {

        Timer timer;
        double birdY = 250;
        double birdVel = 0;
        double gravity = 0.7;
        double flapStrength = -10;

        int birdX = 150;
        int birdSize = 25;

        ArrayList<Rectangle> pipes;
        int pipeWidth = 60;
        int pipeGap = 200;
        int pipeSpeed = 4;

        boolean gameOver = false;

        // Sample kinematics curve -> becomes obstacles
        double[] parabola;

        public GamePanel() {
            setBackground(Color.BLACK);
            addKeyListener(this);
            setFocusable(true);

            parabola = generateParabola(); // generates curve
            pipes = generatePipesFromGraph();

            timer = new Timer(20, this);
            timer.start();
        }

        // =====================================================
        // GRAPH → PIPE CONVERSION
        // =====================================================
        private double[] generateParabola() {
            double[] arr = new double[300];
            for (int t = 0; t < arr.length; t++) {
                double x = t;
                arr[t] = 200 + 0.01 * (x - 150) * (x - 150); // smooth parabola
            }
            return arr;
        }

        private ArrayList<Rectangle> generatePipesFromGraph() {
            ArrayList<Rectangle> pipesList = new ArrayList<>();

            for (int i = 0; i < parabola.length; i += 25) {
                int topHeight = (int) parabola[i];
                int bottomY = topHeight + pipeGap;

                pipesList.add(new Rectangle(
                        600 + i * 3,
                        0,
                        pipeWidth,
                        topHeight
                ));

                pipesList.add(new Rectangle(
                        600 + i * 3,
                        bottomY,
                        pipeWidth,
                        getHeight() - bottomY
                ));
            }

            return pipesList;
        }

        // =====================================================
        // GAME LOOP
        // =====================================================
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!gameOver) {
                birdVel += gravity;
                birdY += birdVel;

                // Move pipes
                for (Rectangle r : pipes) {
                    r.x -= pipeSpeed;
                }

                // Collision detection
                Rectangle birdRect = new Rectangle(birdX, (int) birdY, birdSize, birdSize);
                for (Rectangle r : pipes) {
                    if (birdRect.intersects(r)) {
                        gameOver = true;
                    }
                }

                // Off screen
                if (birdY > getHeight() || birdY < 0) {
                    gameOver = true;
                }
            }

            repaint();
        }

        // =====================================================
        // DRAW EVERYTHING
        // =====================================================
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;

            // Rainbow background
            for (int i = 0; i < getWidth(); i++) {
                g2.setColor(Color.getHSBColor((float) i / getWidth(), 1f, 0.3f));
                g2.drawLine(i, 0, i, getHeight());
            }

            // Bird
            g2.setColor(Color.YELLOW);
            g2.fillOval(birdX, (int) birdY, birdSize, birdSize);

            // Pipes
            for (Rectangle r : pipes) {
                g2.setColor(Color.getHSBColor((float) (r.x % 360) / 360, 1f, 1f));
                g2.fill(r);
            }

            // Text
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Comic Sans MS", Font.BOLD, 32));

            if (gameOver) {
                g2.drawString("GAME OVER — Press R to Restart", 200, 300);
            }

            g2.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
            g2.drawString("SPACE = FLAP", 20, 30);
            g2.drawString("Graph Shapes = Obstacles", 20, 55);
        }

        // =====================================================
        // INPUT
        // =====================================================
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE && !gameOver) {
                birdVel = flapStrength;
            }
            if (e.getKeyCode() == KeyEvent.VK_R && gameOver) {
                resetGame();
            }
        }

        public void resetGame() {
            birdY = 250;
            birdVel = 0;
            gameOver = false;
            pipes = generatePipesFromGraph();
        }

        @Override
        public void keyTyped(KeyEvent e) {}
        @Override
        public void keyReleased(KeyEvent e) {}
    }
}
