package Robotics_III.chatGUIProject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Kinematics Graphing Tool - Swing application
 *
 * - Multiple toggleable lines
 * - Random line generator
 * - Scales to show the full curve
 * - Light, modern UI
 * - Legend shows full parameters for each line
 */
public class KinematicsGraphGUI extends JFrame {

    private final JTextField posField = new JTextField("0");
    private final JTextField velField = new JTextField("5");
    private final JTextField timeField = new JTextField("5");
    private final JTextField accField = new JTextField("0");

    private final GraphPanel graphPanel = new GraphPanel();
    private final DefaultListModel<GraphPanel.LineData> listModel = new DefaultListModel<>();
    private final JList<GraphPanel.LineData> legendList = new JList<>(listModel);

    public KinematicsGraphGUI() {
        super("Kinematics Graphing Tool");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));

        // ---------------- Left: Input Controls ----------------
        JPanel left = new JPanel(new BorderLayout());
        left.setBorder(new EmptyBorder(12, 12, 12, 0));

        JPanel inputCard = new JPanel(new GridBagLayout());
        inputCard.setBorder(BorderFactory.createTitledBorder("Kinematics Input"));
        inputCard.setBackground(new Color(250, 250, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        inputCard.add(new JLabel("Starting Position (x0):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        inputCard.add(posField, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        inputCard.add(new JLabel("Initial Velocity (v):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        inputCard.add(velField, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.fill = GridBagConstraints.NONE;
        inputCard.add(new JLabel("Time (t):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        inputCard.add(timeField, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.fill = GridBagConstraints.NONE;
        inputCard.add(new JLabel("Acceleration (a):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        inputCard.add(accField, gbc);

        JPanel buttonRow = new JPanel(new GridLayout(1, 3, 8, 0));
        JButton addBtn = new JButton("Add Line");
        JButton randBtn = new JButton("Random Line");
        JButton clearBtn = new JButton("Clear All");
        buttonRow.add(addBtn); buttonRow.add(randBtn); buttonRow.add(clearBtn);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        inputCard.add(buttonRow, gbc);

        left.add(inputCard, BorderLayout.NORTH);

        // ---------------- Legend Section ----------------
        JPanel legendCard = new JPanel(new BorderLayout());
        legendCard.setBorder(BorderFactory.createTitledBorder("Lines (click to toggle)"));

        legendList.setCellRenderer(new LegendCellRenderer());
        legendList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        legendList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int idx = legendList.locationToIndex(e.getPoint());
                if (idx >= 0) {
                    GraphPanel.LineData ld = listModel.get(idx);
                    ld.visible = !ld.visible;
                    legendList.repaint();
                    graphPanel.repaint();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(legendList);
        scroll.setPreferredSize(new Dimension(240, 280));
        legendCard.add(scroll, BorderLayout.CENTER);

        left.add(legendCard, BorderLayout.CENTER);

        add(left, BorderLayout.WEST);

        // ---------------- Center Graph Panel ----------------
        JPanel centerCard = new JPanel(new BorderLayout());
        centerCard.setBorder(new EmptyBorder(12, 0, 12, 12));
        centerCard.add(graphPanel, BorderLayout.CENTER);
        add(centerCard, BorderLayout.CENTER);

        // ---------------- Button Actions ----------------
        addBtn.addActionListener(e -> addLineFromFields());
        randBtn.addActionListener(e -> addRandomLine());
        clearBtn.addActionListener(e -> {
            graphPanel.clearLines();
            listModel.clear();
        });

        addDefaultLines();

        getContentPane().setBackground(new Color(240, 240, 245));
        setVisible(true);
    }

    private void addDefaultLines() {
        addLine(0, 5, 5, 0);
        addLine(0, 2, 8, -0.3);
    }

    private void addLineFromFields() {
        try {
            double x0 = Double.parseDouble(posField.getText().trim());
            double v = Double.parseDouble(velField.getText().trim());
            double t = Double.parseDouble(timeField.getText().trim());
            double a = Double.parseDouble(accField.getText().trim());
            addLine(x0, v, t, a);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Enter valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addRandomLine() {
        double x0 = Math.random() * 10 - 5;
        double v = Math.random() * 14 - 7;
        double t = 3 + Math.random() * 7;
        double a = Math.random() * 6 - 3;
        addLine(x0, v, t, a);
    }

    private void addLine(double x0, double v, double t, double a) {
        GraphPanel.LineData ld = graphPanel.addLine(x0, v, t, a);
        listModel.addElement(ld);
        legendList.ensureIndexIsVisible(listModel.size() - 1);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(KinematicsGraphGUI::new);
    }

    // ================================================================
    //  GRAPH PANEL CLASS
    // ================================================================
    static class GraphPanel extends JPanel {

        private final List<LineData> lines = new ArrayList<>();
        private final Color[] palette = {
                new Color(220, 38, 38),
                new Color(37, 99, 235),
                new Color(16, 185, 129),
                new Color(168, 85, 247),
                new Color(245, 158, 11),
                new Color(6, 182, 212)
        };

        public GraphPanel() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));
        }

        public LineData addLine(double x0, double v, double t, double a) {

            double endT = Math.max(Math.abs(t) * 1.5, 6);
            double step = Math.max(0.02, endT / 800.0);

            List<Double> ts = new ArrayList<>();
            List<Double> xs = new ArrayList<>();

            for (double time = 0; time <= endT + 1e-9; time += step) {
                double x = x0 + v * time + 0.5 * a * time * time;
                ts.add(time);
                xs.add(x);
            }

            Color c = palette[lines.size() % palette.length];
            LineData ld = new LineData(
                    "Line " + (lines.size() + 1),
                    ts, xs, c,
                    x0, v, t, a
            );

            lines.add(ld);
            repaint();
            return ld;
        }

        public void clearLines() {
            lines.clear();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int marginLeft = 60, marginRight = 20, marginTop = 20, marginBottom = 60;

            g2.setColor(new Color(250, 250, 250));
            g2.fillRect(0, 0, w, h);

            double maxT = 1, minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;

            for (LineData ld : lines) {
                if (!ld.visible) continue;
                if (!ld.t.isEmpty()) maxT = Math.max(maxT, ld.t.get(ld.t.size() - 1));
                for (double xVal : ld.x) {
                    minX = Math.min(minX, xVal);
                    maxX = Math.max(maxX, xVal);
                }
            }

            if (minX == Double.POSITIVE_INFINITY) { minX = -1; maxX = 10; }

            double pad = (maxX - minX) * 0.12 + 0.5;
            minX -= pad; maxX += pad;

            g2.setColor(new Color(230, 230, 230));
            for (int i = 0; i < 10; i++) {
                int gx = marginLeft + (int)((w - marginLeft - marginRight) * (i / 10.0));
                g2.drawLine(gx, marginTop, gx, h - marginBottom);
            }
            for (int i = 0; i < 8; i++) {
                int gy = marginTop + (int)((h - marginTop - marginBottom) * (i / 8.0));
                g2.drawLine(marginLeft, gy, w - marginRight, gy);
            }

            g2.setColor(new Color(110, 110, 110));
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(marginLeft, h - marginBottom, w - marginRight, h - marginBottom);
            g2.drawLine(marginLeft, h - marginBottom, marginLeft, marginTop);

            g2.setFont(g2.getFont().deriveFont(12f));
            g2.drawString("Time (s)", w / 2 - 10, h - 20);
            g2.drawString("Position (m)", 10, marginTop + 15);

            for (LineData ld : lines) {
                if (!ld.visible) continue;

                g2.setColor(ld.color);
                g2.setStroke(new BasicStroke(2f));

                int prevX = -1, prevY = -1;
                for (int i = 0; i < ld.t.size(); i++) {
                    double time = ld.t.get(i);
                    double xVal = ld.x.get(i);

                    int sx = marginLeft + (int)((w - marginLeft - marginRight) * (time / maxT));
                    int sy = marginTop + (int)((h - marginTop - marginBottom) * (1.0 - (xVal - minX) / (maxX - minX)));

                    if (prevX >= 0) g2.drawLine(prevX, prevY, sx, sy);

                    prevX = sx; prevY = sy;
                }

                int lx = prevX + 6;
                int ly = Math.max(marginTop + 12, Math.min(h - marginBottom - 6, prevY));

                g2.fill(new Ellipse2D.Double(lx - 4, ly - 8, 8, 8));
                g2.setColor(Color.DARK_GRAY);
                g2.drawString(ld.name, lx + 10, ly);
            }

            g2.dispose();
        }

        // ---------------- LINE DATA CLASS ----------------
        static class LineData {
            String name;
            List<Double> t, x;
            Color color;
            boolean visible = true;

            double x0, v, totalT, a;

            LineData(String name, List<Double> t, List<Double> x, Color color,
                     double x0, double v, double totalT, double a) {

                this.name = name;
                this.t = t;
                this.x = x;
                this.color = color;

                this.x0 = x0;
                this.v = v;
                this.totalT = totalT;
                this.a = a;
            }

            @Override
            public String toString() {
                return name + " — x0=" + x0 + ", v=" + v + ", t=" + totalT + ", a=" + a +
                        (visible ? "" : " (hidden)");
            }
        }
    }

    // ================================================================
    //  LEGEND CELL RENDERER
    // ================================================================
    static class LegendCellRenderer extends JPanel implements ListCellRenderer<GraphPanel.LineData> {

        private final JLabel swatch = new JLabel();
        private final JLabel text = new JLabel();

        public LegendCellRenderer() {
            setLayout(new BorderLayout(8, 0));
            swatch.setPreferredSize(new Dimension(18, 18));
            swatch.setOpaque(true);
            add(swatch, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
            setBorder(new EmptyBorder(6, 6, 6, 6));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends GraphPanel.LineData> list,
                                                      GraphPanel.LineData value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {

            if (value != null) {
                swatch.setBackground(value.color);
                text.setText(value.toString());
            }

            setBackground(isSelected ? new Color(230, 240, 255) : Color.WHITE);
            return this;
        }
    }
}
