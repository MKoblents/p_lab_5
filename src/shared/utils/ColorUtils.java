package shared.utils;

import java.awt.Color;

public class ColorUtils {

    /**
     * Generates a deterministic but varied color based on username hash.
     * Uses HSB color space to ensure good visibility (avoid very dark/saturated colors).
     * Same username → same color forever.
     */
    public static String getUserColorHex(String username) {
        if (username == null || username.trim().isEmpty()) {
            username = "unknown";
        }

        int hash = username.hashCode();

        int h = Math.abs(hash) % 360;
        int s = 70 + (Math.abs(hash >> 8) % 25);
        int b = 85 + (Math.abs(hash >> 16) % 15);

        Color color = Color.getHSBColor(h / 360f, s / 100f, b / 100f);
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static Color getUserColor(String username) {
        String hex = getUserColorHex(username);
        return Color.decode(hex);
    }
}