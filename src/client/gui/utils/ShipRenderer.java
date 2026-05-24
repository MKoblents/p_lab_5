package client.gui.utils;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ShipRenderer {
    private static final Color[] USER_PALETTE = {
            Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE,
            Color.MAGENTA, Color.CYAN, Color.PINK, Color.YELLOW
    };

    private static final Map<String, Color> userColorCache = new HashMap<>();

    public static Color getUserColor(String owner) {
        String key = (owner != null && !owner.trim().isEmpty()) ? owner.trim() : "unknown";
        return userColorCache.computeIfAbsent(key, k -> {
            int hash = Math.abs(k.hashCode());
            return USER_PALETTE[hash % USER_PALETTE.length];
        });
    }

    public static void drawShip(Graphics2D g, int x, int y, Color color) {
        // Корпус (треугольник, нос вверх)
        int[] bodyX = {x, x - 10, x + 10};
        int[] bodyY = {y - 14, y + 10, y + 10};
        g.setColor(color);
        g.fillPolygon(bodyX, bodyY, 3);

        // Крылья
        int[] wingLX = {x - 10, x - 18, x - 4};
        int[] wingLY = {y + 4, y + 12, y + 10};
        int[] wingRX = {x + 10, x + 18, x + 4};
        int[] wingRY = {y + 4, y + 12, y + 10};
        g.setColor(color.darker());
        g.fillPolygon(wingLX, wingLY, 3);
        g.fillPolygon(wingRX, wingRY, 3);

        // Обводка
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1.5f));
        g.drawPolygon(bodyX, bodyY, 3);
        g.drawPolygon(wingLX, wingLY, 3);
        g.drawPolygon(wingRX, wingRY, 3);

        // Кабина
        g.setColor(Color.WHITE);
        g.fillOval(x - 3, y - 8, 6, 8);
        g.setColor(Color.DARK_GRAY);
        g.drawOval(x - 3, y - 8, 6, 8);
    }
}