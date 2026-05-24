package client.gui.window;

import client.gui.utils.GuiUtils;
import client.gui.utils.ShipRenderer;
import shared.models.SpaceMarine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        double gridSize = 50.0;
        g2d.setColor(GRID_COLOR);
        g2d.setStroke(new BasicStroke(1.0f / (float)zoom));

        int startX = (int)Math.floor(left / gridSize) * (int)gridSize;
        int startY = (int)Math.floor(top / gridSize) * (int)gridSize;

        for (int x = startX; x <= right; x += gridSize) {
            g2d.drawLine(x, (int)top, x, (int)bottom);
        }
        for (int y = startY; y <= bottom; y += gridSize) {
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

        // 1. Получаем мировые координаты
        double wx = m.getCoordinates().getX();
        double wy = m.getCoordinates().getY();

        // 2. Преобразуем в экранные координаты (пиксели)
        // Формула: screen = (world * zoom) + center + pan
        // Y инвертирован: в мире +Y вверх, на экране +Y вниз
        int sx = (int) ((wx * zoom) + getWidth() / 2.0 + panX);
        int sy = (int) ((-wy * zoom) + getHeight() / 2.0 + panY);

        // 3. Получаем цвет пользователя (детерминированный)
        Color userColor = ShipRenderer.getUserColor(m.getOwner());

        // 4. Сохраняем текущую трансформацию (мир + зум)
        AffineTransform originalTransform = g2d.getTransform();

        // 5. Сбрасываем трансформацию для отрисовки в экранных координатах
        g2d.setTransform(new AffineTransform());

        // 6. Рисуем кораблик фиксированного размера в пикселях экрана
        ShipRenderer.drawShip(g2d, sx, sy, userColor);

        // 7. Восстанавливаем мировую трансформацию для продолжения отрисовки сетки/осей
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