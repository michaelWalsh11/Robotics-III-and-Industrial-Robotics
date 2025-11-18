package Robotics_III.chatGUIProject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;

/**
 * KinematicsRainbowFlappy
 * - Left: rainbow, draggable kinematics graph (x(t) = pos + v t + 1/2 a t^2)
 * - Right: Flappy-style game where the ceiling follows the graph curve and the floor = ceiling + gap
 *
 * Run: javac KinematicsRainbowFlappy.java && java KinematicsRainbowFlappy
 */
public class KinematicsRainbowFlappy extends JFrame {

    // UI fields
    private JTextField posField, velField, timeField, accField, gapField;
    private GraphPanel graphPanel;
    private GamePanel gamePanel;

    public KinematicsRainbowFlappy() {
        setTitle("🌈 Kinematics Rainbow + Flappy (GRAPH LEVEL) 🌈");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top controls
        JPanel inputPanel = new JPanel(new GridLayout(2, 6, 6, 6));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        inputPanel.setBackground(new Color(30, 10, 30));

        posField = makeField("0");
        velField = makeField("0");
        timeField = makeField("2");
        accField = makeField("1");
        gapField = makeField("150"); // gap in pixels for flappy between ceil and floor

        inputPanel.add(makeLabel("Start Pos (m)")); inputPanel.add(posField);
        inputPanel.add(makeLabel("Init Vel (m/s)")); inputPanel.add(velField);
        inputPanel.add(makeLabel("Time (s)")); inputPanel.add(timeField);
        inputPanel.add(makeLabel("Accel (m/s²)")); inputPanel.add(accField);
        inputPanel.add(makeLabel("Gap px (game)")); inputPanel.add(gapField);

        JButton graphBtn = new JButton("🌈 Graph It & Update Level");
        graphBtn.setFont(new Font("Comic Sans MS", Font.BOLD, 16));
        graphBtn.addActionListener(e -> {
            graphPanel.refreshGraphFromInputs();
            gamePanel.recomputeLevel();
        });

        inputPanel.add(graphBtn);

        add(inputPanel, BorderLayout.NORTH);

        // Main split pane: left graph, right game
        JSplitPane split = new JSplitPane();
        split.setDividerLocation(680);
        split.setResizeWeight(0.6);

        graphPanel = new GraphPanel();
        gamePanel = new GamePanel();

        // Link graph -> game so game can query the kinematics function
        graphPanel.setLevelListener(() -> gamePanel.recomputeLevel());

        split.setLeftComponent(graphPanel);
        split.setRightComponent(gamePanel);

        add(split, BorderLayout.CENTER);

        setVisible(true);

        // initial sync
        graphPanel.refreshGraphFromInputs();
        gamePanel.recomputeLevel();
    }

