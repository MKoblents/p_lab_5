package client.gui.window;

import client.gui.GuiClientApp;
import client.gui.MainWindow;
import client.gui.utils.GuiUtils;
import client.gui.utils.ShipRenderer;
import client.utils.LocaleManager;
import shared.models.SpaceMarine;
import shared.utils.ColorUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;

public class SpaceMarineCanvas extends JPanel {
    private List<SpaceMarine> marines = new ArrayList<>();
    private final Map<String, Color> userColors = new HashMap<>();

    private double zoom = 1.0;
    private double panX = 0.0;
    private double panY = 0.0;
    private boolean isPanning = false;
    private Point lastMousePos;

    private Rectangle zoomInRect = new Rectangle();
    private Rectangle zoomOutRect = new Rectangle();
    private static final int BTN_SIZE = 32;
    private static final int BTN_GAP = 8;
    private static final int BTN_MARGIN = 15;
    private static final double GRID_STEP = 50.0;
    private Consumer<SpaceMarine> onMarineDoubleClick;
    private javax.swing.Timer clickTimer;
    private SpaceMarine pendingMarine;

    private static final Color GRID_COLOR = new Color(255, 210, 220, 180);
    private static final Color AXIS_COLOR = GuiUtils.PRIMARY_DARK;
    private static final Color BTN_BG = Color.WHITE;
    private static final Color BTN_HOVER = GuiUtils.PRIMARY_LIGHT;
    private static final Color BTN_ACTIVE = GuiUtils.PRIMARY_COLOR;
    private static final Color BTN_TEXT = GuiUtils.PRIMARY_DARK;

    private static final Color[] USER_PALETTE = {
            new Color(255, 105, 180), new Color(219, 112, 147),
            new Color(255, 160, 180), new Color(199, 21, 133),
            new Color(255, 130, 170), new Color(178, 34, 34)
    };
    public void setOnMarineDoubleClick(Consumer<SpaceMarine> callback) {
        this.onMarineDoubleClick = callback;
    }

    public SpaceMarineCanvas() {
        setBackground(GuiUtils.BACKGROUND_COLOR);
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        setupListeners();
    }

