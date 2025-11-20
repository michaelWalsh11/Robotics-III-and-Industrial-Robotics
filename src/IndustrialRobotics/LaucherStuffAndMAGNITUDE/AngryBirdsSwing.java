package IndustrialRobotics.LaucherStuffAndMAGNITUDE;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * AngryBirdsSwing.java
 * Single-file Angry Birds style mini-game using only Java built-in classes (AWT/Swing).
 *
 * Compile:
 *   javac AngryBirdsSwing.java
 * Run:
 *   java IndustrialRobotics.LaucherStuffAndMAGNITUDE.AngryBirdsSwing
 *
 * Lush and colorful with trajectory prediction that uses the Launcher backend.
 */
public class AngryBirdsSwing extends JFrame {
    public AngryBirdsSwing() {
        setTitle("Lush Angry Birds (Swing) 🌈");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        GamePanel gp = new GamePanel(1000, 600);
        add(gp);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        gp.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AngryBirdsSwing());
    }
}

/* -------- Game Panel -------- */
class GamePanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
    private final int W, H;
    private final Timer timer;
    private final java.util.List<Pig> pigs = Collections.synchronizedList(new ArrayList<>());
    private final java.util.List<Block> blocks = Collections.synchronizedList(new ArrayList<>());
    private final java.util.List<Bird> birds = Collections.synchronizedList(new ArrayList<>());
    private final java.util.List<Particle> particles = Collections.synchronizedList(new ArrayList<>());

    private final Point2D.Double slingAnchor; // fixed sling anchor point
    private Bird currentBird = null;
    private boolean dragging = false;
    private Point dragPoint = new Point();
    private int score = 0;
    private int birdsLeft = 5;

    // Trajectory prediction points (ghost arc)
    private java.util.List<Point2D.Double> predictedPoints = Collections.synchronizedList(new ArrayList<>());

    // Appearance toggles
    private final Font uiFont = new Font("SansSerif", Font.BOLD, 16);

    GamePanel(int width, int height) {
        this.W = width;
        this.H = height;
        setPreferredSize(new Dimension(W, H));
        setBackground(Color.WHITE);
        addMouseListener(this);
        addMouseMotionListener(this);
        timer = new Timer(16, this); // ~60 FPS
        slingAnchor = new Point2D.Double(170, H - 160);
        resetLevel();
    }

    void start() {
        timer.start();
    }

    void resetLevel() {
        pigs.clear();
        blocks.clear();
        birds.clear();
        particles.clear();
        predictedPoints.clear();
        score = 0;
        birdsLeft = 5;

        // nice spread of pigs and shapes
        pigs.add(new Pig(760, H - 190, 24));
        pigs.add(new Pig(820, H - 190, 24));
        pigs.add(new Pig(890, H - 190, 24));
        pigs.add(new Pig(820, H - 260, 24));

        // tower of blocks (platform style)
        blocks.add(new Block(740, H - 160, 40, 20));
        blocks.add(new Block(780, H - 160, 40, 20));
        blocks.add(new Block(820, H - 160, 40, 20));
        blocks.add(new Block(860, H - 160, 40, 20));
        blocks.add(new Block(900, H - 160, 40, 20));

        blocks.add(new Block(780, H - 220, 40, 20));
        blocks.add(new Block(820, H - 220, 40, 20));
        blocks.add(new Block(860, H - 220, 40, 20));

        // large base block
        blocks.add(new Block(840, H - 120, 200, 12));

        spawnNewBird();
    }

    void spawnNewBird() {
        if (birdsLeft <= 0) {
            currentBird = null;
            return;
        }
        currentBird = new Bird(slingAnchor.x - 30, slingAnchor.y + 5, 18);
        birds.add(currentBird);
        birdsLeft--;
        predictedPoints.clear();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // physics update
        updatePhysics();
        repaint();
    }

    void updatePhysics() {
        // Update birds
        synchronized (birds) {
            Iterator<Bird> it = birds.iterator();
            while (it.hasNext()) {
                Bird b = it.next();
                b.update();

                // collision with ground
                if (b.y + b.radius > H - 40) {
                    b.y = H - 40 - b.radius;
                    b.vy *= -0.35; // bounce
                    b.vx *= 0.98;
                    if (Math.abs(b.vy) < 1) b.vy = 0;
                }

                // collision with blocks
                synchronized (blocks) {
                    for (Block block : blocks) {
                        if (block.isDestroyed) continue;
                        if (circleRectCollision(b.x, b.y, b.radius, block)) {
                            // simple response: push bird out and damp velocity
                            Rectangle2D.Double r = block.getRect();
                            Point2D.Double closest = closestPointOnRect(b.x, b.y, r);
                            double dx = b.x - closest.x;
                            double dy = b.y - closest.y;
                            double dist = Math.hypot(dx, dy);
                            if (dist == 0) dist = 0.001;
                            double overlap = b.radius - dist;
                            if (overlap > 0) {
                                b.x += (dx / dist) * overlap;
                                b.y += (dy / dist) * overlap;
                                // transfer some energy to block (makes it wobble)
                                block.vx += b.vx * 0.15;
                                block.vy += b.vy * 0.15;
                                b.vx *= -0.2;
                                b.vy *= -0.2;
                                block.health -= 10;
                                if (block.health <= 0) block.isDestroyed = true;
                            }
                        }
                    }
                }

                // collision with pigs
                synchronized (pigs) {
                    Iterator<Pig> pit = pigs.iterator();
                    while (pit.hasNext()) {
                        Pig p = pit.next();
                        if (p.isPopped) continue;
                        double dx = p.x - b.x;
                        double dy = p.y - b.y;
                        double dist = Math.hypot(dx, dy);
                        if (dist < p.radius + b.radius) {
                            // pop pig
                            p.isPopped = true;
                            score += 100;
                            explodeAt(p.x, p.y, 50);
                            // give bird recoil
                            b.vx *= 0.4;
                            b.vy *= -0.6;
                        }
                    }
                }

                // remove slow/out-of-bounds birds after they come to rest
                if (!b.launched && !dragging) {
                    // not launched yet
                } else {
                    if (Math.abs(b.vx) < 0.2 && Math.abs(b.vy) < 0.2 && b.y + b.radius >= H - 40 - 0.1) {
                        // settled
                        if (currentBird == b) {
                            // after a short delay spawn next bird
                            Timer t = new Timer(700, ev -> {
                                if (allPigsDead()) {
                                    // level cleared: celebration particles
                                    explodeAt(820, H - 200, 120);
                                    // small delay then reset
                                    new Timer(1200, ev2 -> resetLevel()).start();
                                } else {
                                    spawnNewBird();
                                }
                            });
                            t.setRepeats(false);
                            t.start();
                            currentBird = null;
                        }
                    }
                }

                // out of bounds to the left or right or high fall: remove
                if (b.x < -200 || b.x > W + 200 || b.y > H + 400) {
                    // bird gone
                    it.remove();
                    if (b == currentBird) spawnIfNeeded();
                }
            }
        }

        // update pigs (they may slowly slump if hit by blocks)
        synchronized (pigs) {
            for (Pig p : pigs) p.update();
        }

        // update blocks
        synchronized (blocks) {
            Iterator<Block> bit = blocks.iterator();
            List<Block> toRemove = new ArrayList<>();
            while (bit.hasNext()) {
                Block bl = bit.next();
                if (bl.isDestroyed) {
                    // explosion
                    explodeAt(bl.x + bl.w / 2, bl.y + bl.h / 2, 30);
                    toRemove.add(bl);
                } else {
                    bl.update();
                }
            }
            blocks.removeAll(toRemove);
        }

        // update particles
        synchronized (particles) {
            Iterator<Particle> pit = particles.iterator();
            while (pit.hasNext()) {
                Particle p = pit.next();
                p.update();
                if (p.life <= 0) pit.remove();
            }
        }
    }

    private void spawnIfNeeded() {
        if (currentBird == null && birdsLeft > 0) spawnNewBird();
    }

    private boolean allPigsDead() {
        synchronized (pigs) {
            for (Pig p : pigs) if (!p.isPopped) return false;
        }
        return true;
    }

    private void explodeAt(double cx, double cy, int count) {
        for (int i = 0; i < count; i++) {
            double ang = Math.random() * Math.PI * 2;
            double sp = 2 + Math.random() * 6;
            particles.add(new Particle(cx, cy, Math.cos(ang) * sp, Math.sin(ang) * sp, 40 + (int)(Math.random()*20)));
        }
    }

    /* ---- Utilities for collision ---- */
    private boolean circleRectCollision(double cx, double cy, double r, Block block) {
        Rectangle2D.Double rect = block.getRect();
        Point2D.Double closest = closestPointOnRect(cx, cy, rect);
        double dx = cx - closest.x;
        double dy = cy - closest.y;
        return (dx*dx + dy*dy) < (r*r);
    }

    private Point2D.Double closestPointOnRect(double px, double py, Rectangle2D.Double r) {
        double cx = Math.max(r.x, Math.min(px, r.x + r.width));
        double cy = Math.max(r.y, Math.min(py, r.y + r.height));
        return new Point2D.Double(cx, cy);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Lush gradient sky
        GradientPaint sky = new GradientPaint(0, 0, new Color(30, 115, 255), 0, H, new Color(180, 240, 255));
        g.setPaint(sky);
        g.fillRect(0, 0, W, H);

        // Sun with radial gradient
        drawSun(g, 130, 90);

        // rolling hills (layered with gradients)
        drawHills(g);

        // ground strip
        g.setColor(new Color(67, 142, 39));
        g.fillRect(0, H - 40, W, 40);
        g.setColor(new Color(45, 100, 28));
        g.drawLine(0, H - 40, W, H - 40);

        // draw blocks
        synchronized (blocks) {
            for (Block b : blocks) b.draw(g);
        }

        // draw pigs
        synchronized (pigs) {
            for (Pig p : pigs) p.draw(g);
        }

        // draw particles
        synchronized (particles) {
            for (Particle p : particles) p.draw(g);
        }

        // draw birds
        synchronized (birds) {
            for (Bird b : birds) b.draw(g);
        }

        // draw slingshot front and elastic when dragging or bird not launched
        drawSlingshot(g);

        // Draw predicted trajectory (ghost dots) - after slingshot so it's visible
        drawPredictedTrajectory(g);

        // UI overlay
        drawUI(g);

        g.dispose();
    }

    private void drawPredictedTrajectory(Graphics2D g) {
        synchronized (predictedPoints) {
            if (predictedPoints.isEmpty()) return;

            // line path
            Stroke oldStroke = g.getStroke();
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // translucent guide line
            g.setColor(new Color(255, 255, 255, 90));
            Point2D.Double prev = null;
            for (Point2D.Double p : predictedPoints) {
                if (prev != null) {
                    g.draw(new Line2D.Double(prev.x, prev.y, p.x, p.y));
                }
                prev = p;
            }

            // dotted points
            for (int i = 0; i < predictedPoints.size(); i++) {
                Point2D.Double p = predictedPoints.get(i);
                double sz = 6 * (1.0 - i / (double)Math.max(1, predictedPoints.size())); // fade size
                g.setColor(new Color(255, 255, 255, 160 - (i * 120 / Math.max(1, predictedPoints.size()))));
                g.fill(new Ellipse2D.Double(p.x - sz/2, p.y - sz/2, sz, sz));
            }
            g.setStroke(oldStroke);
        }
    }

    private void drawSun(Graphics2D g, int cx, int cy) {
        float[] dist = {0f, 1f};
        Color[] colors = {new Color(255, 250, 200, 220), new Color(255, 170, 60, 80)};
        RadialGradientPaint rg = new RadialGradientPaint(new Point(cx, cy), 80, dist, colors);
        g.setPaint(rg);
        g.fillOval(cx - 80, cy - 80, 160, 160);
        // center sparkle
        g.setColor(new Color(255, 255, 210, 200));
        g.fillOval(cx - 18, cy - 18, 36, 36);
    }

    private void drawHills(Graphics2D g) {
        // back hill
        Path2D.Double p1 = new Path2D.Double();
        p1.moveTo(-200, H);
        p1.curveTo(200, H - 260, 500, H - 200, 800, H - 280);
        p1.curveTo(930, H - 320, 1200, H - 120, 1400, H - 210);
        p1.lineTo(1400, H);
        p1.closePath();
        GradientPaint gp1 = new GradientPaint(0, H - 300, new Color(12, 90, 40), 0, H, new Color(48, 150, 60));
        g.setPaint(gp1);
        g.fill(p1);

        // front hill
        Path2D.Double p2 = new Path2D.Double();
        p2.moveTo(-200, H);
        p2.curveTo(100, H - 120, 300, H - 140, 680, H - 100);
        p2.curveTo(920, H - 70, 1100, H - 160, 1400, H - 60);
        p2.lineTo(1400, H);
        p2.closePath();
        GradientPaint gp2 = new GradientPaint(0, H - 150, new Color(90, 200, 110), 0, H, new Color(48, 140, 70));
        g.setPaint(gp2);
        g.fill(p2);
    }

    private void drawSlingshot(Graphics2D g) {
        // slingshot base posts
        int postW = 10, postH = 70;
        int ax = (int) slingAnchor.x, ay = (int) slingAnchor.y;
        // posts
        g.setColor(new Color(85, 46, 13));
        g.fillRoundRect(ax - 18, ay - postH, 12, postH, 6, 6);
        g.fillRoundRect(ax + 8, ay - postH, 12, postH, 6, 6);

        // elastic when available
        if (currentBird != null && (!currentBird.launched || dragging)) {
            Point2D.Double bPos = new Point2D.Double(currentBird.x, currentBird.y);
            // draw two elastic lines
            Stroke old = g.getStroke();
            g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            GradientPaint gp = new GradientPaint(ax - 18, ay - postH, new Color(120, 20, 20), ax + 8, ay - postH, new Color(220, 80, 80));
            g.setPaint(gp);
            g.draw(new Line2D.Double(ax - 10, ay - postH + 6, bPos.x, bPos.y));
            g.draw(new Line2D.Double(ax + 15, ay - postH + 6, bPos.x, bPos.y));
            g.setStroke(old);
        }
    }

    private void drawUI(Graphics2D g) {
        g.setFont(uiFont);
        // top-left score
        g.setColor(new Color(255, 255, 255, 180));
        g.fillRoundRect(10, 10, 170, 44, 12, 12);
        g.setColor(Color.DARK_GRAY);
        g.drawRoundRect(10, 10, 170, 44, 12, 12);
        g.setColor(new Color(30,30,30));
        g.drawString("Score: " + score, 22, 34);
        g.drawString("Birds Left: " + birdsLeft, 22, 54);

        // restart button
        int bx = W - 120, by = 12, bw = 100, bh = 36;
        g.setColor(new Color(255, 220, 220, 220));
        g.fillRoundRect(bx, by, bw, bh, 12, 12);
        g.setColor(new Color(160, 40, 40));
        g.drawRoundRect(bx, by, bw, bh, 12, 12);
        g.setColor(new Color(160, 40, 40));
        g.setFont(uiFont.deriveFont(Font.BOLD, 14f));
        g.drawString("Restart", bx + 26, by + 24);

        // level status center
        g.setFont(uiFont.deriveFont(18f));
        g.setColor(new Color(255,255,255,140));
        if (allPigsDead()) {
            g.drawString("Level Cleared! ✨", W/2 - 80, 40);
        } else if (birdsLeft == 0 && currentBird == null && birds.size()==0) {
            g.drawString("Out of birds! Try restart.", W/2 - 120, 40);
        }
    }

    /* -------- Mouse Handling (slingshot) -------- */
    @Override
    public void mousePressed(MouseEvent e) {
        // restart button check
        int bx = W - 120, by = 12, bw = 100, bh = 36;
        if (e.getX() >= bx && e.getX() <= bx + bw && e.getY() >= by && e.getY() <= by + bh) {
            resetLevel();
            return;
        }

        if (currentBird != null) {
            double dx = e.getX() - currentBird.x;
            double dy = e.getY() - currentBird.y;
            double d = Math.hypot(dx, dy);
            if (d < currentBird.radius + 10 && !currentBird.launched) {
                dragging = true;
                dragPoint.setLocation(e.getPoint());
                predictTrajectory(currentBird); // initial prediction
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (dragging && currentBird != null) {
            dragging = false;
            // compute launch vector: from anchor to mouse (drag point) reversed
            double dx = slingAnchor.x - e.getX();
            double dy = slingAnchor.y - e.getY();
            // limit power
            double maxPower = 28;
            double len = Math.hypot(dx, dy);
            if (len > maxPower) {
                dx = dx / len * maxPower;
                dy = dy / len * maxPower;
            }
            // apply velocity
            currentBird.vx = dx * 0.9;
            currentBird.vy = dy * 0.9;
            currentBird.launched = true;
            // slight spin
            currentBird.spin = (Math.random() - 0.5) * 0.4;

            // clear predicted arc (we launched)
            predictedPoints.clear();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (dragging && currentBird != null) {
            // move bird with mouse but clamp distance to max
            double dx = e.getX() - slingAnchor.x;
            double dy = e.getY() - slingAnchor.y;
            double max = 100;
            double len = Math.hypot(dx, dy);
            if (len > max) {
                dx = dx / len * max;
                dy = dy / len * max;
            }
            currentBird.x = slingAnchor.x + dx;
            currentBird.y = slingAnchor.y + dy;
            dragPoint.setLocation(e.getX(), e.getY());

            // ---- Trajectory prediction while dragging ----
            predictTrajectory(currentBird);

            repaint();
        }
    }

    // unused mouse methods
    @Override public void mouseMoved(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    /* -------- Trajectory prediction -------- */

    /**
     * Predict flight path based on the current bird position as if the user released it now.
     * Uses the Launcher backend to compute an arc. Converts Launcher Points to panel coordinates.
     */
    private void predictTrajectory(Bird b) {
        synchronized (predictedPoints) {
            predictedPoints.clear();
            if (b == null) return;

            // Derive the launch vector same as mouseReleased
            double dx = slingAnchor.x - b.x;
            double dy = slingAnchor.y - b.y;

            double maxPower = 28;
            double len = Math.hypot(dx, dy);
            if (len > maxPower) {
                dx = dx / len * maxPower;
                dy = dy / len * maxPower;
            }

            double vx = dx * 0.9;
            double vy = dy * 0.9;

            // Convert the velocity vector to magnitude/theta for Launcher
            double magnitude = Math.hypot(vx, vy);
            double theta = Math.atan2(vy, vx);

            // Use Launcher backend (keeps its own gravity/time-step). Launcher uses int Points.
            Launcher launcher = new Launcher((int)Math.round(b.x), (int)Math.round(b.y), theta, magnitude);
            java.util.List<Point> arc = launcher.getLaunchData();

            // Convert arc Points into Point2D.Double for drawing
            // Limit count to avoid oversized lists
            int cap = Math.min(arc.size(), 250);
            for (int i = 0; i < cap; i++) {
                Point p = arc.get(i);
                predictedPoints.add(new Point2D.Double(p.x, p.y));
            }
        }
    }

    /* -------- Inner classes for game objects -------- */

    static class Bird {
        double x, y;
        final double radius;
        double vx = 0, vy = 0;
        double spin = 0;
        boolean launched = false;
        Color baseColor = new Color(230, 60, 50);

        Bird(double x, double y, double r) {
            this.x = x;
            this.y = y;
            this.radius = r;
        }

        void update() {
            if (launched) {
                vy += 0.6; // gravity
                x += vx;
                y += vy;
                vx *= 0.999;
                vy *= 0.999;
            } else {
                // while not launched we rely on GamePanel to position the bird while dragging
            }
        }

        void draw(Graphics2D g) {
            // shiny radial bird
            Point2D center = new Point2D.Double(x - radius/2, y - radius/2);
            float[] dist = {0f, 1f};
            Color[] cols = {baseColor.brighter(), baseColor.darker()};
            RadialGradientPaint rg = new RadialGradientPaint(center, (float)radius*1.3f, dist, cols);
            g.setPaint(rg);
            g.fill(new Ellipse2D.Double(x - radius, y - radius, radius*2, radius*2));
            // beak
            Path2D.Double beak = new Path2D.Double();
            beak.moveTo(x + radius*0.6, y);
            beak.lineTo(x + radius*1.2, y - radius*0.3);
            beak.lineTo(x + radius*1.2, y + radius*0.3);
            beak.closePath();
            g.setColor(new Color(255, 190, 60));
            g.fill(beak);
            // eye
            g.setColor(Color.white);
            g.fill(new Ellipse2D.Double(x - radius/4, y - radius/2, radius*0.6, radius*0.6));
            g.setColor(Color.black);
            g.fill(new Ellipse2D.Double(x - radius/4 + radius*0.2, y - radius/2 + radius*0.2, radius*0.2, radius*0.2));
        }
    }

    static class Pig {
        double x, y;
        final double radius;
        boolean isPopped = false;
        double wobble = 0;
        double vx = 0, vy = 0;
        Color green = new Color(120, 200, 90);

        Pig(double x, double y, double r) {
            this.x = x; this.y = y; this.radius = r;
        }

        void update() {
            if (isPopped) {
                vy += 0.7;
                y += vy;
                x += vx;
            } else {
                // slight breathing/wobble
                wobble += 0.06;
            }
        }

        void draw(Graphics2D g) {
            if (isPopped) {
                // faded circle
                g.setColor(new Color(130, 200, 120, 160));
                g.fill(new Ellipse2D.Double(x - radius, y - radius, radius*2, radius*2));
                return;
            }
            // pig body (radial)
            Point2D center = new Point2D.Double(x - radius/3, y - radius/3);
            float[] dist = {0f, 1f};
            Color[] cols = {green.brighter(), green.darker()};
            RadialGradientPaint rg = new RadialGradientPaint(center, (float)radius*1.2f, dist, cols);
            g.setPaint(rg);
            g.fill(new Ellipse2D.Double(x - radius, y - radius, radius*2, radius*2));
            // snout
            g.setColor(new Color(180, 220, 140));
            g.fill(new Ellipse2D.Double(x - radius*0.4, y - radius*0.15, radius*0.8, radius*0.5));
            // nostrils
            g.setColor(new Color(80,110,50));
            g.fill(new Ellipse2D.Double(x - radius*0.15, y - radius*0.05, radius*0.15, radius*0.15));
            g.fill(new Ellipse2D.Double(x + radius*0.05, y - radius*0.05, radius*0.15, radius*0.15));
            // eyes
            g.setColor(Color.white);
            g.fill(new Ellipse2D.Double(x - radius*0.6, y - radius*0.5, radius*0.4, radius*0.4));
            g.setColor(Color.black);
            g.fill(new Ellipse2D.Double(x - radius*0.52, y - radius*0.48, radius*0.18, radius*0.18));

            // ears
            g.setColor(green.darker());
            g.fill(new Ellipse2D.Double(x - radius*0.85, y - radius*0.95, radius*0.35, radius*0.35));
            g.fill(new Ellipse2D.Double(x + radius*0.5, y - radius*0.95, radius*0.35, radius*0.35));
        }
    }

    static class Block {
        double x, y;
        double w, h;
        boolean isDestroyed = false;
        double vx = 0, vy = 0;
        int health = 100;

        Block(double x, double y, double w, double h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
        }

        void update() {
            // simple physics for wobble
            vy += 0.3;
            x += vx;
            y += vy;
            // ground collision (use panel height 600 as reference like earlier)
            if (y + h > 600 - 40) {
                y = 600 - 40 - h;
                vy *= -0.3;
                vx *= 0.9;
            }
            // friction
            vx *= 0.985;
        }

        Rectangle2D.Double getRect() {
            return new Rectangle2D.Double(x, y, w, h);
        }

        void draw(Graphics2D g) {
            GradientPaint gp = new GradientPaint((float)x, (float)y, new Color(210, 160, 60), (float)(x + w), (float)(y + h), new Color(160, 110, 40));
            g.setPaint(gp);
            g.fill(new Rectangle2D.Double(x, y, w, h));
            g.setColor(new Color(120, 80, 30));
            g.draw(new Rectangle2D.Double(x, y, w, h));
            // crack if low health
            if (health < 50) {
                g.setStroke(new BasicStroke(2f));
                g.drawLine((int)x, (int)(y + h/2), (int)(x + w), (int)(y + h/2 - 8));
            }
        }
    }

    static class Particle {
        double x, y, vx, vy;
        int life;
        Color c;

        Particle(double x, double y, double vx, double vy, int life) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.life = life;
            this.c = randomNiceColor();
        }

        void update() {
            life--;
            vy += 0.15;
            x += vx;
            y += vy;
            vx *= 0.98;
            vy *= 0.99;
        }

        void draw(Graphics2D g) {
            float alpha = Math.max(0, life / 60f);
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(alpha * 255)));
            double s = Math.max(1.0, life / 6.0);
            g.fill(new Ellipse2D.Double(x - s/2, y - s/2, s, s));
        }

        static Color randomNiceColor() {
            int r = 150 + (int)(Math.random() * 100);
            int g = 80 + (int)(Math.random() * 120);
            int b = 60 + (int)(Math.random() * 150);
            return new Color(Math.min(255, r), Math.min(255, g), Math.min(255, b));
        }
    }
}
