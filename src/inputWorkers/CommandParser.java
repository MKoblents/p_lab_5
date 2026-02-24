package inputWorkers;

import java.io.IOException;
import io.Reader;
/**
 * Parses command-line input into structured arguments.
 * Extracts command name and typed arguments (long, double, enum, XML, path) from raw input.
 */
public class CommandParser {
    /** Parsed command name (lowercase). */
    private String commandName;
    /** Last parsed long argument (0 if invalid/missing). */
    private long longArg;
    /** Last parsed double argument (0.0 if invalid/missing). */
    private double doubleArg;
    /** Last parsed int argument (0 if invalid/missing). */
    private int intArg;
    /** Cached XML string argument for add/update commands. */
    private String xmlArg;
    /** File path argument for script execution. */
    private String pathArg;
    /** Raw enum string for deferred parsing via {@link #getEnumValue(Class)}. */
    private String enumArg;
    /**
     * Joins array elements [start, finish) into lowercase string.
     * @param strings source array
     * @param start inclusive start index
     * @param finish exclusive end index
     * @return concatenated lowercase string, or null on index error
     */
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
    /**
     * Parses next line from Reader and populates argument fields.
     * Handles comments (#), empty lines, and command-specific argument extraction.
     * @param reader input source to read from
     * @throws IOException if read operation fails
     * @implNote Sets fields directly; use getters to retrieve values after parse
     */
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
    /**
     * Converts cached enum string to typed enum constant.
     * @param enumType target enum class
     * @return matched constant, or null if invalid/missing
     * @param <T> enum type parameter
     */
    public <T extends Enum<T>> T getEnumValue(Class<T> enumType) {
        if (enumArg == null) return null;
        try {
            return Enum.valueOf(enumType, enumArg.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    /** @return parsed command name or null */
    public String getCommandName() { return commandName; }
    /** @return last long argument (0 if unset) */
    public long getLongArg() { return longArg; }
    /** @return last double argument (0.0 if unset) */
    public double getDoubleArg() { return doubleArg; }
    /** @return last int argument (0 if unset) */
    public int getIntArg() { return intArg; }
    /** @return cached XML string or null */
    public String getXmlArg() { return xmlArg; }
    /** @return script path argument or null */
    public String getPathArg() { return pathArg; }
    /** @return raw enum string for deferred parsing */
    public String getEnumArg() { return enumArg; }
}