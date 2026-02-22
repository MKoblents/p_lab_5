package inputWorkers;

import java.io.IOException;
import io.Reader;

public class CommandParser {
    private long lastLong;
    private double lastDouble;
    private int lastInt;
    private String lastXmlString;
    private String lastPath;
    public String parseCommand(Reader reader) throws IOException {
        String line = reader.nextLine();
        String[] parts = line.trim().split("\\s+");
        String lastPart = parts[parts.length - 1].trim();
        if (listToString(parts).toLowerCase().contains("executescript")){
            lastPath = lastPart;
            return "executescript";
        }
        try {
            lastLong = Long.parseLong(lastPart);
            return listToString(parts);
        } catch (NumberFormatException e) {
            try {
                lastDouble = Double.parseDouble(lastPart);
                return listToString(parts);
            } catch (NumberFormatException e2) {
                try {
                    lastInt = Integer.parseInt(lastPart);
                    return listToString(parts);
                } catch (NumberFormatException e3) {
                    if (lastPart.trim().startsWith("<") && lastPart.trim().endsWith(">")) {
                        lastXmlString = lastPart.trim();
                        return listToString(parts);
                    }
                    return line.replaceAll("\\s+", "").toLowerCase();
                }
            }
        }
    }
    private String listToString(String[] strings){
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < strings.length - 1; i++) {
            key.append(strings[i]);
        }
        return key.toString().toLowerCase();
    }

    public long getLastLong() {
        return lastLong;
    }

    public double getLastDouble() {
        return lastDouble;
    }

    public String getLastXmlString() {
        return lastXmlString;
    }

    public int getLastInt() {
        return lastInt;
    }

    public String getLastPath() {
        return lastPath;
    }
}
