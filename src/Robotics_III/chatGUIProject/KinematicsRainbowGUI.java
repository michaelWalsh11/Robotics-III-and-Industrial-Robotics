package Robotics_III.chatGUIProject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;

public class KinematicsRainbowGUI extends JFrame {

    private JTextField posField, velField, timeField, accField;
    private RainbowPanel panel;

    public KinematicsRainbowGUI() {
        setTitle("🌈 Rainbow Vomit Kinematics 🌈");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ===== INPUT PANEL =====
        JPanel inputPanel = new JPanel(new GridLayout(5, 2)) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(255, 120, 255),
                        getWidth(), getHeight(), new Color(120, 255, 255)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        inputPanel.add(makeLabel("Starting Position (m):"));
        posField = makeField("0");
        inputPanel.add(posField);

        inputPanel.add(makeLabel("Initial Velocity (m/s):"));
        velField = makeField("0");
        inputPanel.add(velField);

        inputPanel.add(makeLabel("Time (s):"));
        timeField = makeField("2");
        inputPanel.add(timeField);

        inputPanel.add(makeLabel("Acceleration (m/s²):"));
        accField = makeField("1");
        inputPanel.add(accField);

        JButton graphButton = new JButton("🌈 GRAPH IT 🌈");
        graphButton.setFont(new Font("Comic Sans MS", Font.BOLD, 22));
        inputPanel.add(graphButton);

        add(inputPanel, BorderLayout.NORTH);

        // ===== GRAPH PANEL =====
        panel = new RainbowPanel();
        add(panel, BorderLayout.CENTER);

        graphButton.addActionListener(e -> panel.refreshGraph());

        setVisible(true);
    }


    // ========================= HELPER METHODS =========================
    private JLabel makeLabel(String s) {
        JLabel l = new JLabel(s);
        l.setFont(new Font("Comic Sans MS", Font.BOLD, 18));
        l.setForeground(Color.MAGENTA);
        return l;
    }

    private JTextField makeField(String def) {
        JTextField tf = new JTextField(def);
        tf.setFont(new Font("Comic Sans MS", Font.BOLD, 18));
        tf.setBackground(new Color(255, 250, 200));
        return tf;
    }


    // ========================= RAINBOW PANEL =========================
    class RainbowPanel extends JPanel {
        double pos, vel, acc, time;

        int px, py;
        boolean dragging = false;

        float hueShift = 0f;
        Timer animator;

        public RainbowPanel() {
            setBackground(Color.BLACK);

            // mouse listeners for dragging the point
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if (Math.hypot(e.getX() - px, e.getY() - py) < 15)
                        dragging = true;
                }

                public void mouseReleased(MouseEvent e) {
                    dragging = false;
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    if (dragging) {
                        time = (e.getX() - 80) / 65.0;
                        if (time < 0) time = 0;
                        timeField.setText(String.format("%.2f", time));
                        repaint();
                    }
                }
            });

            // ANIMATED RAINBOW BACKGROUND
            animator = new Timer(30, e -> {
                hueShift += 0.005;
                if (hueShift > 1) hueShift = 0;
                repaint();
            });
            animator.start();
        }

        public void refreshGraph() {
            pos = Double.parseDouble(posField.getText());
            vel = Double.parseDouble(velField.getText());
            acc = Double.parseDouble(accField.getText());
            time = Double.parseDouble(timeField.getText());
            repaint();
        }

        double xFunc(double t) {
            return pos + vel * t + 0.5 * acc * t * t;
        }


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;

            // smoother
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int W = getWidth();
            int H = getHeight();

            // ===== BACKGROUND GRADIENT ANIMATED =====
            Color bg1 = Color.getHSBColor(hueShift, 1f, 1f);
            Color bg2 = Color.getHSBColor((hueShift + 0.3f) % 1f, 1f, 1f);

            GradientPaint gp = new GradientPaint(0, 0, bg1, W, H, bg2);
            g2.setPaint(gp);
            g2.fillRect(0, 0, W, H);

            // ===== GRID (with random colors) =====
            Random r = new Random();
            for (int x = 80; x < W; x += 60) {
                g2.setColor(new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255), 80));
                g2.drawLine(x, 40, x, H - 80);
            }
            for (int y = 40; y < H - 40; y += 60) {
                g2.setColor(new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255), 80));
                g2.drawLine(80, y, W - 40, y);
            }

            // ===== AXES =====
            g2.setStroke(new BasicStroke(4));
            g2.setColor(Color.WHITE);
            g2.drawLine(80, H - 80, W - 40, H - 80);
            g2.drawLine(80, 40, 80, H - 80);

            // ===== LABELS =====
            g2.setFont(new Font("Comic Sans MS", Font.BOLD, 26));
            g2.setColor(Color.YELLOW);
            g2.drawString("TIME (s)", W / 2 - 50, H - 30);

            g2.rotate(-Math.PI / 2);
            g2.drawString("POSITION (m)", -H / 2 - 70, 40);
            g2.rotate(Math.PI / 2);

            // ===== RAINBOW LINE =====
            g2.setStroke(new BasicStroke(4));

            Path2D path = new Path2D.Double();
            boolean first = true;

            for (int x = 80; x < W - 40; x++) {
                double t = (x - 80) / 65.0;
                double yMeters = xFunc(t);
                int yPix = (int) (H - 80 - yMeters * 30);

                // dynamic rainbow color along curve
                float hue = (float) ((t * 0.15 + hueShift) % 1.0);
                g2.setColor(Color.getHSBColor(hue, 1f, 1f));

                if (!first) {
                    g2.drawLine((int) path.getCurrentPoint().getX(),
                            (int) path.getCurrentPoint().getY(),
                            x, yPix);
                }

                if (first) {
                    path.moveTo(x, yPix);
                    first = false;
                } else {
                    path.lineTo(x, yPix);
                }
            }

            // ===== POINT =====
            double yVal = xFunc(time);
            px = (int) (80 + time * 65);
            py = (int) (H - 80 - yVal * 30);

            // glowing effect
            for (int i = 0; i < 6; i++) {
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillOval(px - 10 - i, py - 10 - i, 22 + i * 2, 22 + i * 2);
            }

            g2.setColor(Color.WHITE);
            g2.fillOval(px - 10, py - 10, 20, 20);

            // label point
            g2.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
            g2.setColor(Color.BLACK);
            g2.drawString(
                    String.format("(t=%.2f, x=%.2f)", time, yVal),
                    px + 15,
                    py - 15
            );
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(KinematicsRainbowGUI::new);
    }
}