    // Helper: colorful label & field
    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Comic Sans MS", Font.BOLD, 14));
        l.setForeground(Color.WHITE);
        return l;
    }

    private JTextField makeField(String def) {
        JTextField f = new JTextField(def);
        f.setFont(new Font("Comic Sans MS", Font.BOLD, 14));
        return f;
    }

    // ================ Graph Panel =================
    class GraphPanel extends JPanel {
        // kinematic params
        double pos = 0, vel = 0, acc = 0, time = 2;

        // visuals
        int leftMargin = 80, rightMargin = 40, topMargin = 40, bottomMargin = 80;
        double pxScale = 65.0; // px per second
        double yScale = 30.0;  // px per meter

        // draggable point screen coords
        int pointX = 0, pointY = 0;
        boolean dragging = false;

        // animated hues
        float hueShift = 0f;
        Timer animator;

        // callback for when level should update
        Runnable levelListener = null;

        public GraphPanel() {
            setBackground(Color.BLACK);

            // mouse listeners for dragging the point
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if (Math.hypot(e.getX() - pointX, e.getY() - pointY) < 16) {
                        dragging = true;
                    }
                }
                public void mouseReleased(MouseEvent e) {
                    dragging = false;
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    if (dragging) {
                        // convert mouse X to time
                        double t = (e.getX() - leftMargin) / pxScale;
                        if (t < 0) t = 0;
                        time = t;
                        timeField.setText(String.format("%.2f", time));
                        repaint();
                        if (levelListener != null) levelListener.run();
                    }
                }
            });

            // animate background hue
            animator = new Timer(30, e -> {
                hueShift += 0.004f;
                if (hueShift > 1f) hueShift = 0f;
                repaint();
            });
            animator.start();
        }

        public void setLevelListener(Runnable r) { levelListener = r; }

        // read fields and refresh
        public void refreshGraphFromInputs() {
            try {
                pos = Double.parseDouble(posField.getText());
                vel = Double.parseDouble(velField.getText());
                acc = Double.parseDouble(accField.getText());
                time = Double.parseDouble(timeField.getText());
            } catch (NumberFormatException ex) {
                // ignore invalid inputs
            }
            repaint();
            if (levelListener != null) levelListener.run();
        }

        // kinematic function x(t)
        public double xFunc(double t) {
            return pos + vel * t + 0.5 * acc * t * t;
        }

        // expose mapping for game to sample ceiling: given t -> pixel y
        public int sampleYPixels(double t, int panelHeight) {
            double meters = xFunc(t);
            double yPix = panelHeight - bottomMargin - (meters * yScale);
            // clamp to safe region
            if (yPix < topMargin) yPix = topMargin;
            if (yPix > panelHeight - bottomMargin) yPix = panelHeight - bottomMargin;
            return (int) yPix;
        }

        // painting
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int W = getWidth();
            int H = getHeight();

            // animated rainbow gradient background
            Color c1 = Color.getHSBColor(hueShift, 0.95f, 1f);
            Color c2 = Color.getHSBColor((hueShift + 0.25f) % 1f, 0.95f, 1f);
            GradientPaint bg = new GradientPaint(0, 0, c1, W, H, c2);
            g2.setPaint(bg);
            g2.fillRect(0, 0, W, H);

            // colorful grid lines
            Random rand = new Random(12345); // keep it stable each repaint for legibility
            for (int x = leftMargin; x < W - rightMargin; x += 60) {
                Color cg = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), 60);
                g2.setColor(cg);
                g2.drawLine(x, topMargin, x, H - bottomMargin);
            }
            for (int y = topMargin; y < H - bottomMargin; y += 60) {
                Color cg = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), 60);
                g2.setColor(cg);
                g2.drawLine(leftMargin, y, W - rightMargin, y);
            }

            // axes
            g2.setStroke(new BasicStroke(3));
            g2.setColor(Color.WHITE);
            g2.drawLine(leftMargin, H - bottomMargin, W - rightMargin, H - bottomMargin); // x-axis
            g2.drawLine(leftMargin, topMargin, leftMargin, H - bottomMargin); // y-axis

            // labels big & sparkly
            g2.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
            g2.setColor(Color.YELLOW);
            g2.drawString("TIME (s)", W / 2 - 30, H - 30);

            // rotate for y label
            AffineTransform old = g2.getTransform();
            g2.rotate(-Math.PI / 2);
            g2.drawString("POSITION (m)", -H / 2 - 60, 30);
            g2.setTransform(old);

            // draw axis tick marks and numeric labels (colored)
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            for (int x = leftMargin; x <= W - rightMargin; x += 60) {
                int tTick = (int) Math.round((x - leftMargin) / pxScale);
                g2.setColor(Color.getHSBColor(((x-leftMargin)/ (float)W) % 1f, 0.85f, 1f));
                g2.drawLine(x, H - bottomMargin - 6, x, H - bottomMargin + 6);
                g2.drawString(String.valueOf(tTick), x - 8, H - bottomMargin + 22);
            }

            // draw curve rainbow
            Path2D.Double curve = new Path2D.Double();
            boolean first = true;
            for (int x = leftMargin; x < W - rightMargin; x++) {
                double t = (x - leftMargin) / pxScale;
                int y = sampleYPixels(t, H);
                if (first) { curve.moveTo(x, y); first = false; } else curve.lineTo(x, y);
            }

            // draw the curve in short colored segments for rainbow look
            int segCount = 120;
            for (int i = 0; i < segCount; i++) {
                float h = (hueShift + i / (float) segCount) % 1f;
                g2.setColor(Color.getHSBColor(h, 1f, 1f));
                Stroke s = new BasicStroke(4f);
                g2.setStroke(s);
                // compute segment range in pixels
                int sx = leftMargin + (i * (getWidth() - leftMargin - rightMargin)) / segCount;
                int ex = leftMargin + ((i+1) * (getWidth() - leftMargin - rightMargin)) / segCount;
                // draw subpath by sampling points
                Path2D.Double sub = new Path2D.Double();
                boolean ff = true;
                for (int x = sx; x <= ex; x++) {
                    double t = (x - leftMargin) / pxScale;
                    int y = sampleYPixels(t, H);
                    if (ff) { sub.moveTo(x, y); ff = false; } else sub.lineTo(x, y);
                }
                g2.draw(sub);
            }

            // compute and draw draggable point (current time)
            double yVal = xFunc(time);
            pointX = (int) (leftMargin + time * pxScale);
            pointY = sampleYPixels(time, H);

            // glowing halo
            for (int r = 20; r >= 6; r -= 4) {
                g2.setColor(new Color(255, 255, 255, Math.max(10, 100 - r * 4)));
                g2.fillOval(pointX - r, pointY - r, 2 * r, 2 * r);
            }
            g2.setColor(Color.BLACK);
            g2.fillOval(pointX - 8, pointY - 8, 16, 16);

            // label point
            g2.setFont(new Font("Comic Sans MS", Font.BOLD, 16));
            g2.setColor(Color.WHITE);
            g2.drawString(String.format("t=%.2f, x=%.2f", time, yVal), pointX + 14, pointY - 12);
        }
    }

    // ================ Game Panel =================
    class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener {
        // level derived from graph: arrays of ceiling y pixels for a large virtual width
        int[] ceilingY; // pixel y for ceiling at each virtual x sample
        int virtualWidth = 3000; // samples across time map (makes the level long)
        double sampleDt = 0.02;   // seconds between samples when sampling graph

        // scrolling & bird
        int scroll = 0; // pixels scrolled (increases)
        Timer gameTimer;
        int fpsMs = 20;

        // bird state
        double birdY;
        double birdVy;
        final int birdX = 140; // fixed bird x in the game panel
        final int birdSize = 18;

        // gameplay
        boolean running = true;
        int score = 0;
        Random rand = new Random();

        // colors & visuals
        float hue = 0f;

        public GamePanel() {
            setBackground(Color.DARK_GRAY);
            setFocusable(true);
            addKeyListener(this);
            addMouseListener(this);

            // init arrays
            ceilingY = new int[virtualWidth];

            // timer
            gameTimer = new Timer(fpsMs, this);
            birdY = 200;
            birdVy = 0;
            gameTimer.start();
        }

        // recompute ceilingY from graph's kinematic function
        public void recomputeLevel() {
            int H = getHeight() > 0 ? getHeight() : 600;
            // We'll sample time along virtualWidth using the same pxScale as the graph,
            // mapping virtual x index i -> time = (i * samplePixel) / pxScale
            double pxScale = graphPanel.pxScale;
            double samplePixelStep = 1.0; // 1 virtual pixel step corresponds to 1 px movement
            for (int i = 0; i < virtualWidth; i++) {
                double virtualPixel = i; // treat i as pixel space
                double t = (virtualPixel) / pxScale;
                int yPix = graphPanel.sampleYPixels(t, graphPanel.getHeight());
                // map graph-panel sampling coordinate space to gamePanel vertical space:
                // we want ceiling to be within [20, H-120] so we remap proportionally:
                int gH = graphPanel.getHeight();
                double frac = (double)(yPix - graphPanel.topMargin) / Math.max(1, gH - graphPanel.topMargin - graphPanel.bottomMargin);
                int mapped = (int) (20 + frac * (H - 140));
                if (mapped < 16) mapped = 16;
                if (mapped > H - 80) mapped = H - 80;
                ceilingY[i] = mapped;
            }
            // reset scroll or keep? we'll keep scroll continuous
        }

        // basic mapping: given screen X, find virtual index
        private int screenXToIndex(int screenX) {
            int idx = screenX + scroll;
            if (idx < 0) idx = 0;
            if (idx >= virtualWidth) idx = virtualWidth - 1;
            return idx;
        }

        // game loop
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!running) { repaint(); return; }

            // physics for bird
            birdVy += 0.9; // gravity acceleration (tweak for feel)
            birdY += birdVy;

            // scroll level
            scroll += 3; // pixels per frame (speed)
            if (scroll >= virtualWidth - getWidth()) scroll = 0; // loop level

            // collision: get ceiling and floor at bird's screen x
            int idx = screenXToIndex(birdX);
            int ceil = ceilingY[idx];
            int gap;
            try {
                gap = Integer.parseInt(gapField.getText());
            } catch (NumberFormatException ex) { gap = 150; }
            int floor = Math.min(getHeight() - 40, ceil + gap);

            // check collisions with top/bottom bounds
            if (birdY - birdSize/2 < ceil || birdY + birdSize/2 > floor) {
                // collision -> restart
                running = false;
            } else {
                running = true;
            }

            // update score when passing certain thresholds (simple)
            score = (scroll / 20);

            // hue shift for colorful look
            hue += 0.005f;
            if (hue > 1f) hue = 0f;

            repaint();
        }

        // painting
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int W = getWidth();
            int H = getHeight();

            // dramatic neon background gradient
            Color bg1 = Color.getHSBColor((hue + 0.1f)%1f, 0.9f, 0.12f);
            Color bg2 = Color.getHSBColor((hue + 0.45f)%1f, 0.9f, 0.18f);
            g2.setPaint(new GradientPaint(0,0,bg1, W, H, bg2));
            g2.fillRect(0,0,W,H);

            // draw ceiling & floor as filled polys derived from ceilingY samples
            int visibleW = W;
            int[] topXs = new int[visibleW];
            int[] topYs = new int[visibleW];
            int[] botXs = new int[visibleW];
            int[] botYs = new int[visibleW];

            for (int x = 0; x < visibleW; x++) {
                int idx = screenXToIndex(x);
                int cY = ceilingY[idx];
                topXs[x] = x;
                topYs[x] = cY;

                int gap;
                try { gap = Integer.parseInt(gapField.getText()); } catch (Exception ex) { gap = 150; }
                int fY = Math.min(H - 40, cY + gap);
                botXs[x] = x;
                botYs[x] = fY;
            }

            // top region (ceiling) fill
            Polygon topPoly = new Polygon(topXs, topYs, visibleW);
            g2.setColor(new Color(180, 0, 255, 190)); // neon purple for ceiling
            g2.fill(topPoly);

            // bottom region (floor) fill (draw from bottom to floor to make gap visible)
            Polygon botPoly = new Polygon(botXs, botYs, visibleW);
            g2.setColor(new Color(0, 220, 150, 200)); // neon teal for floor
            // fill bottom area under botPoly:
            Polygon floorFill = new Polygon();
            for (int i = 0; i < visibleW; i++) floorFill.addPoint(botXs[i], botYs[i]);
            floorFill.addPoint(visibleW - 1, H);
            floorFill.addPoint(0, H);
            g2.fill(floorFill);

            // decorative grid lines (moving)
            g2.setStroke(new BasicStroke(1f));
            for (int x = 0; x < visibleW; x += 60) {
                g2.setColor(new Color(255, 255, 255, 15));
                g2.drawLine(x, 0, x, H);
            }

            // draw the moving "pipes" edges in rainbow
            for (int x = 0; x < visibleW; x += 20) {
                float h = (hue + x/ (float)visibleW) % 1f;
                g2.setColor(Color.getHSBColor(h, 1f, 1f));
                g2.drawLine(x, topYs[x], x, botYs[x]);
            }

            // draw bird
            // If not running, show red smashed bird
            if (!running) {
                g2.setColor(new Color(255, 50, 50));
                g2.fillOval(birdX - birdSize, (int)birdY - birdSize, birdSize*2, birdSize*2);
                g2.setFont(new Font("Comic Sans MS", Font.BOLD, 28));
                g2.setColor(Color.WHITE);
                g2.drawString("CRASHED! Press SPACE or CLICK to restart", 30, 40);
                g2.setFont(new Font("SansSerif", Font.BOLD, 20));
                g2.drawString("Score: " + score, W/2 - 40, 70);
                return;
            }

            // bird with glow
            for (int r = 14; r >= 6; r -= 4) {
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillOval(birdX - r, (int)birdY - r, r*2, r*2);
            }
            g2.setColor(Color.YELLOW);
            g2.fillOval(birdX - birdSize/2, (int)birdY - birdSize/2, birdSize, birdSize);
            g2.setColor(Color.BLACK);
            g2.fillOval(birdX + 3, (int)birdY - 6, 6, 6); // eye

            // HUD: colored border & labels
            g2.setColor(Color.getHSBColor((hue+0.2f)%1f, 1f, 1f));
            g2.setStroke(new BasicStroke(6f));
            g2.drawRoundRect(6, 6, W - 12, H - 12, 12, 12);

            g2.setFont(new Font("Comic Sans MS", Font.BOLD, 16));
            g2.setColor(Color.WHITE);
            g2.drawString("Flappy (Graph Level). Bird X = " + birdX, 14, 28);
            g2.drawString("Score: " + score, 14, 52);
            g2.drawString("Gap px: " + gapField.getText(), 14, 76);
        }

        // Controls: flap on space or mouse click
        @Override
        public void keyTyped(KeyEvent e) {}
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) flapOrRestart();
        }
        @Override
        public void keyReleased(KeyEvent e) {}

        @Override
        public void mouseClicked(MouseEvent e) { flapOrRestart(); }
        @Override public void mousePressed(MouseEvent e) {}
        @Override public void mouseReleased(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}

        private void flapOrRestart() {
            if (!running) {
                // restart: reset bird and resume
                birdY = getHeight() / 2.0;
                birdVy = 0;
                scroll = 0;
                score = 0;
                running = true;
            } else {
                birdVy = -9.5; // flap impulse
            }
        }
    }

    // ================ Entry ================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KinematicsRainbowFlappy());
    }
}
