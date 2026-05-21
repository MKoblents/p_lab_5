package client.gui;

import shared.models.SpaceMarine;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpaceMarineCanvas extends JPanel {
    private List<SpaceMarine> marines = new ArrayList<>();
    private Map<String, Color> userColors = new HashMap<>();
    private static final int DOT_SIZE = 15;
    private static final int PADDING = 50;

    private static final Color[] USER_COLORS = {
            Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE,
            Color.MAGENTA, Color.CYAN, Color.PINK, Color.YELLOW
    };

    public SpaceMarineCanvas() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));
    }

    public void setMarines(List<SpaceMarine> marines) {
        this.marines = marines != null ? new ArrayList<>(marines) : new ArrayList<>();
        assignUserColors();
        repaint();
    }

    private void assignUserColors() {
        userColors.clear();
        int colorIndex = 0;
        for (SpaceMarine marine : marines) {
            String owner = marine.getOwner() != null ? marine.getOwner() : "unknown";
            if (!userColors.containsKey(owner)) {
                userColors.put(owner, USER_COLORS[colorIndex % USER_COLORS.length]);
                colorIndex++;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        drawGrid(g2d, width, height);

        drawAxes(g2d, width, height);

        for (SpaceMarine marine : marines) {
            drawMarine(g2d, marine, width, height);
        }

        drawLegend(g2d);
    }

    private void drawGrid(Graphics2D g2d, int width, int height) {
        g2d.setColor(new Color(240, 240, 240));
        int gridSize = 40;

        for (int x = PADDING; x < width - PADDING; x += gridSize) {
            g2d.drawLine(x, PADDING, x, height - PADDING);
        }
        for (int y = PADDING; y < height - PADDING; y += gridSize) {
            g2d.drawLine(PADDING, y, width - PADDING, y);
        }
    }

    private void drawAxes(Graphics2D g2d, int width, int height) {
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));

        int centerX = width / 2;
        g2d.drawLine(PADDING, height / 2, width - PADDING, height / 2);

        int centerY = height / 2;
        g2d.drawLine(width / 2, PADDING, width / 2, height - PADDING);

        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("X", width - PADDING + 5, height / 2);
        g2d.drawString("Y", width / 2, PADDING - 10);
    }

    private void drawMarine(Graphics2D g2d, SpaceMarine marine, int canvasWidth, int canvasHeight) {
        if (marine.getCoordinates() == null) return;

        long x = marine.getCoordinates().getX();
        long y = marine.getCoordinates().getY();

        double scale = Math.min(
                (canvasWidth - 2 * PADDING) / 2000.0,
                (canvasHeight - 2 * PADDING) / 2000.0
        );

        int drawX = canvasWidth / 2 + (int) (x * scale);
        int drawY = canvasHeight / 2 - (int) (y * scale);

        String owner = marine.getOwner() != null ? marine.getOwner() : "unknown";
        Color color = userColors.getOrDefault(owner, Color.GRAY);

        g2d.setColor(color);
        g2d.fillOval(drawX - DOT_SIZE / 2, drawY - DOT_SIZE / 2, DOT_SIZE, DOT_SIZE);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawOval(drawX - DOT_SIZE / 2, drawY - DOT_SIZE / 2, DOT_SIZE, DOT_SIZE);

        g2d.setFont(new Font("Arial", Font.PLAIN, 9));
        String label = marine.getName() != null ? marine.getName() : String.valueOf(marine.getId());
        FontMetrics fm = g2d.getFontMetrics();
        int labelWidth = fm.stringWidth(label);
        g2d.setColor(Color.BLACK);
        g2d.drawString(label, drawX - labelWidth / 2, drawY - DOT_SIZE - 2);
    }

    private void drawLegend(Graphics2D g2d) {
        int legendX = 10;
        int legendY = 20;

        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.setColor(Color.BLACK);
        g2d.drawString("Legend:", legendX, legendY);

        int y = legendY + 20;
        for (Map.Entry<String, Color> entry : userColors.entrySet()) {
            g2d.setColor(entry.getValue());
            g2d.fillOval(legendX, y - 8, 12, 12);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(legendX, y - 8, 12, 12);
            g2d.drawString(entry.getKey(), legendX + 20, y);
            y += 25;
        }
    }

    public void clear() {
        marines.clear();
        userColors.clear();
        repaint();
    }
}