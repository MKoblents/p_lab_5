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
        int scale = 4;

        int[] bodyX = {x, x - 10 * scale, x + 10 * scale};
        int[] bodyY = {y - 14 * scale, y + 10 * scale, y + 10 * scale};
        g.setColor(color);
        g.fillPolygon(bodyX, bodyY, 3);

        int[] wingLX = {x - 10 * scale, x - 18 * scale, x - 4 * scale};
        int[] wingLY = {y + 4 * scale, y + 12 * scale, y + 10 * scale};
        int[] wingRX = {x + 10 * scale, x + 18 * scale, x + 4 * scale};
        int[] wingRY = {y + 4 * scale, y + 12 * scale, y + 10 * scale};
        g.setColor(color);
        g.fillPolygon(wingLX, wingLY, 3);
        g.fillPolygon(wingRX, wingRY, 3);

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1.5f * scale));
        g.drawPolygon(bodyX, bodyY, 3);
        g.drawPolygon(wingLX, wingLY, 3);
        g.drawPolygon(wingRX, wingRY, 3);

        g.setColor(Color.WHITE);
        g.fillOval(x - 3 * scale, y - 8 * scale, 6 * scale, 8 * scale);
        g.setColor(Color.DARK_GRAY);
        g.drawOval(x - 3 * scale, y - 8 * scale, 6 * scale, 8 * scale);
    }
}