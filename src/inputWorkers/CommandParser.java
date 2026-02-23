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

    private String listToString(String[] strings, int start, int finish){
        try {
            if (start == finish){
                return strings[start];
            }
            StringBuilder key = new StringBuilder();
            for (int i = start; i < finish; i++) {
                key.append(strings[i]);
            }
            return key.toString().toLowerCase();
        }catch (IndexOutOfBoundsException e){
            return null;
        }
    }
    public void parse(Reader reader) throws IOException {
        String line = reader.nextLine();
        if (line == null || line.trim().isEmpty()) {
            return;
        }
        if (line.trim().startsWith("#")) {
            commandName = null;
            return;
        }
        String[] parts = line.trim().split("\\s+");
        if (parts.length == 1){
            commandName = line.trim().toLowerCase();
            return;
        }
        commandName = parts[0];
        if (commandName.equals("add") || commandName.equals("remove_greater")){
            xmlArg = listToString(parts, 1, parts.length);
            reader.setLastXmlString(xmlArg);
            return;
        }
        if (commandName.equals("update")|| commandName.equals("remove_by_id")){
            try {
                longArg = Long.parseLong(parts[1]);
                return;
            } catch (NumberFormatException e) {
                longArg = 0;
            }
        }
        if (commandName.equals("insert_at")){
            try {
                intArg = Integer.parseInt(parts[1]);
                return;
            } catch (NumberFormatException e) {
                intArg = 0;
            }
        }
        if (commandName.equals("execute_script")) {
            pathArg = parts[1];
            return;
        }
        if (commandName.equals("filter_less_than_melee_weapon")){
            enumArg = parts[1];
            return;
        }
        xmlArg = listToString(parts, 2, parts.length);
        reader.setLastXmlString(xmlArg);

    }
    public <T extends Enum<T>> T getEnumValue(Class<T> enumType) {
        if (enumArg == null) return null;
        try {
            return Enum.valueOf(enumType, enumArg.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
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