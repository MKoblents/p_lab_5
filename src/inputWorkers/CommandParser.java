package inputWorkers;

import java.io.IOException;
import io.Reader;

public class CommandParser {
    private String commandName;
    private String rawArgument;
    private long longArg;
    private double doubleArg;
    private int intArg;
    private String xmlArg;
    private String pathArg;
    private String enumArg;

    private String listToString(String[] strings){
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < strings.length - 1; i++) {
            key.append(strings[i]);
        }
        return key.toString().toLowerCase();
    }
    public void parse(Reader reader) throws IOException {
        String line = reader.nextLine();
        System.out.println(line);
//        clearState();
        if (line == null || line.trim().isEmpty()) {
            return;
        }
        String[] parts = line.trim().split("\\s+");
        commandName = listToString(parts);
        System.out.println(commandName);
        rawArgument = parts[parts.length - 1].trim();
        if (rawArgument.startsWith("<") && rawArgument.endsWith(">")) {
            xmlArg = rawArgument;
            reader.setLastXmlString(xmlArg);
            return;
        }
        if (commandName.equals("executescript")) {
            pathArg = rawArgument;
            return;
        }
        try {
            longArg = Long.parseLong(rawArgument);
        } catch (NumberFormatException e) {
            try {
                doubleArg = Double.parseDouble(rawArgument);
            } catch (NumberFormatException e2) {
                try {
                    intArg = Integer.parseInt(rawArgument);
                } catch (NumberFormatException e3) {
                    commandName = line.replaceAll("\\s+", "").toLowerCase();
                }
            }
        }
    }
    public <T extends Enum<T>> T getEnumValue(Class<T> enumType) {
        if (enumArg == null) return null;
        try {
            return Enum.valueOf(enumType, enumArg.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
//    private void clearState() {
//        commandName = null;
//        rawArgument = null;
//        longArg = null;
//        doubleArg = null;
//        intArg = null;
//        xmlArg = null;
//        pathArg = null;
//    }
    public String getCommandName() { return commandName; }
    public String getRawArgument() { return rawArgument; }
    public long getLongArg() { return longArg; }
    public double getDoubleArg() { return doubleArg; }
    public int getIntArg() { return intArg; }
    public String getXmlArg() { return xmlArg; }
    public String getPathArg() {
        return pathArg;
    }
    public String getEnumArg() {
        return enumArg;
    }
}