    private void setupListeners() {
        addMouseWheelListener(e -> {
            double factor = e.getWheelRotation() < 0 ? 1.15 : 0.85;
            applyZoom(factor, e.getX(), e.getY());
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isMiddleMouseButton(e) || SwingUtilities.isRightMouseButton(e)) {
                    isPanning = true;
                    lastMousePos = e.getPoint();
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    if (zoomInRect.contains(e.getPoint())) applyZoom(1.2, getWidth()/2, getHeight()/2);
                    else if (zoomOutRect.contains(e.getPoint())) applyZoom(0.8, getWidth()/2, getHeight()/2);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPanning = false;
                setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) return;
                if (zoomInRect.contains(e.getPoint()) || zoomOutRect.contains(e.getPoint())) return;

                SpaceMarine clicked = findMarineAtPoint(e.getX(), e.getY());
                if (clicked == null) return;

                if (e.getClickCount() == 2) {
                    // Двойной клик: отменяем таймер и открываем апдейт
                    if (clickTimer != null) clickTimer.stop();
                    if (onMarineDoubleClick != null) {
                        onMarineDoubleClick.accept(clicked);
                    }
                } else if (e.getClickCount() == 1) {
                    // Одинарный клик: запускаем задержку 250мс
                    pendingMarine = clicked;
                    if (clickTimer == null) {
                        clickTimer = new javax.swing.Timer(250, evt -> {
                            showMarineInfo(pendingMarine);
                            pendingMarine = null;
                        });
                        clickTimer.setRepeats(false);
                    }
                    clickTimer.restart();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isPanning) {
                    panX += e.getX() - lastMousePos.x;
                    panY += e.getY() - lastMousePos.y;
                    lastMousePos = e.getPoint();
                    repaint();
                }
            }
        });

        // Клавиатурный зум (оставляем как был)
        InputMap im = getInputMap(WHEN_FOCUSED);
        ActionMap am = getActionMap();
        im.put(KeyStroke.getKeyStroke('+'), "zoomIn");
        im.put(KeyStroke.getKeyStroke('-'), "zoomOut");
        im.put(KeyStroke.getKeyStroke('='), "zoomIn");
        im.put(KeyStroke.getKeyStroke('_'), "zoomOut");
        am.put("zoomIn", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { applyZoom(1.2, getWidth()/2, getHeight()/2); }
        });
        am.put("zoomOut", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { applyZoom(0.8, getWidth()/2, getHeight()/2); }
        });
    }
    private SpaceMarine findMarineAtPoint(int screenX, int screenY) {
        double hitRadius = 25.0 / zoom;

        for (SpaceMarine m : marines) {
            if (m.getCoordinates() == null) continue;

            double wx = m.getCoordinates().getX();
            double wy = m.getCoordinates().getY();


            double scaledX = wx * GRID_STEP;
            double scaledY = wy * GRID_STEP;
            double centerX = getWidth() / 2.0 + panX;
            double centerY = getHeight() / 2.0 + panY;

            int sx = (int) Math.round(scaledX * zoom + centerX);
            int sy = (int) Math.round(-scaledY * zoom + centerY);

            double dx = screenX - sx;
            double dy = screenY - sy;
            if (dx * dx + dy * dy <= hitRadius * hitRadius) {
                return m;
            }
        }
        return null;
    }

    private void showMarineInfo(SpaceMarine marine) {
        GuiUtils.showMessageDialog(
                null,
                LocaleManager.get("canvas.marine.details"),
                marine.toString());
    }

    private void applyZoom(double factor, int centerX, int centerY) {
        double oldZoom = zoom;
        zoom *= factor;
        zoom = Math.max(0.1, Math.min(10.0, zoom));

        double worldX = (centerX - getWidth()/2.0 - panX) / oldZoom;
        double worldY = -(centerY - getHeight()/2.0 - panY) / oldZoom;
        panX = centerX - getWidth()/2.0 - worldX * zoom;
        panY = centerY - getHeight()/2.0 + worldY * zoom;

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.translate(getWidth()/2.0 + panX, getHeight()/2.0 + panY);
        g2d.scale(zoom, -zoom);

        drawGrid(g2d);
        drawAxes(g2d);
        for (SpaceMarine m : marines) drawMarine(g2d, m);

        g2d.setTransform(new java.awt.geom.AffineTransform());
        drawControls(g2d);
    }

    private void drawGrid(Graphics2D g2d) {
        double left   = (-getWidth()/2.0  - panX) / zoom;
        double right  = ( getWidth()/2.0  - panX) / zoom;
        double bottom = ( getHeight()/2.0 + panY) / zoom;
        double top    = (-getHeight()/2.0 + panY) / zoom;

        g2d.setColor(GRID_COLOR);
        g2d.setStroke(new BasicStroke(1.0f / (float)zoom));

        int startX = (int)Math.floor(left / GRID_STEP) * (int)GRID_STEP;
        int startY = (int)Math.floor(top / GRID_STEP) * (int)GRID_STEP;

        for (int x = startX; x <= right; x += GRID_STEP) {
            g2d.drawLine(x, (int)top, x, (int)bottom);
        }
        for (int y = startY; y <= bottom; y += GRID_STEP) {
            g2d.drawLine((int)left, y, (int)right, y);
        }
    }

    private void drawAxes(Graphics2D g2d) {
        g2d.setColor(AXIS_COLOR);
        g2d.setStroke(new BasicStroke(2.0f / (float)zoom));

        g2d.drawLine(-100000, 0, 100000, 0);
        g2d.drawLine(0, -100000, 0, 100000);

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2d.drawString("X", 9500, -15);
        g2d.drawString("Y", 15, -9500);
    }

    private void drawMarine(Graphics2D g2d, SpaceMarine m) {
        if (m.getCoordinates() == null) return;

        double wx = m.getCoordinates().getX();
        double wy = m.getCoordinates().getY();
        Color userColor = Color.decode(ColorUtils.getUserColorHex(m.getOwner()));

        double scaledX = wx * GRID_STEP;
        double scaledY = wy * GRID_STEP;

        double centerX = getWidth() / 2.0 + panX;
        double centerY = getHeight() / 2.0 + panY;

        int sx = (int) Math.round(scaledX * zoom + centerX);
        int sy = (int) Math.round(-scaledY * zoom + centerY);


        AffineTransform originalTransform = g2d.getTransform();
        g2d.setTransform(new AffineTransform());

        ShipRenderer.drawShip(g2d, sx, sy, userColor);

        g2d.setTransform(originalTransform);
    }

    private void drawControls(Graphics2D g2d) {
        int x = getWidth() - BTN_SIZE - BTN_MARGIN;
        int y = getHeight() - 2 * BTN_SIZE - 2 * BTN_MARGIN - BTN_MARGIN;

        zoomInRect.setBounds(x, y, BTN_SIZE, BTN_SIZE);
        drawRoundButton(g2d, zoomInRect, "+", false);

        zoomOutRect.setBounds(x, y + BTN_SIZE + BTN_MARGIN, BTN_SIZE, BTN_SIZE);
        drawRoundButton(g2d, zoomOutRect, "−", false);
    }

    private void drawRoundButton(Graphics2D g2d, Rectangle r, String text, boolean hovered) {
        g2d.setColor(hovered ? BTN_HOVER : BTN_BG);
        g2d.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        g2d.setColor(GuiUtils.PRIMARY_COLOR);
        g2d.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);

        g2d.setColor(BTN_TEXT);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(text, r.x + (r.width - fm.stringWidth(text))/2, r.y + (r.height + fm.getAscent())/2 - 2);
    }

    public void setMarines(List<SpaceMarine> marines) {
        this.marines = marines != null ? new ArrayList<>(marines) : new ArrayList<>();
        userColors.clear();
        for (SpaceMarine m : this.marines) {
            String owner = m.getOwner() != null ? m.getOwner() : "unknown";
            if (!userColors.containsKey(owner)) {
                int hash = Math.abs(owner.hashCode());
                userColors.put(owner, USER_PALETTE[hash % USER_PALETTE.length]);
            }
        }
        repaint();
    }

    public double getZoom() { return zoom; }
    public void setZoom(double zoom) { this.zoom = Math.max(0.1, Math.min(10.0, zoom)); repaint(); }
    public void resetView() { panX = 0; panY = 0; zoom = 1.0; repaint(); }
}